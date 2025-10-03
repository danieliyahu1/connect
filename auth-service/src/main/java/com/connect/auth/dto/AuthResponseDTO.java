package com.akatsuki.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AuthResponseDTO {
    private final String accessToken;
    private final String refreshToken;
}
