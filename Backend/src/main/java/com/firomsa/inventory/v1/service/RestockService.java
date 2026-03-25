package com.firomsa.inventory.v1.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.RestockRequestDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.mapper.RestockMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestockService {
    private final RestockRepository restockRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RestockMapper restockMapper;

    @Transactional
    public RestockResponseDTO createRestock(RestockRequestDTO restockRequestDTO, String email) {
        var product = productRepository.findById(restockRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + restockRequestDTO.getProductId()));
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        var restock = restockMapper.toModel(restockRequestDTO);
        restock.setProduct(product);
        restock.setRestockedBy(user);

        product.setQuantity(product.getQuantity() + restockRequestDTO.getQuantity());
        productRepository.save(product);

        var savedRestock = restockRepository.save(restock);
        var response = restockMapper.toDTO(savedRestock);
        response.setMessage("Restock recorded successfully");
        return response;
    }

    @Transactional(readOnly = true)
    public List<RestockResponseDTO> getAllRestocks() {
        return restockRepository.findAll().stream().map(restockMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RestockResponseDTO getRestockById(UUID restockId) {
        var restock = restockRepository.findById(restockId)
                .orElseThrow(() -> new ResourceNotFoundException("Restock not found with id: " + restockId));
        return restockMapper.toDTO(restock);
    }

    @Transactional(readOnly = true)
    public List<RestockResponseDTO> getRestocksByEmployee(String email) {
        return restockRepository.findByRestockedByEmail(email).stream().map(restockMapper::toDTO)
                .toList();
    }

    @Transactional
    public void deleteRestockById(UUID restockId) {
        var restock = restockRepository.findById(restockId)
                .orElseThrow(() -> new ResourceNotFoundException("Restock not found with id: " + restockId));
        restockRepository.delete(restock);
    }
}
