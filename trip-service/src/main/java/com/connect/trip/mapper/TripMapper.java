package com.connect.trip.mapper;

import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
import com.connect.trip.model.Trip;
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
