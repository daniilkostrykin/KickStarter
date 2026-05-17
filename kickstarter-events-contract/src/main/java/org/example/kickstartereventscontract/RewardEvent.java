package org.example.kickstartereventscontract;

import java.math.BigDecimal;

public sealed interface RewardEvent {

    record Created(
            Long rewardId,
            String title,
            String description,
            BigDecimal minPrice,
            Long projectId
    ) implements RewardEvent {}

}