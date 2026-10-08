package com.amit.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amit.auth.dto.ApiResponse;
import com.amit.auth.entity.Product;
import com.amit.auth.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	public ApiResponse<Product> createProduct(@RequestBody Product product) {

		Product savedProduct = productService.createProduct(product);

		return new ApiResponse<>(true, "Product created successfully", savedProduct);
	}

	@GetMapping
	public ApiResponse<List<Product>> getAllProducts() {

		return new ApiResponse<>(true, "Products fetched successfully", productService.getAllProducts());
	}

	@GetMapping("/{id}")
	public ApiResponse<Product> getProduct(@PathVariable Long id) {

		Product product = productService.getProductById(id);

		return new ApiResponse<>(true, "Product retreived successfully", product);
	}

	@PutMapping("/{id}")
	public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
		Product updateProduct = productService.updateProduct(id, product);
		return new ApiResponse<>(true, "Product updated successfully", updateProduct);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<String> deleteProduct(@PathVariable Long id) {

		productService.deleteProduct(id);

		return new ApiResponse<>(true, "Product deleted successfully", null);
	}
}