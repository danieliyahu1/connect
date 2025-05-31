package com.connect.connector.dto.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountryCityValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCity {
    String message() default "City does not belong to the selected country";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}