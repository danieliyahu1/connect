package com.connect.connector.service;
import com.connect.connector.dto.response.UploadSignatureResponseDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CloudinaryMediaStorageServiceTest {

    private static final String TEST_CLOUDINARY_API_KEY = "apiKey123";
    private static final String TEST_CLOUDINARY_CLOUD_NAME = "cloudName456";
    private static final String TEST_CLOUDINARY_API_SECRET = "secret789";

    private final CloudinaryMediaStorageService  mediaService = new CloudinaryMediaStorageService (
            TEST_CLOUDINARY_API_KEY,
            TEST_CLOUDINARY_CLOUD_NAME,
            TEST_CLOUDINARY_API_SECRET
    );

    @ParameterizedTest
    @CsvSource({
            "image1, folderA",
            "image2, folderB",
            "myImage, myFolder"
    })
    void createCloudinaryUploadSignature_shouldReturnValidSignatureAndFields(String imageName, String folder) {
        UploadSignatureResponseDTO dto = mediaService.generateUploadSignature(imageName, folder);

        assertNotNull(dto);
        assertEquals(TEST_CLOUDINARY_API_KEY, dto.getApiKey());
        assertEquals(TEST_CLOUDINARY_CLOUD_NAME, dto.getCloudName());
        assertEquals(folder, dto.getFolder());
        assertEquals(imageName, dto.getPublicId());

        // timestamp should be a positive integer string
        assertTrue(dto.getTimestamp().matches("\\d+"));

        // signature should not be null or empty
        assertNotNull(dto.getSignature());
        assertFalse(dto.getSignature().isEmpty());
    }
}
