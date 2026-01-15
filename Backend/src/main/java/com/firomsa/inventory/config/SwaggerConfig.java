package com.firomsa.inventory.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("Maaed API v1")
                                .description("Version 1 of the maaed API")
                                .version("v1")))
                .build();
    }

    @Bean
    public OpenAPI mainOpenApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Maaed API")
                .description("All available API versions")
                .version("Current"));
    }
}
