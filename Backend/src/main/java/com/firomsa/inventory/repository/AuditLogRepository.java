package com.firomsa.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditLog;
import com.firomsa.inventory.model.AuditStatus;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByCorrelationId(UUID correlationId, Pageable pageable);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);

    Page<AuditLog> findByStatus(AuditStatus status, Pageable pageable);

    List<AuditLog> findByTimestampBefore(LocalDateTime timestamp);

    void deleteByTimestampBefore(LocalDateTime timestamp);

    long countByStatus(AuditStatus status);

    long countByAction(AuditAction action);

    @Query("SELECT a.resourceType, COUNT(a) FROM AuditLog a GROUP BY a.resourceType")
    List<Object[]> countByResourceTypeGrouped();
}
