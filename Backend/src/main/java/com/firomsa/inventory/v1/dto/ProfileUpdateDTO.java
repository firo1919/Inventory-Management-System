package com.firomsa.inventory.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateDTO(@NotBlank String firstName, @NotBlank String lastName,
        @NotBlank String username, String password,
        @NotBlank @Email String email, @NotBlank String phone) {

}
