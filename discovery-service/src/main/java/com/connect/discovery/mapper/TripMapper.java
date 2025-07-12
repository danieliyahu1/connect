package com.connect.discovery.mapper;

import com.connect.discovery.dto.IncomingTripRequestDto;
import com.connect.discovery.dto.TripResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    IncomingTripRequestDto toIncomingTripRequest(TripResponseDTO tripResponseDTO);
}
