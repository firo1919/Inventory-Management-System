package com.firomsa.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration for bootstrap token used to secure initial admin registration.
 * 
 * Maps to the APP_BOOTSTRAP_TOKEN environment variable.
 * Example: APP_BOOTSTRAP_TOKEN=your-secure-token maps to app.bootstrap.token property.
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapConfig {
    private String token;
}
