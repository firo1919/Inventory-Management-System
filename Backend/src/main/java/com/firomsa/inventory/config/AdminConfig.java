package com.firomsa.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String phone;
}
