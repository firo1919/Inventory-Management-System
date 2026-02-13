package com.firomsa.inventory.v1.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDTO> getEmployees() {
        var users = userRepository.findAll();
        var response = new ArrayList<UserResponseDTO>();
        for (var user : users) {
            var userResponse = userMapper.toDTO(user);
            if (user.getImageKey() != null) {
                userResponse.setProfilePictureUrl(storageService.getUrl(user.getImageKey()));
            }
            response.add(userResponse);
        }

        return response;
    }

    public UserResponseDTO getEmployeeById(UUID id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(id.toString() + " User not found"));
        var userResponse = userMapper.toDTO(user);
        if (user.getImageKey() != null) {
            userResponse.setProfilePictureUrl(storageService.getUrl(user.getImageKey()));
        }
        return userResponse;
    }

    public void deactivateEmployee(UUID id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(id.toString() + " User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateEmployee(UUID id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(id.toString() + " User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    public void deleteEmployee(UUID id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(id.toString() + " User not found"));
        userRepository.delete(user);
    }

    public UserResponseDTO updateEmployee(UUID id, UserUpdateRequestDTO userUpdateRequestDTO) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(id.toString() + " User not found"));
        Role role = roleRepository.findByName(userUpdateRequestDTO.role()).orElseThrow(
                () -> new ResourceNotFoundException("Role: " + userUpdateRequestDTO.role().name()));
        userMapper.updateModelFromDTO(user, userUpdateRequestDTO);
        user.setPassword(passwordEncoder.encode(userUpdateRequestDTO.password()));
        user.setRole(role);
        userRepository.save(user);
        var updatedUserResponse = userMapper.toDTO(user);
        if (user.getImageKey() != null) {
            updatedUserResponse.setProfilePictureUrl(storageService.getUrl(user.getImageKey()));
        }
        return updatedUserResponse;
    }
}
