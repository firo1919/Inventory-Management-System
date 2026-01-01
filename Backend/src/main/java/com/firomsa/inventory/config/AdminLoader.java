package com.firomsa.inventory.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(2)
@Slf4j
public class AdminLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfig adminProperties;

    public AdminLoader(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, AdminConfig adminProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Attempting to register admin");
        if (userRepository.findByEmail(adminProperties.getEmail()).isEmpty()
                && userRepository.findByUsername(adminProperties.getUsername()).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            Role role = roleRepository.findByName(Roles.ADMIN)
                    .orElseThrow(() -> (new RuntimeException("Role doesnt exist")));
            User admin = User.builder()
                    .username(adminProperties.getUsername())
                    .email(adminProperties.getEmail())
                    .phone(adminProperties.getPhone())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .firstName(adminProperties.getFirstName())
                    .lastName(adminProperties.getLastName())
                    .active(true)
                    .role(role)
                    .createdAt(now)
                    .build();
            userRepository.save(admin);
            log.info("Successfully registered admin");
        } else {
            log.info("Admin already registered");
        }
    }

}
