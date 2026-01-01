package com.firomsa.inventory.v1.service;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.firomsa.inventory.config.AdminConfig;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    private JavaMailSender javaMailSender;
    private AdminConfig adminConfig;

    public EmailService(JavaMailSender javaMailSender, AdminConfig adminConfig) {
        this.javaMailSender = javaMailSender;
        this.adminConfig = adminConfig;
    }

    public void sendOtp(String otp, String email) throws MailException {
        log.info("Sending confirmation otp to: {}", email);
        var mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setFrom(adminConfig.getEmail());
        mail.setText(otp);
        javaMailSender.send(mail);
    }
}
