package com.navrotskyi.trippyapi.service.trip;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pojedynczy przelew w rozliczeniu wycieczki: {@code fromUserId} → {@code toUserId}, kwota {@code amount}.
 *
 * <p>Niemutowalny rekord używany jako wynik {@link DebtSettlementService#settle}.
 * Kwota jest zawsze dodatnia i ma dokładnie 2 miejsca po przecinku (grosze).</p>
 */
public record Settlement(
        UUID fromUserId,
        UUID toUserId,
        BigDecimal amount
) {}
