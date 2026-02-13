package com.firomsa.inventory.v1.dto;

import com.firomsa.inventory.model.Roles;

public record LoginResponseDTO(Roles role, String accessToken, String refreshToken, String username,
        String email) {

}
