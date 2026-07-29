package com.navrotskyi.trippyapi.service.trip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Pure-logic service wyliczający minimalną listę transakcji (Simplified Debt Graph),
 * które wyrównają netto-bilanse uczestników wycieczki do zera.
 *
 * <p>Hybrydowa strategia:</p>
 * <ul>
 *   <li><b>Greedy</b> (zawsze): O(N log N), gwarantuje ≤ N−1 transakcji.</li>
 *   <li><b>DP + bitmask</b> (tylko gdy N ≤ {@value #DP_THRESHOLD}): O(3^N · N) — znajduje
 *       partycjonowanie bilansów na maksymalną liczbę zero-sum subsetów, co
 *       daje prawdziwe minimum transakcji (N − liczba_subsetów). W obrębie każdego
 *       subsetu transfery generowane są greedy (co dla irreducible subsetu jest optymalne).</li>
 * </ul>
 *
 * <p>Zwracany jest wynik z mniejszą liczbą transakcji. Dla N &gt; {@value #DP_THRESHOLD}
 * używany jest tylko greedy — 3^20 ≈ 3.5 mld operacji byłoby zbyt wolne, a realnie
 * grupy turystyczne nie przekraczają kilkunastu osób.</p>
 *
 * <p><b>Precondition:</b> wartości w mapie muszą sumować się dokładnie do zera
 * i mieć {@code scale ≤ 2} (grosze). Obliczanie bilansów i obsługa groszy
 * resztkowych (np. z dzielenia 100 zł przez 3 osoby) są odpowiedzialnością warstwy
 * wyżej ({@code TripBalanceService}).</p>
 */
@Service
public class DebtSettlementService {

    /** Próg dla gałęzi DP — 3^15 ≈ 14M operacji, bezpieczne pod ~100ms. */
    static final int DP_THRESHOLD = 15;

    private static final int SCALE = 2;

    /**
     * Wylicza listę przelewów rozliczających podane netto-bilanse.
     *
     * @param netBalances mapa {@code userId → netBalance}; dodatni bilans = grupa winna mu pieniądze,
     *                    ujemny = on winien grupie. Musi sumować się do zera.
     * @return lista przelewów o minimalnej liczbie. Pusta, gdy wszystkie bilanse są zerowe.
     * @throws IllegalArgumentException gdy walidacja wejścia nie przejdzie.
     */
    public List<Settlement> settle(Map<UUID, BigDecimal> netBalances) {
        validateInput(netBalances);

        // Konwersja do groszy (long) — szybsze i odporne na quirki BigDecimal w hot-path.
        List<UUID> nonZeroIds = new ArrayList<>();
        List<Long> nonZeroCents = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> e : netBalances.entrySet()) {
            long c = toCents(e.getValue(), e.getKey());
            if (c != 0) {
                nonZeroIds.add(e.getKey());
                nonZeroCents.add(c);
            }
        }

        if (nonZeroIds.isEmpty()) {
            return List.of();
        }

        int n = nonZeroIds.size();
        UUID[] ids = nonZeroIds.toArray(new UUID[0]);
        long[] cents = new long[n];
        for (int i = 0; i < n; i++) {
            cents[i] = nonZeroCents.get(i);
        }

        List<Settlement> greedy = greedySettle(ids, cents.clone());

        if (n <= DP_THRESHOLD) {
            List<Settlement> optimal = optimalSettle(ids, cents);
            if (optimal.size() < greedy.size()) {
                return optimal;
            }
        }
        return greedy;
    }

    // ---------------------- validation ----------------------

    private void validateInput(Map<UUID, BigDecimal> netBalances) {
        Objects.requireNonNull(netBalances, "netBalances must not be null");

        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<UUID, BigDecimal> e : netBalances.entrySet()) {
            if (e.getKey() == null) {
                throw new IllegalArgumentException("netBalances contains null userId");
            }
            if (e.getValue() == null) {
                throw new IllegalArgumentException("netBalance is null for user " + e.getKey());
            }
            if (e.getValue().scale() > SCALE) {
                throw new IllegalArgumentException(
                        "netBalance scale > 2 (groszowa precyzja wymagana) for user "
                                + e.getKey() + ": " + e.getValue());
            }
            sum = sum.add(e.getValue());
        }
        if (sum.signum() != 0) {
            throw new IllegalArgumentException(
                    "netBalances do not sum to zero (residual=" + sum
                            + "). Caller musi wyrównać grosze resztkowe przed wywołaniem.");
        }
    }

    private long toCents(BigDecimal value, UUID userId) {
        try {
            return value.movePointRight(SCALE).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "netBalance poza zakresem long (cents) dla user " + userId + ": " + value, ex);
        }
    }

    private static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, SCALE);
    }

    // ---------------------- greedy ----------------------

    /**
     * Klasyczny Simplified Debt Graph: max-heap creditorów i debtorów, w każdym kroku
     * dopasowujemy największego creditora z największym (|amount|) debtorem i transferujemy
     * {@code min(credit, |debt|)}. Gwarantuje ≤ N−1 transakcji.
     */
    private List<Settlement> greedySettle(UUID[] ids, long[] cents) {
        PriorityQueue<Integer> creditors = new PriorityQueue<>(
                (a, b) -> Long.compare(cents[b], cents[a])); // max-heap po wartości
        PriorityQueue<Integer> debtors = new PriorityQueue<>(
                (a, b) -> Long.compare(cents[a], cents[b])); // min-heap po wartości (najbardziej ujemny na topie)

        for (int i = 0; i < cents.length; i++) {
            if (cents[i] > 0) creditors.offer(i);
            else if (cents[i] < 0) debtors.offer(i);
        }

        List<Settlement> result = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            int cIdx = creditors.poll();
            int dIdx = debtors.poll();
            long transfer = Math.min(cents[cIdx], -cents[dIdx]);

            result.add(new Settlement(ids[dIdx], ids[cIdx], fromCents(transfer)));

            cents[cIdx] -= transfer;
            cents[dIdx] += transfer;

            if (cents[cIdx] > 0) creditors.offer(cIdx);
            if (cents[dIdx] < 0) debtors.offer(dIdx);
        }
        return result;
    }

    // ---------------------- optimal (DP + bitmask) ----------------------

    /**
     * Znajduje partycjonowanie zbioru niezerowych bilansów na maksymalną liczbę
     * zero-sum subsetów. Wewnątrz każdego subsetu generuje transfery przez greedy.
     *
     * <p>Kluczowa obserwacja: dla zbioru N niezerowych bilansów sumujących się do 0,
     * jeśli da się go podzielić na S zero-sum subsetów, to minimum transakcji = N − S.
     * Każdy subset o rozmiarze k domyka się k−1 transakcjami (z greedy).</p>
     *
     * <p>Złożoność: O(3^N · N) — iteracja po wszystkich submaskach wszystkich masek.</p>
     */
    private List<Settlement> optimalSettle(UUID[] ids, long[] cents) {
        final int n = cents.length;
        final int full = (1 << n) - 1;

        // 1. Precompute sum dla każdej maski (O(2^N)).
        long[] maskSum = new long[1 << n];
        for (int mask = 1; mask <= full; mask++) {
            int lowBit = Integer.numberOfTrailingZeros(mask);
            maskSum[mask] = maskSum[mask ^ (1 << lowBit)] + cents[lowBit];
        }

        // 2. dp[mask] = max liczba zero-sum subsetów w partycji `mask`.
        //    chosenSubset[mask] = submaska wybrana jako jeden subset w optymalnej partycji.
        int[] dp = new int[1 << n];
        int[] chosenSubset = new int[1 << n];

        for (int mask = 1; mask <= full; mask++) {
            if (maskSum[mask] != 0) continue; // mask nie jest zero-sum → nie da się spartycjonować
            int bestCount = 0;
            int bestSub = 0;
            // Standardowa iteracja po niepustych submaskach:
            for (int s = mask; s > 0; s = (s - 1) & mask) {
                if (maskSum[s] != 0) continue;
                int rem = mask ^ s;
                int candidate = 1 + dp[rem];
                if (candidate > bestCount) {
                    bestCount = candidate;
                    bestSub = s;
                }
            }
            dp[mask] = bestCount;
            chosenSubset[mask] = bestSub;
        }

        // 3. Rekonstrukcja subsetów.
        List<Integer> subsets = new ArrayList<>();
        int remaining = full;
        while (remaining != 0) {
            int s = chosenSubset[remaining];
            if (s == 0) {
                // Nie powinno się zdarzyć jeśli input sumuje się do 0 (gwarant w validateInput).
                throw new IllegalStateException(
                        "DP nie znalazł partycji dla maski " + Integer.toBinaryString(remaining));
            }
            subsets.add(s);
            remaining ^= s;
        }

        // 4. Greedy wewnątrz każdego subsetu (optymalne dla subsetów irreducible).
        List<Settlement> result = new ArrayList<>();
        for (int subset : subsets) {
            int k = Integer.bitCount(subset);
            UUID[] subIds = new UUID[k];
            long[] subCents = new long[k];
            int idx = 0;
            for (int bit = 0; bit < n; bit++) {
                if ((subset & (1 << bit)) != 0) {
                    subIds[idx] = ids[bit];
                    subCents[idx] = cents[bit];
                    idx++;
                }
            }
            result.addAll(greedySettle(subIds, subCents));
        }
        return result;
    }
}
