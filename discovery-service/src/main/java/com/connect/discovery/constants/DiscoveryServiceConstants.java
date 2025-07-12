package com.connect.discovery.constants;

public class DiscoveryServiceConstants {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX = "Bearer ";

    public static final String SYSTEM_MESSAGE =
            "You are a social matching expert. Given a requesting user and a list of candidate users, " +
                    "analyze their compatibility based on their travel interests, location, social media links, and other available profile information. " +
                    "For each candidate, return a relevance score from 1 to 100 and a short explanation why they are (or aren't) a good match for the requester. " +
                    "Format your response as a JSON array with fields: userId, score, and reason.";

    private DiscoveryServiceConstants() {
        // Prevent instantiation
    }
}
