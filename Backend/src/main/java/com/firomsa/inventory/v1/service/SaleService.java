package com.firomsa.inventory.v1.service;

import java.util.List;
import java.util.UUID;

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
    private final NotificationService notificationService;

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

        // Check for low stock and send notification if needed
        notificationService.sendLowStockAlertIfNeeded(product);

        var savedSale = saleRepository.save(sale);
        var response = saleMapper.toDTO(savedSale);
        response.setMessage("Sale recorded successfully");
        return response;
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAll().stream().map(saleMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));
        return saleMapper.toDTO(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByEmployee(String email) {
        return saleRepository.findBySoldByEmail(email).stream().map(saleMapper::toDTO).toList();
    }

    @Transactional
    public void deleteSaleById(UUID saleId) {
        var sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));
        saleRepository.delete(sale);
    }

}
