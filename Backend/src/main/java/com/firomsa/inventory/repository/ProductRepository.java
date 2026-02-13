package com.firomsa.inventory.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

}
