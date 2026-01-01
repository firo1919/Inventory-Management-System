package com.firomsa.inventory.v1.dto;

import java.util.UUID;

public record UserResponseDTO(
                UUID id,
                String firstName,
                String lastName,
                String username,
                String email,
                String phone,
                String role,
                String createdAt,
                boolean active) {

}
