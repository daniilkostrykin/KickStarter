package org.example.kickstartereventscontract;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public sealed interface PledgeEvent {

    record Created(
            Long pledgeId,
            Long projectId,
            Long rewardId,
            Long userId,
            BigDecimal amount,
            String status,
            OffsetDateTime transactionDate
    ) implements PledgeEvent {}

}