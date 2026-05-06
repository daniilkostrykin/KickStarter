package org.example.kickstarterrest.graphql.types;

import java.time.OffsetDateTime;

public record CreateProjectInputGql(
        String title,
        String description,
        java.math.BigDecimal goal,
        java.time.OffsetDateTime deadline,
        String authorId) {}