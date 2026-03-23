package com.firomsa.inventory.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.firomsa.inventory.model.Restock;

@Repository
public interface RestockRepository extends JpaRepository<Restock, UUID> {

}
