package com.firomsa.inventory.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogoutRequestDTO(@NotNull UUID refreshToken, @NotBlank @Email String email) {

}
