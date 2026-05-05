package org.example.kickstarterapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о вознаграждениях")
@Relation(collectionRelation = "rewards", itemRelation = "reward")
public class RewardResponse extends RepresentationModel<RewardResponse> {

    @Schema(description = "Уникальный идентификатор награды", example = "1")
    private Long id;
    
    @Schema(description = "Название награды", example = "Стандартная награда")
    private String title;
    
    @Schema(description = "Описание награды", example = "Копия продукта")
    private String description;
    
    @Schema(description = "Минимальная цена для получения награды", example = "50.00")
    private BigDecimal minPrice;
    
    @Schema(description = "ID проекта, к которому относится награда", example = "1")
    private Long projectId;

}