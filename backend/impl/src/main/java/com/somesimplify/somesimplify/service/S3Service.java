package com.somesimplify.somesimplify.service;

import com.somesimplify.somesimplify.config.AwsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsConfig awsConfig;

    /**
     * Upload a file to S3 and return the S3 key
     */
    public String uploadFile(MultipartFile file, String userId) throws IOException {
        String s3Key = generateS3Key(userId, file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsConfig.getS3Bucket())
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        log.info("Uploaded file to S3: {}", s3Key);
        return s3Key;
    }

    /**
     * Generate a pre-signed URL for accessing an image (valid for 1 hour)
     */
    public String generatePresignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsConfig.getS3Bucket())
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Delete a file from S3
     */
    public void deleteFile(String s3Key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(awsConfig.getS3Bucket())
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Deleted file from S3: {}", s3Key);
    }

    /**
     * Generate unique S3 key: userId/uuid-originalFilename
     */
    private String generateS3Key(String userId, String originalFilename) {
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("%s/%s-%s", userId, UUID.randomUUID(), sanitizedFilename);
    }
}
