package org.example.kickstarter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "pledges", itemRelation = "pledge")
@Schema(description = "Информация о взносах")
public class PledgeResponse extends RepresentationModel<PledgeResponse> {

    @Schema(description = "ID взноса", example = "1")
    private final Long pledgeId;

    @Schema(description = "Статус", example = "ACTIVE")
    private final String status;
    
    @Schema(description = "Дата транзакции", example = "2026-03-29T21:40:45.422407")
    private final LocalDateTime transactionDate;
}