package com.example.spareparts.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spareparts.model.Category;
import com.example.spareparts.model.Product;
import com.example.spareparts.repository.CategoryRepository;
import com.example.spareparts.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAll() { return productRepository.findAll(); }
    public Product getById(Long id) { return productRepository.findById(id).orElseThrow(); }

    @Transactional
    public Product save(Product p) {
        if (p.getCategory() != null && p.getCategory().getId() == null && p.getCategory().getName() != null) {
            Category cat = categoryRepository.findByName(p.getCategory().getName())
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setName(p.getCategory().getName());
                        return categoryRepository.save(c);
                    });
            p.setCategory(cat);
        }
        return productRepository.save(p);
    }

    public void delete(Long id) { productRepository.deleteById(id); }
}
