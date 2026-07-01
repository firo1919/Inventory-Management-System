package com.firomsa.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.firomsa.inventory.support.SharedContainers;

@SpringBootTest
@Testcontainers
class InventoryApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = SharedContainers.POSTGRES;

    @Test
    void contextLoads() {
    }
}
