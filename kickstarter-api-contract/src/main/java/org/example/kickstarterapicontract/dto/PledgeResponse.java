package org.example.kickstarterapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "pledges", itemRelation = "pledge")
@Schema(description = "Информация о взносах")
public class PledgeResponse extends RepresentationModel<PledgeResponse> {

    @Schema(description = "ID взноса", example = "1")
    private Long pledgeId;

    @Schema(description = "ID проекта", example = "1")
    private Long projectId;

    @Schema(description = "ID вознаграждения", example = "1")
    private Long rewardId;

    @Schema(description = "ID спонсора", example = "1000")
    private Long userId;

    @Schema(description = "Сумма взноса", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "Статус", example = "AUTHORIZED")
    private PledgeStatus status;

    @Schema(description = "Дата транзакции", example = "2026-03-29T21:40:45.422407")
    private OffsetDateTime transactionDate;
}