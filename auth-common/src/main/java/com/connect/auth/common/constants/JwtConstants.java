package com.connect.auth.common.constants;

public class JwtConstants {

    public static final int ACCESS_TOKEN_LIFE_SPAN_IN_MINUTES = 3;
    public static final int REFRESH_TOKEN_LIFE_SPAN_IN_MINUTES = 15;

    public static final int ACCESS_TOKEN_LIFE_SPAN = 1000 * 60 * ACCESS_TOKEN_LIFE_SPAN_IN_MINUTES;
    public static final int REFRESH_TOKEN_LIFE_SPAN = 1000 * 60 * REFRESH_TOKEN_LIFE_SPAN_IN_MINUTES;

    }
