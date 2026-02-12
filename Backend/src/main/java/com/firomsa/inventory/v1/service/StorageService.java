package com.firomsa.inventory.v1.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.firomsa.inventory.v1.dto.UploadRequestDTO;
import com.firomsa.inventory.v1.dto.UploadResponseDTO;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Template s3Template;
    private final String bucketName = "inventory-management-system";
    private final Integer expiresIn = 2;

    public UploadResponseDTO createUploadPresignTicket(UploadRequestDTO file) {
        log.info("Generating presigned upload URL for file");
        String key = System.currentTimeMillis() + "_" + file.getFilename();
        return new UploadResponseDTO(key, s3Template.createSignedPutURL(bucketName, key,
                Duration.ofDays(expiresIn), null, file.getContentType()).toString(),
                expiresIn.toString());
    }

    public boolean exists(String key) {
        return s3Template.objectExists(bucketName, key);
    }

    public String getUrl(String key) {
        return s3Template.createSignedGetURL(bucketName, key, Duration.ofDays(expiresIn))
                .toString();
    }
}
