package com.firomsa.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditStatus;

public record AuditLogResponseDTO(
        Long id,
        UUID correlationId,
        UUID userId,
        String username,
        AuditAction action,
        String resourceType,
        UUID resourceId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent,
        LocalDateTime timestamp,
        AuditStatus status,
        String errorMessage
) {}
