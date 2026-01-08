package com.example.spareparts.controller.api;

import com.example.spareparts.model.firestore.ProductDocument;
import com.example.spareparts.model.firestore.CategoryDocument;
import com.example.spareparts.model.firestore.BrandDocument;
import com.example.spareparts.model.firestore.ReviewDocument;
import com.example.spareparts.service.firestore.ProductFirestoreService;
import com.example.spareparts.service.firestore.ReviewFirestoreService;
import com.example.spareparts.repository.firestore.CategoryFirestoreRepository;
import com.example.spareparts.repository.firestore.BrandFirestoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API endpoints accessible without authentication.
 */
@RestController
@RequestMapping("/api/v2/public")
@CrossOrigin(origins = "*")
public class PublicApiController {

    private final ProductFirestoreService productService;
    private final ReviewFirestoreService reviewService;
    private final CategoryFirestoreRepository categoryRepository;
    private final BrandFirestoreRepository brandRepository;

    public PublicApiController(ProductFirestoreService productService,
            ReviewFirestoreService reviewService,
            CategoryFirestoreRepository categoryRepository,
            BrandFirestoreRepository brandRepository) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    // ============== PRODUCTS ==============

    @GetMapping("/products")
    public ResponseEntity<List<ProductDocument>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDocument> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/{id}/with-rating")
    public ResponseEntity<ProductDocument> getProductWithRating(@PathVariable String id) {
        ProductDocument product = productService.getProductWithRating(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<ProductDocument>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/products/brand/{brand}")
    public ResponseEntity<List<ProductDocument>> getProductsByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand));
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDocument>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    // ============== REVIEWS ==============

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewDocument>> getProductReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // ============== CATEGORIES ==============

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDocument>> getAllCategories() {
        try {
            return ResponseEntity.ok(categoryRepository.findAllActive());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDocument> getCategoryById(@PathVariable String id) {
        try {
            return categoryRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories/{id}/subcategories")
    public ResponseEntity<List<CategoryDocument>> getSubcategories(@PathVariable String id) {
        try {
            return ResponseEntity.ok(categoryRepository.findSubcategories(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== BRANDS (Motorcycle Brands) ==============

    @GetMapping("/brands")
    public ResponseEntity<List<BrandDocument>> getAllBrands() {
        try {
            return ResponseEntity.ok(brandRepository.findAllActive());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/brands/{id}")
    public ResponseEntity<BrandDocument> getBrandById(@PathVariable String id) {
        try {
            return brandRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== STATS ==============

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        try {
            return ResponseEntity.ok(Map.of(
                    "productCount", productService.getProductCount(),
                    "categoryCount", categoryRepository.findAllActive().size(),
                    "brandCount", brandRepository.findAllActive().size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
