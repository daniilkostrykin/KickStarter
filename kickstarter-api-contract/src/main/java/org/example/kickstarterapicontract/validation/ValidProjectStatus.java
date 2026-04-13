package org.example.kickstarterapicontract.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProjectStatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProjectStatus {

    String message() default "Статус может быть только: DRAFT, ACTIVE, SUCCESSFUL, FAILED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}