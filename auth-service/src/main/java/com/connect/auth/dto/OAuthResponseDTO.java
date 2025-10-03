package com.akatsuki.auth.dto;

import lombok.Getter;

@Getter
public class OAuthResponseDTO extends AuthResponseDTO {
    private final boolean isNewUser;

    public OAuthResponseDTO(String accessToken, String refreshToken, boolean isNewUser) {
        super(accessToken, refreshToken);
        this.isNewUser = isNewUser;
    }
}
