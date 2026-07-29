package com.navrotskyi.trippyapi.integration;

import com.navrotskyi.trippyapi.domain.Currency;
import com.navrotskyi.trippyapi.domain.Role;
import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.trip.ParticipantBalanceDto;
import com.navrotskyi.trippyapi.dto.trip.SettlementDto;
import com.navrotskyi.trippyapi.dto.trip.TripBalanceResponse;
import com.navrotskyi.trippyapi.repository.CurrencyRepository;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.TripRoleRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.service.trip.TripBalanceService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripBalanceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired private TripBalanceService balanceService;
    @Autowired private TripEventRepository tripEventRepository;
    @Autowired private TripParticipantRepository participantRepository;
    @Autowired private TripNodeRepository nodeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private TripRoleRepository tripRoleRepository;
    @Autowired private EntityManager em;

    private Currency pln;
    private TripRole memberRole;

    @BeforeEach
    void seedCommon() {
        pln = currencyRepository.findById("PLN")
                .orElseGet(() -> currencyRepository.save(new Currency("PLN", "Polski złoty")));
        memberRole = tripRoleRepository.findAll().stream()
                .filter(r -> "MEMBER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> tripRoleRepository.save(new TripRole("MEMBER")));
    }

    @Test
    void emptyTrip_returnsZeroBalances() {
        User alice = persistUser("alice@ex.com", "Alice");
        TripEvent trip = persistTrip(alice);
        acceptParticipant(trip, alice);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), alice);

        assertThat(resp.totalSharedExpenses()).isEqualByComparingTo("0.00");
        assertThat(resp.balances()).hasSize(1);
        assertThat(resp.balances().get(0).netBalance()).isEqualByComparingTo("0.00");
        assertThat(resp.settlements()).isEmpty();
    }

    @Test
    void simpleSplit_twoParticipants_oneSettlement() {
        User alice = persistUser("alice@ex.com", "Alice");
        User bob   = persistUser("bob@ex.com", "Bob");
        TripEvent trip = persistTrip(alice);
        acceptParticipant(trip, alice);
        acceptParticipant(trip, bob);
        persistNode(trip, alice, new BigDecimal("100.00"), false);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), alice);

        assertThat(resp.totalSharedExpenses()).isEqualByComparingTo("100.00");
        assertThat(resp.settlements()).hasSize(1);
        SettlementDto s = resp.settlements().get(0);
        assertThat(s.fromUserId()).isEqualTo(bob.getId());
        assertThat(s.toUserId()).isEqualTo(alice.getId());
        assertThat(s.amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void residualGrosze_distributedDeterministically() {
        User a = persistUser("a@ex.com", "A");
        User b = persistUser("b@ex.com", "B");
        User c = persistUser("c@ex.com", "C");
        TripEvent trip = persistTrip(a);
        acceptParticipant(trip, a);
        acceptParticipant(trip, b);
        acceptParticipant(trip, c);
        persistNode(trip, a, new BigDecimal("100.00"), false);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), a);

        BigDecimal sum = resp.balances().stream()
                .map(ParticipantBalanceDto::netBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("0.00");
        assertThat(resp.settlements()).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void separateExpense_excludedFromBalance() {
        User alice = persistUser("alice@ex.com", "Alice");
        User bob   = persistUser("bob@ex.com", "Bob");
        TripEvent trip = persistTrip(alice);
        acceptParticipant(trip, alice);
        acceptParticipant(trip, bob);
        persistNode(trip, alice, new BigDecimal("100.00"), false);
        persistNode(trip, alice, new BigDecimal("999.99"), true);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), alice);

        assertThat(resp.totalSharedExpenses()).isEqualByComparingTo("100.00");
        assertThat(resp.settlements()).hasSize(1);
        assertThat(resp.settlements().get(0).amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void notAcceptedParticipant_isIgnoredInSplit() {
        User alice = persistUser("alice@ex.com", "Alice");
        User bob   = persistUser("bob@ex.com", "Bob");
        User pending = persistUser("pending@ex.com", "Pending");
        TripEvent trip = persistTrip(alice);
        acceptParticipant(trip, alice);
        acceptParticipant(trip, bob);
        persistParticipant(trip, pending, false);
        persistNode(trip, alice, new BigDecimal("100.00"), false);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), alice);

        assertThat(resp.balances()).hasSize(2);
        assertThat(resp.settlements()).hasSize(1);
        assertThat(resp.settlements().get(0).amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void nonParticipant_cannotAccessBalances() {
        User alice = persistUser("alice@ex.com", "Alice");
        User outsider = persistUser("evil@ex.com", "Evil");
        TripEvent trip = persistTrip(alice);
        acceptParticipant(trip, alice);

        assertThatThrownBy(() -> balanceService.getBalances(trip.getId(), outsider))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void multipleExpenses_settlementsZeroSum() {
        User a = persistUser("a@ex.com", "A");
        User b = persistUser("b@ex.com", "B");
        User c = persistUser("c@ex.com", "C");
        TripEvent trip = persistTrip(a);
        acceptParticipant(trip, a);
        acceptParticipant(trip, b);
        acceptParticipant(trip, c);
        persistNode(trip, a, new BigDecimal("60.00"), false);
        persistNode(trip, b, new BigDecimal("30.00"), false);
        persistNode(trip, c, new BigDecimal("90.00"), false);

        TripBalanceResponse resp = balanceService.getBalances(trip.getId(), a);

        assertThat(resp.totalSharedExpenses()).isEqualByComparingTo("180.00");
        BigDecimal sum = resp.balances().stream()
                .map(ParticipantBalanceDto::netBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("0.00");
        assertThat(resp.settlements()).hasSizeLessThanOrEqualTo(2);
    }

    // ---------- helpers ----------

    private User persistUser(String email, String name) {
        User u = new User(name, email, "{noop}pwd", Role.USER, null, pln, true, false);
        return userRepository.save(u);
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
        persistParticipant(trip, user, true);
    }

    private void persistParticipant(TripEvent trip, User user, boolean accepted) {
        TripParticipant p = new TripParticipant(
                trip, user, memberRole, new BigDecimal("0.00"), accepted);
        participantRepository.save(p);
    }

    private void persistNode(TripEvent trip, User reporter, BigDecimal price, boolean separate) {
        TripNode n = new TripNode();
        n.setEvent(trip);
        n.setReporter(reporter);
        n.setName("expense");
        n.setStartTime(LocalDateTime.now());
        n.setEndTime(LocalDateTime.now().plusHours(1));
        n.setPrice(price);
        n.setSeparate(separate);
        nodeRepository.save(n);
        em.flush();
    }
}