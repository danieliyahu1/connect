package com.connect.auth.validator;

import com.connect.auth.dto.RegisterRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequestDTO> {

    @Override
    public boolean isValid(RegisterRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getConfirmedPassword() == null) {
            return false; // or return true if you want to handle missing values separately
        }
        return dto.getPassword().equals(dto.getConfirmedPassword());
    }
}
