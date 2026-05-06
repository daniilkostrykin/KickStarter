package org.example.kickstarterapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;


@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о проекте")
@Relation(collectionRelation = "projects", itemRelation = "project")
public class ProjectResponse extends RepresentationModel<ProjectResponse> {

    @Schema(description = "Уникальный идентификатор проекта", example = "1")
    private Long id;
    
    @Schema(description = "Название проекта", example = "Новый технический гаджет")
    private String title;
    
    @Schema(description = "Описание проекта", example = "Инновационный технический гаджет для повседневного использования")
    private String description;

    @Schema(description = "Целевая сумма для сбора", example = "1000.00")
    private BigDecimal goal;
    
    @Schema(description = "Уже собранная сумма", example = "250.00")
    private BigDecimal pledged;

    @Schema(description = "Текущий статус проекта", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Дедлайн проекта", example = "2026-12-31T23:59:59")
    private OffsetDateTime deadline;

    @Schema(description = "ID автора", example = "1")
    private Long authorId;
}