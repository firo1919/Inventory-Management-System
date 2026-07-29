package com.firomsa.inventory.aspect;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.firomsa.inventory.model.AuditAction;
import com.firomsa.inventory.model.AuditStatus;
import com.firomsa.inventory.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String resourceType = extractResourceType(method);
        AuditAction action = extractAction(method);

        if (action == null || resourceType == null) {
            return joinPoint.proceed();
        }

        UUID resourceId = extractResourceId(joinPoint.getArgs());
        Object oldValue = null;
        Object newValue = null;

        // Synchronously capture user details and HTTP request context before proceeding or spawning @Async thread
        String username = extractUsername();
        String correlationId = MDC.get("correlationId");
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIpAddress(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        try {
            Object result = joinPoint.proceed();

            auditLogService.logAuditWithContext(action, resourceType, resourceId, oldValue, newValue,
                    AuditStatus.SUCCESS, null, username, correlationId, ipAddress, userAgent);

            return result;
        } catch (Exception e) {
            auditLogService.logAuditWithContext(action, resourceType, resourceId, oldValue, newValue,
                    AuditStatus.FAILURE, e.getMessage(), username, correlationId, ipAddress, userAgent);
            throw e;
        }
    }

    private String extractUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

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

    private String extractResourceType(Method method) {
        RequestMapping classMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            String path = classMapping.value()[0];
            return extractResourceFromPath(path);
        }
        return null;
    }

    private AuditAction extractAction(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {
            return AuditAction.CREATE;
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            return AuditAction.READ;
        } else if (method.isAnnotationPresent(PutMapping.class) || method.isAnnotationPresent(PatchMapping.class)) {
            return AuditAction.UPDATE;
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return AuditAction.DELETE;
        }
        return null;
    }

    private String extractResourceFromPath(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty() && !parts[i].startsWith("{") && !parts[i].equals("api") && !parts[i].equals("v1")) {
                return parts[i];
            }
        }
        return "UNKNOWN";
    }

    private UUID extractResourceId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
            if (arg instanceof String) {
                try {
                    return UUID.fromString((String) arg);
                } catch (IllegalArgumentException e) {
                    // Not a UUID, continue
                }
            }
        }
        return null;
    }
}
