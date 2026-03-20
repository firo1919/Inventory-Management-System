package com.firomsa.inventory.v1.controller.integrationTest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.firomsa.inventory.v1.service.EmailService;
import com.firomsa.inventory.v1.service.StorageService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Definite Integration Test
@AutoConfigureMockMvc
@Testcontainers
@SuppressWarnings("resource")
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine");

    @MockitoBean
    protected StorageService storageService;

    @MockitoBean
    protected EmailService emailService;

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.jdbc-url", postgres::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
