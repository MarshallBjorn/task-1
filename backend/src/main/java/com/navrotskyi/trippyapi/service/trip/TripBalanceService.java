package com.navrotskyi.trippyapi.service.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.trip.ParticipantBalanceDto;
import com.navrotskyi.trippyapi.dto.trip.SettlementDto;
import com.navrotskyi.trippyapi.dto.trip.TripBalanceResponse;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;

/**
 * Orchestrator dla endpointu {@code GET /api/trips/{tripId}/balances}.
 *
 * <p>Pobiera dane wycieczki, wylicza netto-bilanse uczestników (z obsługą groszy
 * resztkowych z dzielenia), przekazuje do {@link DebtSettlementService} i mapuje
 * wynik do DTO.</p>
 *
 * <p><b>Reguły biznesowe:</b></p>
 * <ul>
 *   <li>Tylko zaakceptowani uczestnicy ({@code isAccepted=true}) są w grze.</li>
 *   <li>Wydatki z {@code isSeparate=true} są pomijane (reporter zapłacił "za siebie").</li>
 *   <li>Caller musi być zaakceptowanym uczestnikiem wycieczki — inaczej 403.</li>
 *   <li>Każdy node musi mieć reportera który jest zaakceptowanym uczestnikiem —
 *       inaczej {@code IllegalStateException} (inkonsystencja danych).</li>
 * </ul>
 */
@Service
public class TripBalanceService {

    private static final BigDecimal ONE_GROSZ = new BigDecimal("0.01");

    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final TripNodeRepository tripNodeRepository;
    private final DebtSettlementService debtSettlementService;
    private final TripBalancePdfReportService pdfReportService;

    public TripBalanceService(
            TripEventRepository tripEventRepository,
            TripParticipantRepository tripParticipantRepository,
            TripNodeRepository tripNodeRepository,
            DebtSettlementService debtSettlementService,
            TripBalancePdfReportService pdfReportService
    ) {
        this.tripEventRepository = tripEventRepository;
        this.tripParticipantRepository = tripParticipantRepository;
        this.tripNodeRepository = tripNodeRepository;
        this.debtSettlementService = debtSettlementService;
        this.pdfReportService = pdfReportService;
    }

    @Transactional(readOnly = true)
    public TripBalanceResponse getBalances(UUID tripId, User currentUser) {
        // 1. Trip musi istnieć
        TripEvent trip = tripEventRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + tripId));

        // 2. Fetch zaakceptowanych uczestników (deterministyczna kolejność po userId)
        List<TripParticipant> accepted = tripParticipantRepository
                .findAllByEventIdAndIsAcceptedTrue(tripId)
                .stream()
                .sorted(Comparator.comparing(p -> p.getUser().getId()))
                .toList();

        // 3. Auth: caller musi być zaakceptowanym uczestnikiem
        boolean callerIsParticipant = accepted.stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
        if (!callerIsParticipant) {
            throw new SecurityException("Nie jesteś zaakceptowanym uczestnikiem tej wycieczki.");
        }

        // 4. Fetch nodów
        List<TripNode> nodes = tripNodeRepository.findByEventId(tripId);

        // 5. Wylicz bilanse
        BalanceComputation computation = computeNetBalances(accepted, nodes);

        // 6. Wywołaj algorytm (pusty input → pusta lista)
        List<Settlement> settlements = computation.netBalances().isEmpty()
                ? List.of()
                : debtSettlementService.settle(computation.netBalances());

        // 7. Zbuduj response
        return buildResponse(trip, accepted, computation, settlements);
    }

    /**
 * Generuje raport PDF rozliczeń. Reużywa {@link #getBalances} (ta sama
    * autoryzacja: tylko zaakceptowany uczestnik), a następnie renderuje dokument.
    */
    @Transactional(readOnly = true)
    public byte[] exportBalancesPdf(UUID tripId, User currentUser) {
        TripBalanceResponse data = getBalances(tripId, currentUser);
        TripEvent trip = tripEventRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + tripId));
        return pdfReportService.generate(trip, data);
    }

    // ---------------------- balance computation ----------------------

    private record BalanceComputation(
            BigDecimal totalShared,
            Map<UUID, BigDecimal> netBalances
    ) {}

    private BalanceComputation computeNetBalances(List<TripParticipant> accepted, List<TripNode> nodes) {
        int n = accepted.size();
        if (n == 0) {
            return new BalanceComputation(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY), Map.of());
        }

        // Zbiór ID zaakceptowanych (do walidacji reporterów).
        List<UUID> acceptedUserIds = accepted.stream().map(p -> p.getUser().getId()).toList();

        // 5a. Sumy per-user z nodów niepodzielonych (!isSeparate).
        BigDecimal totalShared = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        Map<UUID, BigDecimal> paidByUser = new HashMap<>();
        for (UUID uid : acceptedUserIds) {
            paidByUser.put(uid, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        }

        for (TripNode node : nodes) {
            if (node.isSeparate()) continue;
            if (node.getPrice() == null || node.getPrice().signum() == 0) continue;

            UUID reporterId = node.getReporter().getId();
            if (!paidByUser.containsKey(reporterId)) {
                throw new IllegalStateException(
                        "Node " + node.getId() + " ma reportera " + reporterId
                                + " który nie jest zaakceptowanym uczestnikiem — dane niespójne.");
            }

            BigDecimal price = node.getPrice().setScale(2, RoundingMode.HALF_UP);
            totalShared = totalShared.add(price);
            paidByUser.merge(reporterId, price, BigDecimal::add);
        }

        // 5b. Jeśli nic nie współdzielono lub jest tylko 1 uczestnik → wszystkie bilanse 0.
        if (totalShared.signum() == 0 || n == 1) {
            Map<UUID, BigDecimal> zeros = new HashMap<>();
            for (UUID uid : acceptedUserIds) {
                zeros.put(uid, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
            }
            return new BalanceComputation(totalShared, zeros);
        }

        // 5c. baseShare + residual (grosze). Dzielimy z DOWN, resztę distr. do pierwszych residualCents osób.
        BigDecimal bdN = BigDecimal.valueOf(n);
        BigDecimal baseShare = totalShared.divide(bdN, 2, RoundingMode.DOWN);
        BigDecimal totalAllocated = baseShare.multiply(bdN);
        BigDecimal residual = totalShared.subtract(totalAllocated);          // 0 ≤ residual < n * 0.01
        long residualCents = residual.movePointRight(2).longValueExact();    // 0 ≤ residualCents < n

        // 5d. Budujemy netBalance = paid - owed.
        Map<UUID, BigDecimal> netBalances = new HashMap<>();
        for (int i = 0; i < acceptedUserIds.size(); i++) {
            UUID uid = acceptedUserIds.get(i);
            BigDecimal owed = i < residualCents ? baseShare.add(ONE_GROSZ) : baseShare;
            BigDecimal paid = paidByUser.get(uid);
            netBalances.put(uid, paid.subtract(owed));
        }
        return new BalanceComputation(totalShared, netBalances);
    }

    // ---------------------- response mapping ----------------------

    private TripBalanceResponse buildResponse(
            TripEvent trip,
            List<TripParticipant> accepted,
            BalanceComputation computation,
            List<Settlement> settlements
    ) {
        Map<UUID, String> userNames = new HashMap<>();
        for (TripParticipant p : accepted) {
            User u = p.getUser();
            userNames.put(u.getId(), u.getName());
        }

        List<ParticipantBalanceDto> balanceDtos = new ArrayList<>(accepted.size());
        for (TripParticipant p : accepted) {
            UUID uid = p.getUser().getId();
            BigDecimal bal = computation.netBalances().getOrDefault(uid, BigDecimal.ZERO.setScale(2));
            balanceDtos.add(new ParticipantBalanceDto(uid, p.getUser().getName(), bal));
        }

        List<SettlementDto> settlementDtos = new ArrayList<>(settlements.size());
        for (Settlement s : settlements) {
            settlementDtos.add(new SettlementDto(
                    s.fromUserId(),
                    userNames.getOrDefault(s.fromUserId(), "?"),
                    s.toUserId(),
                    userNames.getOrDefault(s.toUserId(), "?"),
                    s.amount()
            ));
        }

        return new TripBalanceResponse(
                trip.getId(),
                trip.getCurrency().getCode(),
                computation.totalShared(),
                balanceDtos,
                settlementDtos
        );
    }
}
