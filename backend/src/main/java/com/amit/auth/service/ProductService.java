package com.amit.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.amit.auth.entity.Product;
import com.amit.auth.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Product createProduct(Product product) {
		return productRepository.save(product);
	}

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	
	public Page<Product> getAllProducts(
	        int page,
	        int size,
	        String sortBy,
	        String direction,
	        String search) {

	    if (page < 0) {
	        throw new IllegalArgumentException(
	                "Page number cannot be negative");
	    }

	    if (size < 1 || size > 100) {
	        throw new IllegalArgumentException(
	                "Page size must be between 1 and 100");
	    }

	    if (!java.util.Set.of("id", "name", "price", "quantity")
	            .contains(sortBy)) {
	        throw new IllegalArgumentException(
	                "Invalid sort field");
	    }

	    Sort sort = "desc".equalsIgnoreCase(direction)
	            ? Sort.by(sortBy).descending()
	            : Sort.by(sortBy).ascending();

	    Pageable pageable = PageRequest.of(page, size, sort);

	    if (search == null || search.isBlank()) {
	        return productRepository.findAll(pageable);
	    }

	    return productRepository.findByNameContainingIgnoreCase(
	            search.trim(), pageable);
	}

	public Product getProductById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
	}

	public Product updateProduct(Long id, Product product) {

		Product existing = getProductById(id);

		existing.setName(product.getName());
		existing.setPrice(product.getPrice());
		existing.setQuantity(product.getQuantity());

		return productRepository.save(existing);
	}

	public void deleteProduct(Long id) {

		Product existing = getProductById(id);

		productRepository.delete(existing);
	}
}