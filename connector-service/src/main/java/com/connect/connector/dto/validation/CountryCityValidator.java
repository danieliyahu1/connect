package com.connect.connector.dto.validation;


import com.connect.connector.dto.ConnectorRequestDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CountryCityValidator implements ConstraintValidator<ValidCountryCity, ConnectorRequestDTO> {
    @Override
    public boolean isValid(ConnectorRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.getCountry() == null || dto.getCity() == null) return true;
        try {
            Country country = Country.valueOf(dto.getCountry().toUpperCase());
            City city = City.valueOf(dto.getCity().toUpperCase());
            return city.getCountry() == country;
        } catch (IllegalArgumentException e) {
            // Invalid enum value
            return false;
        }
    }
}