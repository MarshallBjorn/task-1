package com.navrotskyi.trippyapi.service.trip;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure-JUnit testy algorytmu rozliczeń. Żadnych zależności Springa ani Mockito —
 * {@link DebtSettlementService} to pure logic bez kolaboratorów.
 *
 * <p>Testy używają deterministycznych UUIDów (A &lt; B &lt; C &lt; D &lt; E) żeby ustalić
 * spodziewaną kolejność iteracji. Tam gdzie PriorityQueue może tasować remisy po wartości,
 * asercje sprawdzają <b>niezmienniki</b> (suma transferów z/do węzła, liczba transakcji)
 * zamiast konkretnej kolejności.</p>
 */
class DebtSettlementServiceTest {

    private final DebtSettlementService service = new DebtSettlementService();

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID D = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID E = UUID.fromString("00000000-0000-0000-0000-000000000005");

    // ======================================================================
    // Walidacja wejścia
    // ======================================================================

    @Test
    @DisplayName("null input → NullPointerException (Objects.requireNonNull)")
    void rejectsNullInput() {
        assertThatThrownBy(() -> service.settle(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("null userId w mapie → IllegalArgumentException")
    void rejectsNullUserId() {
        Map<UUID, BigDecimal> m = new HashMap<>();
        m.put(null, bd("0.00"));

        assertThatThrownBy(() -> service.settle(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null userId");
    }

    @Test
    @DisplayName("null wartość bilansu → IllegalArgumentException")
    void rejectsNullBalance() {
        Map<UUID, BigDecimal> m = new HashMap<>();
        m.put(A, null);

        assertThatThrownBy(() -> service.settle(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is null");
    }

    @Test
    @DisplayName("scale > 2 (np. 0.001) → IllegalArgumentException")
    void rejectsSubCentPrecision() {
        Map<UUID, BigDecimal> m = balances(
                A, "0.001",
                B, "-0.001"
        );

        assertThatThrownBy(() -> service.settle(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scale");
    }

    @Test
    @DisplayName("suma bilansów ≠ 0 → IllegalArgumentException")
    void rejectsNonZeroSum() {
        Map<UUID, BigDecimal> m = balances(
                A, "100.00",
                B, "-50.00" // łącznie +50, niezbalansowane
        );

        assertThatThrownBy(() -> service.settle(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sum to zero");
    }

    // ======================================================================
    // Edge cases
    // ======================================================================

    @Test
    @DisplayName("Pusta mapa → pusta lista przelewów")
    void emptyMapReturnsEmptyList() {
        List<Settlement> result = service.settle(Map.of());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Wszyscy uczestnicy na zero → pusta lista przelewów")
    void allZeroBalancesReturnEmpty() {
        Map<UUID, BigDecimal> m = balances(
                A, "0.00",
                B, "0.00",
                C, "0.00"
        );

        List<Settlement> result = service.settle(m);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Jeden uczestnik z bilansem 0 → pusta lista (nic do rozliczenia)")
    void singleZeroParticipantReturnsEmpty() {
        List<Settlement> result = service.settle(Map.of(A, bd("0.00")));
        assertThat(result).isEmpty();
    }

    // ======================================================================
    // Podstawowe przypadki
    // ======================================================================

    @Test
    @DisplayName("Dwie osoby perfect match: A=+100, B=-100 → 1 przelew B→A 100")
    void twoPersonsPerfectMatch() {
        Map<UUID, BigDecimal> m = balances(
                A, "100.00",
                B, "-100.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(1);
        Settlement s = result.get(0);
        assertThat(s.fromUserId()).isEqualTo(B);
        assertThat(s.toUserId()).isEqualTo(A);
        assertThat(s.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Jeden kredytor, dwóch dłużników: A=+200, B=-100, C=-100 → 2 przelewy")
    void oneCreditorTwoDebtors() {
        Map<UUID, BigDecimal> m = balances(
                A, "200.00",
                B, "-100.00",
                C, "-100.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.toUserId().equals(A));
        assertThat(sumOfTransfersTo(result, A)).isEqualByComparingTo("200.00");
        assertThat(sumOfTransfersFrom(result, B)).isEqualByComparingTo("100.00");
        assertThat(sumOfTransfersFrom(result, C)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Dwóch kredytorów, jeden dłużnik: A=+100, B=+100, C=-200 → 2 przelewy")
    void twoCreditorsOneDebtor() {
        Map<UUID, BigDecimal> m = balances(
                A, "100.00",
                B, "100.00",
                C, "-200.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.fromUserId().equals(C));
        assertThat(sumOfTransfersFrom(result, C)).isEqualByComparingTo("200.00");
        assertThat(sumOfTransfersTo(result, A)).isEqualByComparingTo("100.00");
        assertThat(sumOfTransfersTo(result, B)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Precyzja do grosza: A=+0.01, B=-0.01 → 1 przelew 0.01")
    void subCentPrecisionRoundTrip() {
        Map<UUID, BigDecimal> m = balances(
                A, "0.01",
                B, "-0.01"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("0.01");
    }

    // ======================================================================
    // Zero-sum subset optimization (flagship feature DP)
    // ======================================================================

    @Test
    @DisplayName("Subset opt: A=+100, B=-100, C=+200, D=-200 → 2 przelewy (nie 3)")
    void zeroSumSubsetsSplitCleanly() {
        Map<UUID, BigDecimal> m = balances(
                A, "100.00",
                B, "-100.00",
                C, "200.00",
                D, "-200.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(2);
        // Każdy dłużnik płaci dokładnie jednemu kredytorowi bo mamy 2 czyste subsety
        assertThat(sumOfTransfersFrom(result, B)).isEqualByComparingTo("100.00");
        assertThat(sumOfTransfersFrom(result, D)).isEqualByComparingTo("200.00");
        assertThat(sumOfTransfersTo(result, A)).isEqualByComparingTo("100.00");
        assertThat(sumOfTransfersTo(result, C)).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Subset opt gdzie greedy suboptymalny: A=+3, B=-3, C=+5, D=-2, E=-3 → 3 przelewy (greedy by zrobił 4)")
    void dpBeatsGreedy() {
        // Greedy flow:
        //  C+5 vs B-3: transfer 3, C=2
        //  C+2 vs E-3: transfer 2, C=0, E=-1
        //  A+3 vs E-1: transfer 1, A=2, E=0
        //  A+2 vs D-2: transfer 2, A=0, D=0
        //  → 4 transakcje.
        // DP znajduje subsety {A,B} i {C,D,E} → 5 − 2 = 3 transakcje.
        Map<UUID, BigDecimal> m = balances(
                A, "3.00",
                B, "-3.00",
                C, "5.00",
                D, "-2.00",
                E, "-3.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result)
                .as("DP powinien znaleźć 2 zero-sum subsety → 3 przelewy, nie 4")
                .hasSize(3);
    }

    // ======================================================================
    // Showcase — realny przypadek Zakopane 2026
    // ======================================================================

    @Test
    @DisplayName("Showcase Zakopane: Jan +200, Anna +1200, Marek -600, Zofia -600, Piotr -200 → 3 przelewy")
    void showcaseZakopaneCase() {
        UUID jan    = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID anna   = UUID.fromString("00000000-0000-0000-0000-000000000020");
        UUID marek  = UUID.fromString("00000000-0000-0000-0000-000000000030");
        UUID zofia  = UUID.fromString("00000000-0000-0000-0000-000000000040");
        UUID piotr  = UUID.fromString("00000000-0000-0000-0000-000000000050");

        Map<UUID, BigDecimal> m = balances(
                jan,    "200.00",
                anna,   "1200.00",
                marek,  "-600.00",
                zofia,  "-600.00",
                piotr,  "-200.00"
        );

        List<Settlement> result = service.settle(m);

        assertThat(result).hasSize(3);
        // Każdy dłużnik oddaje dokładnie swój netto-deficyt
        assertThat(sumOfTransfersFrom(result, marek)).isEqualByComparingTo("600.00");
        assertThat(sumOfTransfersFrom(result, zofia)).isEqualByComparingTo("600.00");
        assertThat(sumOfTransfersFrom(result, piotr)).isEqualByComparingTo("200.00");
        // Każdy kredytor dostaje dokładnie swój netto-plus
        assertThat(sumOfTransfersTo(result, jan)).isEqualByComparingTo("200.00");
        assertThat(sumOfTransfersTo(result, anna)).isEqualByComparingTo("1200.00");
    }

    // ======================================================================
    // Globalne niezmienniki (prawo zachowania pieniędzy)
    // ======================================================================

    @Test
    @DisplayName("Invariant: każda kwota przelewu > 0 i scale = 2")
    void everyAmountIsPositiveWithScaleTwo() {
        Map<UUID, BigDecimal> m = balances(
                A, "3.00",
                B, "-3.00",
                C, "5.00",
                D, "-2.00",
                E, "-3.00"
        );

        List<Settlement> result = service.settle(m);

        for (Settlement s : result) {
            assertThat(s.amount())
                    .as("amount musi być dodatni: %s", s)
                    .isGreaterThan(BigDecimal.ZERO);
            assertThat(s.amount().scale())
                    .as("amount musi mieć scale=2: %s", s)
                    .isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Invariant: dla każdego uczestnika suma (transfery_in − transfery_out) == jego bilans")
    void conservationOfMoney() {
        Map<UUID, BigDecimal> m = balances(
                A, "3.00",
                B, "-3.00",
                C, "5.00",
                D, "-2.00",
                E, "-3.00"
        );

        List<Settlement> result = service.settle(m);

        for (Map.Entry<UUID, BigDecimal> e : m.entrySet()) {
            BigDecimal inflow  = sumOfTransfersTo(result, e.getKey());
            BigDecimal outflow = sumOfTransfersFrom(result, e.getKey());
            BigDecimal net = inflow.subtract(outflow);
            assertThat(net)
                    .as("Netto transferów dla %s musi równać się bilansowi %s", e.getKey(), e.getValue())
                    .isEqualByComparingTo(e.getValue());
        }
    }

    // ======================================================================
    // Helpers
    // ======================================================================

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    /** LinkedHashMap — zachowana kolejność insercji dla determinizmu testów. */
    private static Map<UUID, BigDecimal> balances(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("balances() wymaga par klucz-wartość");
        }
        Map<UUID, BigDecimal> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((UUID) kv[i], new BigDecimal((String) kv[i + 1]));
        }
        return m;
    }

    private static BigDecimal sumOfTransfersFrom(List<Settlement> transfers, UUID userId) {
        return transfers.stream()
                .filter(s -> s.fromUserId().equals(userId))
                .map(Settlement::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumOfTransfersTo(List<Settlement> transfers, UUID userId) {
        return transfers.stream()
                .filter(s -> s.toUserId().equals(userId))
                .map(Settlement::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
