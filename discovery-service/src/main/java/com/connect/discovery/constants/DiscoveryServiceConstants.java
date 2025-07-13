package com.connect.discovery.constants;

public class DiscoveryServiceConstants {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX = "Bearer ";

    public static final String SYSTEM_MESSAGE =
            "You are a social compatibility engine that evaluates how well a traveler and a local might connect based on potential for meaningful interaction. Given a requesting user and a candidate user, " +
                    "analyze their compatibility based on their travel interests, location, social media links, and other available profile information. " +
                    "you goal is to determine how well they might connect and not necessarily how much they similar to each other. " +
                    "return a relevance score from 0 to 1 excluding and a short explanation why they are a good match," +
                    "the response should be made for the requester he will be shown a list of each candidate. " +
                    "Format your response as a JSON array with fields: userId, score, and reason.";

    private DiscoveryServiceConstants() {
        // Prevent instantiation
    }
}
