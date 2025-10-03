package com.akatsuki.auth.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class JwtConstants {

    public static final int ACCESS_TOKEN_LIFE_SPAN_IN_MINUTES = 10080;
    public static final int REFRESH_TOKEN_LIFE_SPAN_IN_MINUTES = 10080;

    public static final int ACCESS_TOKEN_LIFE_SPAN = 1000 * 60 * ACCESS_TOKEN_LIFE_SPAN_IN_MINUTES;
    public static final int REFRESH_TOKEN_LIFE_SPAN = 1000 * 60 * REFRESH_TOKEN_LIFE_SPAN_IN_MINUTES;

    }
