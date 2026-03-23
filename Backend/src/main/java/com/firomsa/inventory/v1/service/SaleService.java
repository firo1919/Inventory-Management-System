package com.firomsa.inventory.v1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.SaleRequestDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.mapper.SaleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SaleMapper saleMapper;

    @Transactional
    public SaleResponseDTO createSale(SaleRequestDTO saleRequestDTO, String email) {
        var product = productRepository.findById(saleRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + saleRequestDTO.getProductId()));
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        if (product.getQuantity() < saleRequestDTO.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        var sale = saleMapper.toModel(saleRequestDTO);
        sale.setProduct(product);
        sale.setSoldBy(user);
        product.setQuantity(product.getQuantity() - saleRequestDTO.getQuantity());
        productRepository.save(product);
        var savedSale = saleRepository.save(sale);
        var response = saleMapper.toDTO(savedSale);
        response.setMessage("Sale recorded successfully");
        return response;
    }

}
