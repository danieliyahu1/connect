package com.connect.connector.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadSignatureResponseDTO {
    private String apiKey;
    private String cloudName;
    private String signature;
    private String timestamp;
    private String folder;
    private String publicId;
}
