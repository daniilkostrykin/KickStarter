package org.example.kickstarterrest.graphql.types;

import java.math.BigDecimal;

public record CreateRewardInputGql(
        String projectId,
        String title,
        String description,
        BigDecimal minPrice) {
}