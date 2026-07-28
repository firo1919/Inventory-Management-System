package com.firomsa.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.firomsa.inventory.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findBySoldByEmail(String email);

    Page<Sale> findBySoldByEmail(String email, Pageable pageable);

    List<Sale> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}
