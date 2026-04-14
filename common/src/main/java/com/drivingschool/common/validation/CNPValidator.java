package com.drivingschool.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPValidator implements ConstraintValidator<CNP, String> {

    @Override
    public boolean isValid(String cnp, ConstraintValidatorContext context) {
        if (cnp == null || cnp.isEmpty()) {
            return false;
        }
        // Romanian CNP validation: 13 digits
        return cnp.matches("^[0-9]{13}$");
    }
}

