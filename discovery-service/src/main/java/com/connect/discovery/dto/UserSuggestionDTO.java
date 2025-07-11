package com.connect.discovery.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionDTO {
    private String userId;
    private String name;
    private int age;
    private String profilePictureUrl;
    private String city;
    private String country;
    private String reason;
}
