package com.amit.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.amit.auth.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}