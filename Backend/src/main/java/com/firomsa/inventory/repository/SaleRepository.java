package com.firomsa.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.firomsa.inventory.model.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findBySoldByEmail(String email);

    List<Sale> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}
