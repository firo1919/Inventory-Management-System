package com.firomsa.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "cache.ttl")
@Validated
public class CacheProperties {

    @Positive
    private long products = 5;

    @Positive
    private long lowStock = 5;

    @Positive
    private long inventoryValue = 10;

    @Positive
    private long categories = 30;
}
