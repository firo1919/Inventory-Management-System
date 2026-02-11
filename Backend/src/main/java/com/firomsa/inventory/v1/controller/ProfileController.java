package com.firomsa.inventory.v1.controller;

import com.firomsa.inventory.v1.dto.FileDTO;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "API for user profile operations")
@Slf4j
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @Operation(summary = "For getting user profile")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getProfile(userDetails.getUsername());
    }

    @Operation(summary = "For updating user profile")
    @PutMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO updateProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ProfileUpdateDTO profileUpdateDTO
    ) {
        return userService.updateProfile(userDetails.getUsername(), profileUpdateDTO);
    }
    
    @Operation(summary = "For adding/updating user profile picture")
    @PostMapping("/profile-picture")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO addProductImage(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody FileDTO profileImageDTO
    ) {
        var response = userService.addProfilePicture(userDetails.getUsername(), profileImageDTO.getObjectKey());
        return response;
    }
}
