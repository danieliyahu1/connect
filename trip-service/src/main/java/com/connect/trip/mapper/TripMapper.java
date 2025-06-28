package com.connect.trip.mapper;

import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.model.Trip;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    TripResponseDTO toDto(Trip model);
}
