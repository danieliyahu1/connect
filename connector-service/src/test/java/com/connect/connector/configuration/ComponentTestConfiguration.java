package com.connect.connector.configuration;

import com.connect.connector.mapper.ConnectorImageMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("component-test")
public class ComponentTestConfiguration {

    @Bean
    public String accessToken()
    {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhNTg2NGMwYS1jNDVkLTQyOWEtOWMzOS1kMzI2ZjNjMGFjOTkiLCJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzUwMDg0OTYzLCJleHAiOjE3NTAwODUwMjN9.j-lqdhd8VPtCB2s4wr-gstUhHQ7pyla3CGHsPtwbEo8";
    }

    @Bean
    public ConnectorImageMapper connectorImageMapper() {
        // This tells Spring to get the MapStruct generated implementation
        return Mappers.getMapper(ConnectorImageMapper.class);
    }


}
