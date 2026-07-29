package com.firomsa.inventory.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditLog;
import com.firomsa.inventory.model.AuditStatus;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.AuditLogRepository;
import com.firomsa.inventory.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Environment environment;

    @Async
    public void logAudit(AuditAction action, String resourceType, UUID resourceId,
            Object oldValue, Object newValue, AuditStatus status, String errorMessage) {
        logAuditWithContext(action, resourceType, resourceId, oldValue, newValue, status, errorMessage,
                null, null, null, null);
    }

    @Async
    public void logAuditWithContext(AuditAction action, String resourceType, UUID resourceId,
            Object oldValue, Object newValue, AuditStatus status, String errorMessage,
            String username, String correlationId, String ipAddress, String userAgent) {

        if (!isAuditLoggingEnabled()) {
            return;
        }

        try {
            AuditLog auditLog = buildAuditLog(action, resourceType, resourceId,
                    oldValue, newValue, status, errorMessage, username, correlationId, ipAddress, userAgent);

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved successfully for action: {} on resource: {}",
                    action, resourceType);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    private AuditLog buildAuditLog(AuditAction action, String resourceType, UUID resourceId,
            Object oldValue, Object newValue, AuditStatus status, String errorMessage,
            String providedUsername, String providedCorrelationId, String providedIpAddress, String providedUserAgent) {

        String correlationId = providedCorrelationId != null ? providedCorrelationId : MDC.get("correlationId");
        UUID correlationIdUuid = correlationId != null ? UUID.fromString(correlationId) : UUID.randomUUID();

        String username = providedUsername;
        if (username == null) {
            username = resolveUsernameFromSecurityContext();
        }

        UUID userId = null;
        if (username != null) {
            final String targetUsername = username;
            userId = userRepository.findByEmail(targetUsername)
                    .or(() -> userRepository.findByUsername(targetUsername))
                    .map(User::getId)
                    .orElse(null);
        }

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = providedIpAddress != null ? providedIpAddress
                : (request != null ? getClientIpAddress(request) : null);
        String userAgent = providedUserAgent != null ? providedUserAgent
                : (request != null ? request.getHeader("User-Agent") : null);

        return AuditLog.builder()
                .correlationId(correlationIdUuid)
                .userId(userId)
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .oldValue(serializeToJson(oldValue))
                .newValue(serializeToJson(newValue))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorMessage(errorMessage)
                .build();
    }

    private String resolveUsernameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.isBlank()) {
                    return email;
                }
                return jwt.getSubject();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                return ud.getUsername();
            } else if (principal instanceof String s) {
                return s;
            }
            return authentication.getName();
        }
        return null;
    }

    private String serializeToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return object.toString();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAuditLoggingEnabled() {
        return Boolean.parseBoolean(
                environment.getProperty("audit.logging.enabled", "true"));
    }

    @Transactional
    public void deleteAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        int deletedCount = auditLogRepository.deleteByTimestampBetween(startDate, endDate);
        log.info("Deleted {} audit logs between {} and {}", deletedCount, startDate, endDate);
    }
}
