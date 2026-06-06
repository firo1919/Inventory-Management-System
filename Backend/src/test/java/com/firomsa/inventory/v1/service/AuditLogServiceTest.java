package com.firomsa.inventory.v1.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;

import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditLog;
import com.firomsa.inventory.model.AuditStatus;
import com.firomsa.inventory.repository.AuditLogRepository;
import com.firomsa.inventory.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLogAuditWhenEnabled() {
        when(environment.getProperty("audit.logging.enabled", "true")).thenReturn("true");

        auditLogService.logAudit(AuditAction.CREATE, "Product", UUID.randomUUID(),
                null, null, AuditStatus.SUCCESS, null);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogAuditWhenDisabled() {
        when(environment.getProperty("audit.logging.enabled", "true")).thenReturn("false");

        auditLogService.logAudit(AuditAction.CREATE, "Product", UUID.randomUUID(),
                null, null, AuditStatus.SUCCESS, null);

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void testLogAuditWithException() {
        when(environment.getProperty("audit.logging.enabled", "true")).thenReturn("true");

        auditLogService.logAudit(AuditAction.CREATE, "Product", UUID.randomUUID(),
                "oldValue", "newValue", AuditStatus.SUCCESS, null);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testDeleteAuditLogsByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();

        auditLogService.deleteAuditLogsByDateRange(startDate, endDate);

        verify(auditLogRepository).findByTimestampBefore(eq(endDate));
    }
}
