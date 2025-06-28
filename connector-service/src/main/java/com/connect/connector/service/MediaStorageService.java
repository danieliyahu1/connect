package com.connect.connector.service;

import com.connect.connector.dto.response.UploadSignatureResponseDTO;

public interface MediaStorageService {
    public UploadSignatureResponseDTO generateUploadSignature(String imageName, String folder);
}