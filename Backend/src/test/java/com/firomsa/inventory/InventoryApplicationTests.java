package com.firomsa.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.firomsa.inventory.support.SharedContainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class InventoryApplicationTests {

    @ServiceConnection
    static PostgreSQLContainer postgres = SharedContainers.POSTGRES;

    @Test
    void contextLoads() {
    }
}
