package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.admin.NodeResponse;
import com.navrotskyi.trippyapi.dto.trip.CreateTripNodeRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service zarządzający węzłami wycieczki ({@link TripNode}).
 * <p>
 * <b>Uwaga architektoniczna:</b> w modelu Trippy <i>wydatek</i> (dawny "Expense")
 * nie jest osobnym bytem — to zwykły {@link TripNode} z dodatnią ceną oraz flagą
 * {@code isSeparate}. Dawny {@code ExpenseService} / {@code ExpenseController}
 * został w całości wchłonięty tutaj: tworzenie wydatku = utworzenie węzła z ceną,
 * a listowanie wydatków = listowanie węzłów. Dzięki temu istnieje jedna ścieżka
 * tworzenia/odczytu/edycji i jeden komplet reguł autoryzacji.
 * <p>
 * Reguły dostępu są spójne dla wszystkich operacji:
 * <ul>
 *   <li><b>Odczyt</b> (lista, pojedynczy node) — wymaga bycia <i>zaakceptowanym</i>
 *       uczestnikiem wycieczki ({@code isAccepted=true}). Zaproszeni, ale nieaktywowani
 *       uczestnicy nie mają dostępu.</li>
 *   <li><b>Tworzenie</b> — wymaga bycia zaakceptowanym uczestnikiem.</li>
 *   <li><b>Aktualizacja / usuwanie</b> — wymaga bycia <i>autorem</i> węzła
 *       <b>lub</b> ORGANIZATOREM wycieczki. W obu przypadkach wykonujący
 *       musi być zaakceptowanym uczestnikiem.</li>
 * </ul>
 * <p>
 * Wszystkie metody zapisujące/modyfikujące zwracają w pełni zainicjalizowaną encję
 * (z załadowanymi relacjami {@code event} i {@code reporter}), tak aby mapper
 * mógł bezpiecznie skonwertować je na DTO bez {@code LazyInitializationException}.
 */
@Service
public class TripNodeService {

    private static final String ROLE_ORGANIZER = "ORGANIZER";

    private final TripNodeRepository tripNodeRepository;
    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public TripNodeService(TripNodeRepository tripNodeRepository,
                           TripEventRepository tripEventRepository,
                           TripParticipantRepository tripParticipantRepository) {
        this.tripNodeRepository = tripNodeRepository;
        this.tripEventRepository = tripEventRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    // ============================================================
    //  CREATE
    // ============================================================

    /**
     * Tworzy nowy węzeł w obrębie podanej wycieczki.
     * <p>
     * Obsługuje zarówno "klasyczne" elementy planu (lot, hotel, atrakcja), jak i
     * wydatki — różnica jest wyłącznie w danych ({@code price > 0}, {@code isSeparate}).
     *
     * @param eventId  identyfikator wycieczki
     * @param request  dane węzła (już zwalidowane przez Bean Validation w kontrolerze)
     * @param reporter aktualnie zalogowany użytkownik (autor węzła)
     * @return świeżo zapisany węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy wycieczka nie istnieje
     * @throws SecurityException         gdy użytkownik nie jest zaakceptowanym uczestnikiem
     * @throws IllegalArgumentException  gdy {@code endTime <= startTime}
     */
    @Transactional
    public TripNode createTripNode(UUID eventId, CreateTripNodeRequest request, User reporter) {
        validateTimeRange(request);

        TripEvent event = tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono wycieczki o id: " + eventId));

        requireAcceptedParticipant(eventId, reporter.getId());

        TripNode node = new TripNode();
        node.setEvent(event);
        node.setReporter(reporter);
        node.setName(request.name());
        node.setStartTime(request.startTime());
        node.setEndTime(request.endTime());
        node.setNote(request.note());
        node.setPrice(request.price());
        node.setSeparate(request.isSeparate());

        TripNode savedNode = tripNodeRepository.save(node);
        // Dotykamy relacji, by były zainicjalizowane przed mapowaniem na DTO
        // (ochrona przed LazyInitializationException poza transakcją).
        savedNode.getReporter().getName();
        savedNode.getEvent().getId();

        return savedNode;
    }

    // ============================================================
    //  READ — lista
    // ============================================================

    /**
     * Zwraca wszystkie węzły wycieczki posortowane po {@code startTime} rosnąco,
     * zmapowane na {@link NodeResponse} z flagami {@code canEdit}/{@code canDelete}
     * wyliczonymi względem aktualnego użytkownika.
     *
     * @param eventId identyfikator wycieczki
     * @param user    aktualnie zalogowany użytkownik
     * @return lista węzłów
     * @throws SecurityException gdy użytkownik nie jest zaakceptowanym uczestnikiem
     */
    @Transactional(readOnly = true)
    public List<NodeResponse> getNodesForEvent(UUID eventId, User user) {
        TripParticipant participant = requireAcceptedParticipant(eventId, user.getId());
        boolean isOrganizer = ROLE_ORGANIZER.equalsIgnoreCase(participant.getTripRole().getName());

        List<TripNode> nodes = tripNodeRepository.findAllByEventIdOrderByStartTimeAsc(eventId);

        return nodes.stream()
                .map(node -> mapToNodeResponse(node, user, isOrganizer))
                .toList();
    }

    private NodeResponse mapToNodeResponse(TripNode node, User currentUser, boolean isOrganizer) {
        boolean isAuthor = node.getReporter().getId().equals(currentUser.getId());
        boolean canEdit = isAuthor || isOrganizer;
        boolean canDelete = isAuthor || isOrganizer;

        return new NodeResponse(
                node.getId(),
                node.getName(),
                node.getNote(),
                node.getPrice(),
                node.isSeparate(),
                node.getReporter().getName(),
                List.of(),
                canEdit,
                canDelete);
    }

    // ============================================================
    //  READ — pojedynczy
    // ============================================================

    /**
     * Zwraca pojedynczy węzeł po jego ID, weryfikując że należy do podanej wycieczki.
     *
     * @param eventId identyfikator wycieczki (z URL — sprawdzamy spójność)
     * @param nodeId  identyfikator węzła
     * @param user    aktualnie zalogowany użytkownik
     * @return węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do podanej wycieczki
     * @throws SecurityException         gdy użytkownik nie jest zaakceptowanym uczestnikiem
     */
    @Transactional(readOnly = true)
    public TripNode getTripNode(UUID eventId, UUID nodeId, User user) {
        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        validateBelongsToTrip(eventId, node);
        requireAcceptedParticipant(eventId, user.getId());
        return node;
    }

    // ============================================================
    //  UPDATE
    // ============================================================

    /**
     * Aktualizuje wszystkie pola istniejącego węzła (semantyka PUT — pełna podmiana).
     *
     * @param eventId     identyfikator wycieczki
     * @param nodeId      identyfikator węzła
     * @param request     nowe dane węzła
     * @param currentUser aktualnie zalogowany użytkownik
     * @return zaktualizowany węzeł z załadowanymi relacjami
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do wycieczki lub czasy są niespójne
     * @throws SecurityException         gdy użytkownik nie jest autorem ani organizatorem
     */
    @Transactional
    public TripNode updateTripNode(UUID eventId, UUID nodeId,
                                   CreateTripNodeRequest request, User currentUser) {
        validateTimeRange(request);

        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        validateBelongsToTrip(eventId, node);
        requireAuthorOrOrganizer(eventId, node, currentUser);

        node.setName(request.name());
        node.setStartTime(request.startTime());
        node.setEndTime(request.endTime());
        node.setNote(request.note());
        node.setPrice(request.price());
        node.setSeparate(request.isSeparate());

        TripNode savedNode = tripNodeRepository.save(node);
        savedNode.getReporter().getName();
        return savedNode;
    }

    // ============================================================
    //  DELETE
    // ============================================================

    /**
     * Usuwa węzeł (kaskadowo: powiązane posty i zdjęcia).
     *
     * @param eventId     identyfikator wycieczki
     * @param nodeId      identyfikator węzła
     * @param currentUser aktualnie zalogowany użytkownik
     * @throws ResourceNotFoundException gdy węzeł nie istnieje
     * @throws IllegalArgumentException  gdy węzeł nie należy do wycieczki
     * @throws SecurityException         gdy użytkownik nie jest autorem ani organizatorem
     */
    @Transactional
    public void deleteTripNode(UUID eventId, UUID nodeId, User currentUser) {
        TripNode node = tripNodeRepository.findWithDetailsById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nie znaleziono węzła o id: " + nodeId));

        validateBelongsToTrip(eventId, node);
        requireAuthorOrOrganizer(eventId, node, currentUser);

        tripNodeRepository.delete(node);
    }

    // ============================================================
    //  PRIVATE HELPERS — autoryzacja i walidacje
    // ============================================================

    /**
     * Sprawdza, czy użytkownik jest zaakceptowanym uczestnikiem wycieczki.
     * Zaproszeni, ale nieaktywowani ({@code isAccepted=false}) są blokowani.
     */
    private TripParticipant requireAcceptedParticipant(UUID eventId, UUID userId) {
        TripParticipant participant = tripParticipantRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new SecurityException(
                        "Nie jesteś uczestnikiem tej wycieczki."));

        if (!participant.isAccepted()) {
            throw new SecurityException(
                    "Nie zaakceptowałeś jeszcze zaproszenia do tej wycieczki.");
        }
        return participant;
    }

    /**
     * Sprawdza, czy podany użytkownik może modyfikować/usuwać węzeł:
     * musi być zaakceptowanym uczestnikiem oraz albo autorem węzła,
     * albo organizatorem wycieczki.
     */
    private void requireAuthorOrOrganizer(UUID eventId, TripNode node, User currentUser) {
        TripParticipant participant = requireAcceptedParticipant(eventId, currentUser.getId());

        boolean isAuthor = node.getReporter().getId().equals(currentUser.getId());
        boolean isOrganizer = ROLE_ORGANIZER.equalsIgnoreCase(participant.getTripRole().getName());

        if (!isAuthor && !isOrganizer) {
            throw new SecurityException(
                    "Brak uprawnień. Musisz być autorem węzła lub organizatorem wycieczki.");
        }
    }

    /**
     * Sprawdza, że węzeł rzeczywiście należy do wycieczki podanej w URL.
     * Chroni przed sytuacją, w której ktoś podaje {@code /api/trips/A/nodes/X},
     * gdzie {@code X} należy do wycieczki {@code B}.
     */
    private void validateBelongsToTrip(UUID eventId, TripNode node) {
        if (!node.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException(
                    "Węzeł " + node.getId() + " nie należy do wycieczki " + eventId + ".");
        }
    }

    /**
     * Cross-field walidacja: {@code endTime} musi być po {@code startTime}.
     * Bean Validation nie pokrywa walidacji wzajemnej dwóch pól bez customowych adnotacji,
     * więc robimy to ręcznie tutaj.
     */
    private void validateTimeRange(CreateTripNodeRequest request) {
        if (request.endTime() == null || request.startTime() == null) {
            return; // złapie @NotNull z DTO — defensive check
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException(
                    "Pole endTime musi być późniejsze niż startTime.");
        }
    }
}