package com.firomsa.inventory.scheduled;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.firomsa.inventory.model.AuditLog;
import com.firomsa.inventory.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogCleanupJob {

    private final AuditLogRepository auditLogRepository;

    @Value("${audit.logging.retention-days:90}")
    private int retentionDays;

    @Scheduled(cron = "${audit.logging.cleanup.cron:0 0 2 * * ?}") // Default: 2 AM daily
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        log.info("Starting audit log cleanup for logs older than {} days (before {})",
                retentionDays, cutoffDate);

        try {
            List<AuditLog> logsToDelete = auditLogRepository.findByTimestampBefore(cutoffDate);
            int deletedCount = logsToDelete.size();

            auditLogRepository.deleteAll(logsToDelete);

            log.info("Audit log cleanup completed. Deleted {} logs older than {}",
                    deletedCount, cutoffDate);
        } catch (Exception e) {
            log.error("Failed to cleanup audit logs: {}", e.getMessage(), e);
        }
    }
}
