package com.example.spareparts.controller;

import com.example.spareparts.model.Product;
import com.example.spareparts.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final ProductService productService;

    public PublicController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> products() {
        return productService.getAll();
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/health")
    public String health() { return "OK"; }
}
