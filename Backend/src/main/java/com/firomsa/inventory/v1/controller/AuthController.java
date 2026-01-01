package com.firomsa.inventory.v1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.LogoutRequestDTO;
import com.firomsa.inventory.v1.dto.LogoutResponseDTO;
import com.firomsa.inventory.v1.dto.RefreshTokenRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.ResendOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ResendOtpResponseDTO;
import com.firomsa.inventory.v1.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for performing authentication")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "For registering a user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public RegisterResponseDTO registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        var response = authService.create(registerRequestDTO);
        return response;
    }

    @Operation(summary = "For confirming otp")
    @PostMapping("/confirm-otp")
    @ResponseStatus(HttpStatus.OK)
    public ConfirmOtpResponseDTO confirmOtp(@Valid @RequestBody ConfirmOtpRequestDTO confirmOtpRequestDTO) {
        var response = authService.confirmOtp(confirmOtpRequestDTO);
        return response;
    }

    @Operation(summary = "For resending otp")
    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.OK)
    public ResendOtpResponseDTO resendOtp(@Valid @RequestBody ResendOtpRequestDTO resendOtpRequestDTO) {
        var response = authService.resendOtp(resendOtpRequestDTO);
        return response;
    }

    @Operation(summary = "For logging in")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO loginInUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        var response = authService.login(loginRequestDTO);
        return response;
    }

    @Operation(summary = "For refreshing access token")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        var response = authService.refreshAccessToken(refreshTokenRequestDTO);
        return response;
    }

    @Operation(summary = "For logging out")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public LogoutResponseDTO logout(@Valid @RequestBody LogoutRequestDTO loginRequestDTO) {
        var response = authService.logoutUser(loginRequestDTO);
        return response;
    }
}
