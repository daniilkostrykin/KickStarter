package org.example.kickstarterapicontract.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class ProjectStatusValidator implements ConstraintValidator<ValidProjectStatus, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        for (ProjectStatus status : ProjectStatus.values()){
            if (status.name().equals(value)){
                return true;
            }
        }
        return false;
    }
}