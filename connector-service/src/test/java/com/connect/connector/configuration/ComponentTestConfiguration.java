package com.connect.connector.configuration;

import com.connect.connector.mapper.ConnectorImageMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("component-test")
public class ComponentTestConfiguration {


}
