package com.firomsa.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "s3")
public class S3Config {
    private String bucketName;
    private int expiryDays;
}
