package com.akatsuki.discovery.mapper;

import com.akatsuki.discovery.dto.IncomingTripRequestDto;
import com.akatsuki.discovery.dto.TripResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    IncomingTripRequestDto toIncomingTripRequest(TripResponseDTO tripResponseDTO);
}
