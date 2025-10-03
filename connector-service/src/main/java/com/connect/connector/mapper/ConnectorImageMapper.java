package com.akatsuki.connector.mapper;

import com.akatsuki.connector.dto.ConnectorImageDTO;
import com.akatsuki.connector.model.ConnectorImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConnectorImageMapper {
    ConnectorImageDTO toDto(ConnectorImage model);
    List<ConnectorImageDTO> toDtoList(List<ConnectorImage> models);
}
