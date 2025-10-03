package com.akatsuki.connector.service;

import com.akatsuki.connector.dto.response.UploadSignatureResponseDTO;

public interface MediaStorageService {
    public UploadSignatureResponseDTO generateUploadSignature(String imageName, String folder);
}