package com.connect.discovery.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionDTO {
    @NonNull
    private String userId;
    @NonNull
    private String name;
    private String profilePictureUrl;
    private String city;
    @NonNull
    private String country;
    @NonNull
    private String reason;
    @NonNull
    private double score;
}
