package com.connect.connector.service;

import com.connect.connector.dto.response.UploadSignatureResponseDTO;
import com.connect.connector.exception.MediaStorageSignatureGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.connect.connector.constants.ConnectorServiceConstants.*;

@Service
public class CloudinaryMediaStorageService  implements MediaStorageService{

    private final String cloudinaryApiKey;
    private final String cloudinaryCloudName;
    private final String cloudinaryApiSecret;

    public CloudinaryMediaStorageService(
            @Value("${cloudinary.api.key}") String cloudinaryApiKey,
            @Value("${cloudinary.cloud.name}") String cloudinaryCloudName,
            @Value("${cloudinary.api.secret}") String cloudinaryApiSecret
    ) {
        this.cloudinaryApiKey = cloudinaryApiKey;
        this.cloudinaryCloudName = cloudinaryCloudName;
        this.cloudinaryApiSecret = cloudinaryApiSecret;
    }

    public UploadSignatureResponseDTO generateUploadSignature(String imageName, String folder) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        Map<String, String> paramsToSign = generateParamsToSign(imageName, folder, timestamp);

        String signature = generateCloudinarySignature(paramsToSign);

        return new UploadSignatureResponseDTO(
                cloudinaryApiKey,
                cloudinaryCloudName,
                signature,
                timestamp,
                folder,
                imageName
        );
    }

    private Map<String, String> generateParamsToSign(String imageName, String folder, String timestamp) {
        return new TreeMap<>(Map.of(
                KEY_FOLDER, folder,
                KEY_PUBLIC_ID, imageName,
                KEY_TIMESTAMP, timestamp
        ));
    }

    private String generateCloudinarySignature(Map<String, String> params) {
        String paramString = params.entrySet().stream()
                .map(entry -> entry.getKey() + SYMBOL_ASSIGNMENT + entry.getValue())
                .collect(Collectors.joining(SYMBOL_DELIMITER));

        try {
            Mac hmacSha1 = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(cloudinaryApiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            hmacSha1.init(secretKey);

            byte[] hash = hmacSha1.doFinal(paramString.getBytes(StandardCharsets.UTF_8));
            return DigestUtils.md5DigestAsHex(hash);
        } catch (IllegalArgumentException | NullPointerException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new MediaStorageSignatureGenerationException("Error generating Cloudinary signature");
        }
    }
}
