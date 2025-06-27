package com.connect.connector.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloudinaryUploadSignatureResponseDTO {
    private String apiKey;
    private String cloudName;
    private String signature;
    private String timestamp;
    private String folder;
    private String publicId;
}
