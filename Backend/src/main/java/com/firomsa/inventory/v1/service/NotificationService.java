package com.firomsa.inventory.v1.service;

import org.springframework.stereotype.Service;

import com.firomsa.inventory.config.AdminConfig;
import com.firomsa.inventory.model.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final AdminConfig adminConfig;

    public void sendLowStockAlert(Product product) {
        try {
            String adminEmail = adminConfig.getEmail();
            log.info("Sending low stock alert for product: {} to admin: {}", product.getName(), adminEmail);
            emailService.sendLowStockAlert(adminEmail, product);
        } catch (Exception e) {
            log.error("Failed to send low stock notification email for product: {}", product.getId(), e);
        }
    }

    public void sendLowStockAlertIfNeeded(Product product) {
        if (product.getQuantity() < product.getLowStockThreshold()) {
            sendLowStockAlert(product);
        }
    }
}
