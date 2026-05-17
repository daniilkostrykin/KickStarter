package org.example.kickstartereventscontract;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public sealed interface ProjectEvent {

    record Created(
            Long projectId,
            String title,
            String description,
            BigDecimal goal,
            OffsetDateTime deadline,
            Long authorId
    ) implements ProjectEvent {}

    record Updated(
            Long projectId,
            String title,
            BigDecimal pledged,
            String status
    ) implements ProjectEvent {}

    record Deleted(
            Long projectId,
            String title
    ) implements ProjectEvent {}
}