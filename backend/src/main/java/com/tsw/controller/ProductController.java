package com.tsw.controller;

import com.tsw.model.Product;
import com.tsw.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET all products
    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // GET product by id
    @GetMapping("/{id}")
    public Product getById(@PathVariable UUID id) {
        return productRepository.findById(id).orElse(null);
    }
}
