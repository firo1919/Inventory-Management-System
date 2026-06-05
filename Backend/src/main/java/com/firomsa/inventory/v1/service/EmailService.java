package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.config.AdminConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AdminConfig adminConfig;

    public void sendOtp(String otp, String email) throws MailException {
        log.info("Sending confirmation otp to: {}", email);
        var mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setFrom(adminConfig.getEmail());
        mail.setText(otp);
        javaMailSender.send(mail);
    }

    public void sendLowStockAlert(String adminEmail, com.firomsa.inventory.model.Product product) throws MailException {
        log.info("Sending low stock alert for product: {} to: {}", product.getName(), adminEmail);
        var mail = new SimpleMailMessage();
        mail.setTo(adminEmail);
        mail.setFrom(adminConfig.getEmail());
        mail.setSubject("Low Stock Alert: " + product.getName());
        mail.setText(String.format(
                "Product: %s\nSKU: %s\nCurrent Quantity: %d\nLow Stock Threshold: %d\nSuggested Reorder: %d",
                product.getName(),
                product.getSku(),
                product.getQuantity(),
                product.getLowStockThreshold(),
                product.getLowStockThreshold() * 2));
        javaMailSender.send(mail);
    }

    public void sendDailyReport(String adminEmail, String report) throws MailException {
        log.info("Sending daily report to: {}", adminEmail);
        var mail = new SimpleMailMessage();
        mail.setTo(adminEmail);
        mail.setFrom(adminConfig.getEmail());
        mail.setSubject("Daily Inventory Report - " + java.time.LocalDate.now());
        mail.setText(report);
        javaMailSender.send(mail);
    }
}
