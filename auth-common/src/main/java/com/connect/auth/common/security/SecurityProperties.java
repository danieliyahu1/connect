package com.connect.auth.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private List<String> permitAllRoutes = new ArrayList<>();

    public List<String> getPermitAllRoutes() {
        return permitAllRoutes;
    }

    public void setPermitAllRoutes(List<String> permitAllRoutes) {
        this.permitAllRoutes = permitAllRoutes;
    }
}