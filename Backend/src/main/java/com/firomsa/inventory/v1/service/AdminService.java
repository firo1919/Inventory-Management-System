package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.UserMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private RoleRepository roleRepository;

    public AdminService(
        UserRepository userRepository,
        UserMapper userMapper,
        RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
    }

    public List<UserResponseDTO> getEmployees() {
        var users = userRepository.findAll();
        var userResponses = users.stream().map(userMapper::toDTO).toList();
        return userResponses;
    }

    public UserResponseDTO getEmployeeById(UUID id) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id.toString() + " User not found"));
        var userResponse = userMapper.toDTO(user);
        return userResponse;
    }

    public void deactivateEmployee(UUID id) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id.toString() + " User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateEmployee(UUID id) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id.toString() + " User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    public void deleteEmployee(UUID id) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id.toString() + " User not found"));
        userRepository.delete(user);
    }

    public UserResponseDTO updateEmployee(UUID id, UserUpdateRequestDTO userUpdateRequestDTO) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id.toString() + " User not found"));
        Role role = roleRepository
            .findByName(userUpdateRequestDTO.role())
            .orElseThrow(() ->
                new ResourceNotFoundException("Role: " + userUpdateRequestDTO.role().name())
            );
        user.setFirstName(userUpdateRequestDTO.firstName());
        user.setLastName(userUpdateRequestDTO.lastName());
        user.setEmail(userUpdateRequestDTO.email());
        user.setPhone(userUpdateRequestDTO.phone());
        user.setUsername(userUpdateRequestDTO.username());
        user.setPassword(userUpdateRequestDTO.password());
        user.setRole(role);
        userRepository.save(user);
        var updatedUserResponse = userMapper.toDTO(user);
        return updatedUserResponse;
    }
}
