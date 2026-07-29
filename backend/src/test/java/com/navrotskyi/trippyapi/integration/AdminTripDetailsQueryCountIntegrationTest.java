package com.navrotskyi.trippyapi.integration;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.admin.TripDetailsResponse;
import com.navrotskyi.trippyapi.repository.*;
import com.navrotskyi.trippyapi.service.admin.AdminTripService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTripDetailsQueryCountIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired private AdminTripService adminTripService;
    @Autowired private TripEventRepository tripEventRepository;
    @Autowired private TripParticipantRepository participantRepository;
    @Autowired private TripNodeRepository nodeRepository;
    @Autowired private TripPostRepository postRepository;
    @Autowired private TripPhotoRepository photoRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private TripRoleRepository tripRoleRepository;
    @Autowired private EntityManagerFactory emf;
    @Autowired private EntityManager em;

    private Currency pln;
    private TripRole memberRole;
    private Statistics stats;

    @BeforeEach
    void setUp() {
        pln = currencyRepository.findById("PLN")
                .orElseGet(() -> currencyRepository.save(new Currency("PLN", "Polski złoty")));
        memberRole = tripRoleRepository.findAll().stream()
                .filter(r -> "MEMBER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> tripRoleRepository.save(new TripRole("MEMBER")));
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
    }

    @Test
    void getTripDetails_doesNotScaleWithNodesOrPosts() {
        // 5 nodów × 4 posty × 3 zdjęcia = 60 zdjęć łącznie.
        // Bez optymalizacji: 1 (nodes) + 5 (reporter) + 5 (posts) + 20 (post.reporter) + 20 (photos)
        //                  = ~51 SELECT-ów. Z fixem: <= 6.
        User admin = persistUser("admin@ex.com", "Admin");
        TripEvent trip = persistTrip(admin);
        acceptParticipant(trip, admin);

        for (int i = 0; i < 5; i++) {
            TripNode node = persistNode(trip, admin);
            for (int j = 0; j < 4; j++) {
                TripPost post = persistPost(node, admin);
                for (int k = 0; k < 3; k++) {
                    persistPhoto(post);
                }
            }
        }
        em.flush();
        em.clear(); // wyczyść first-level cache, żeby liczyć FAKTYCZNE zapytania

        stats.clear();

        TripDetailsResponse resp = adminTripService.getTripDetails(trip.getId());

        long queries = stats.getPrepareStatementCount();
        assertThat(resp.nodes()).hasSize(5);
        assertThat(resp.nodes().get(0).posts()).hasSize(4);
        assertThat(resp.nodes().get(0).posts().get(0).photos()).hasSize(3);

        // Górny limit — stała liczba zapytań niezależna od N i M.
        // 1) findById(event) + owner
        // 2) findByEventId(nodes + reporter)
        // 3) findAllByNodeIdInWithReporterAndPhotos
        // + ewentualnie 1-2 dodatkowe (currency, batch fetch lazy).
        assertThat(queries)
                .as("Liczba SELECT-ów nie może rosnąć z N nodów ani M postów")
                .isLessThanOrEqualTo(6);
    }

    // ---------- helpers ----------

    private User persistUser(String email, String name) {
        return userRepository.save(new User(name, email, "{noop}pwd", Role.USER, null, pln, true, false));
    }

    private TripEvent persistTrip(User owner) {
        TripEvent t = new TripEvent();
        t.setOwner(owner);
        t.setName("Test trip");
        t.setCurrency(pln);
        t.setStartDate(LocalDate.now());
        t.setEndDate(LocalDate.now().plusDays(3));
        t.setBudget(new BigDecimal("0.00"));
        return tripEventRepository.save(t);
    }

    private void acceptParticipant(TripEvent trip, User user) {
        participantRepository.save(new TripParticipant(
                trip, user, memberRole, new BigDecimal("0.00"), true));
    }

    private TripNode persistNode(TripEvent trip, User reporter) {
        TripNode n = new TripNode();
        n.setEvent(trip);
        n.setReporter(reporter);
        n.setName("expense");
        n.setStartTime(LocalDateTime.now());
        n.setEndTime(LocalDateTime.now().plusHours(1));
        n.setPrice(new BigDecimal("10.00"));
        n.setSeparate(false);
        return nodeRepository.save(n);
    }

    private TripPost persistPost(TripNode node, User reporter) {
        TripPost p = new TripPost();
        p.setNode(node);
        p.setReporter(reporter);
        p.setNote("post-note");
        return postRepository.save(p);
    }

    private void persistPhoto(TripPost post) {
        TripPhoto ph = new TripPhoto();
        ph.setPost(post);
        ph.setPhotoUrl("/photos/x.jpg");
        photoRepository.save(ph);
    }
}