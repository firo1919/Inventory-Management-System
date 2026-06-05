package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.firomsa.inventory.repository.AuditLogRepository;

class AuditLogControllerE2ETest extends AbstractE2ETest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testAuditLogFlow() {
        String suffix = randomSuffix();
        String adminEmail = adminEmailForSuffix(suffix);

        // Register admin
        var registerResponse = client.post().uri(AUTH_BASE_URL + "/register/admin")
                .contentType(MediaType.APPLICATION_JSON).body(registerAdminPayload(suffix)).exchange();
        registerResponse.expectStatus().isOk();

        // Confirm OTP
        String otp = latestOtpForEmail(adminEmail);
        String confirmPayload = "{\"email\":\"" + adminEmail + "\",\"otp\":\"" + otp + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(MediaType.APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        // Login
        String accessToken = loginByEmail(adminEmail);

        // Create a product to trigger audit log
        String productPayload = """
                {
                    "name": "Test Product %s",
                    "description": "Test Description",
                    "price": 100.0,
                    "quantity": 50,
                    "categoryId": 1
                }
                """.formatted(suffix);

        var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                .header("Authorization", authorizationHeader(accessToken))
                .contentType(MediaType.APPLICATION_JSON).body(productPayload).exchange();
        createProductResponse.expectStatus().isOk();

        // Verify audit log was created
        long auditLogCount = auditLogRepository.count();
        assertThat(auditLogCount).isGreaterThan(0);

        // Get audit logs
        var auditLogsResponse = client.get().uri(ADMIN_BASE_URL + "/audit-logs")
                .header("Authorization", authorizationHeader(accessToken)).exchange();
        auditLogsResponse.expectStatus().isOk();

        String auditLogsBody = auditLogsResponse.returnResult(String.class).getResponseBody();
        assertThat(auditLogsBody).contains("content");
    }

    @Test
    void testAuditLogStatistics() {
        String suffix = randomSuffix();
        String adminEmail = adminEmailForSuffix(suffix);

        // Register and login
        var registerResponse = client.post().uri(AUTH_BASE_URL + "/register/admin")
                .contentType(MediaType.APPLICATION_JSON).body(registerAdminPayload(suffix)).exchange();
        registerResponse.expectStatus().isOk();

        String otp = latestOtpForEmail(adminEmail);
        String confirmPayload = "{\"email\":\"" + adminEmail + "\",\"otp\":\"" + otp + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(MediaType.APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        String accessToken = loginByEmail(adminEmail);

        // Get statistics
        var statsResponse = client.get().uri(ADMIN_BASE_URL + "/audit-logs/statistics")
                .header("Authorization", authorizationHeader(accessToken)).exchange();
        statsResponse.expectStatus().isOk();

        String statsBody = statsResponse.returnResult(String.class).getResponseBody();
        assertThat(statsBody).contains("totalLogs");
        assertThat(statsBody).contains("successCount");
        assertThat(statsBody).contains("failureCount");
    }

    @Test
    void testAuditLogExport() {
        String suffix = randomSuffix();
        String adminEmail = adminEmailForSuffix(suffix);

        // Register and login
        var registerResponse = client.post().uri(AUTH_BASE_URL + "/register/admin")
                .contentType(MediaType.APPLICATION_JSON).body(registerAdminPayload(suffix)).exchange();
        registerResponse.expectStatus().isOk();

        String otp = latestOtpForEmail(adminEmail);
        String confirmPayload = "{\"email\":\"" + adminEmail + "\",\"otp\":\"" + otp + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(MediaType.APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        String accessToken = loginByEmail(adminEmail);

        // Export to CSV
        String exportFilter = """
                {
                    "page": 0,
                    "size": 20,
                    "sort": "timestamp,desc"
                }
                """;

        var exportResponse = client.post().uri(ADMIN_BASE_URL + "/audit-logs/export/csv")
                .header("Authorization", authorizationHeader(accessToken))
                .contentType(MediaType.APPLICATION_JSON).body(exportFilter).exchange();
        exportResponse.expectStatus().isOk();
    }

    @Test
    void testAuditLogAccessDeniedForNonAdmin() {
        String suffix = randomSuffix();
        String employeeEmail = employeeEmailForSuffix(suffix);

        // Register employee
        var registerResponse = client.post().uri(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON).body(registerEmployeePayload(suffix)).exchange();
        registerResponse.expectStatus().isOk();

        String otp = latestOtpForEmail(employeeEmail);
        String confirmPayload = "{\"email\":\"" + employeeEmail + "\",\"otp\":\"" + otp + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(MediaType.APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        String accessToken = loginByEmail(employeeEmail);

        // Try to access audit logs (should be denied)
        var auditLogsResponse = client.get().uri(ADMIN_BASE_URL + "/audit-logs")
                .header("Authorization", authorizationHeader(accessToken)).exchange();
        auditLogsResponse.expectStatus().isForbidden();
    }
}
