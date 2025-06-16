package com.connect.connector.mapper;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.model.ConnectorImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConnectorImageMapper {
    ConnectorImageDTO toDto(ConnectorImage model);
    List<ConnectorImageDTO> toDtoList(List<ConnectorImage> models);
}
