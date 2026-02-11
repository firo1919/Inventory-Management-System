package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public UserResponseDTO updateProfile(String username, ProfileUpdateDTO profileUpdateDTO) {
        var user = userRepository
            .findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstName(profileUpdateDTO.firstName());
        user.setLastName(profileUpdateDTO.lastName());
        user.setUsername(profileUpdateDTO.username());
        user.setEmail(profileUpdateDTO.email());
        user.setPhone(profileUpdateDTO.phone());
        user.setPassword(passwordEncoder.encode(profileUpdateDTO.password()));
        userRepository.save(user);
        var response = userMapper.toDTO(user);
        if (user.getImageKey() != null) {
            response.setProfilePictureUrl(storageService.getUrl(user.getImageKey()));
        }
        return response;
    }

    public UserResponseDTO getProfile(String username) {
        var user = userRepository
            .findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        var response = userMapper.toDTO(user);
        if (user.getImageKey() != null) {
            response.setProfilePictureUrl(storageService.getUrl(user.getImageKey()));
        }
        return response;
    }
    
    public UserResponseDTO addProfilePicture(String username, String objectKey
    ) {
        var user = userRepository
            .findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (storageService.exists(objectKey)) {
            user.setImageKey(objectKey);
            User saved = userRepository.save(user);
            var response = userMapper.toDTO(saved);
            response.setProfilePictureUrl(storageService.getUrl(objectKey));
            return response;
        } 
        else {
            throw new ResourceNotFoundException("Image not found in storage: " + objectKey);
        }
        
    }
}
