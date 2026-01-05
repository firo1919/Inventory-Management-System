package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

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
        return userMapper.toDTO(user);
    }

    public UserResponseDTO getProfile(String username) {
        var user = userRepository
            .findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDTO(user);
    }
}
