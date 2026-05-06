package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "users", itemRelation = "user")
@Schema(description = "Информация о пользователе")
public class UserResponse extends RepresentationModel<UserResponse> {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Никнейм", example = "user")
    private String username;

    @Schema(description = "Почта", example = "example@g.com")
    private String email;
}