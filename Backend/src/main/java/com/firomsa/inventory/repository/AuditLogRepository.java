package com.firomsa.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditLog;
import com.firomsa.inventory.model.AuditStatus;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByCorrelationId(UUID correlationId, Pageable pageable);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);

    Page<AuditLog> findByStatus(AuditStatus status, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:correlationId IS NULL OR a.correlationId = :correlationId) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:username IS NULL OR a.username = :username) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> findByFilters(
            @Param("correlationId") UUID correlationId,
            @Param("userId") UUID userId,
            @Param("username") String username,
            @Param("action") AuditAction action,
            @Param("resourceType") String resourceType,
            @Param("status") AuditStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<AuditLog> findByTimestampBefore(LocalDateTime timestamp);

    void deleteByTimestampBefore(LocalDateTime timestamp);

    long countByStatus(AuditStatus status);

    long countByAction(AuditAction action);

    @Query("SELECT a.resourceType, COUNT(a) FROM AuditLog a GROUP BY a.resourceType")
    List<Map<String, Long>> countByResourceTypeGrouped();
}
