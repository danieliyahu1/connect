package com.akatsuki.trip.mapper;

import com.akatsuki.trip.dto.response.TripResponseDTO;
import com.akatsuki.trip.enums.City;
import com.akatsuki.trip.enums.Country;
import com.akatsuki.trip.model.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "country", expression = "java(mapCountry(model.getCountry()))")
    @Mapping(target = "city", expression = "java(mapCity(model.getCity()))")
    TripResponseDTO toDto(Trip model);

    default String mapCountry(Country country) {
        return country != null ? country.getDisplayValue() : null;
    }

    default String mapCity(City city) {
        return city != null ? city.getDisplayValue() : null;
    }
}
