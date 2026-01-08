package com.example.spareparts.service.firestore;

import com.example.spareparts.model.firestore.ProductDocument;
import com.example.spareparts.repository.firestore.ProductFirestoreRepository;
import com.example.spareparts.repository.firestore.ReviewFirestoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service for Product operations using Firestore.
 */
@Service
public class ProductFirestoreService {

    private final ProductFirestoreRepository productRepository;
    private final ReviewFirestoreRepository reviewRepository;

    public ProductFirestoreService(ProductFirestoreRepository productRepository,
            ReviewFirestoreRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ProductDocument> getAllProducts() {
        try {
            return productRepository.findAllActive();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching products", e);
        }
    }

    public Optional<ProductDocument> getProductById(String id) {
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching product", e);
        }
    }

    public ProductDocument saveProduct(ProductDocument product) {
        try {
            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    public void deleteProduct(String id) {
        try {
            productRepository.softDelete(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    public List<ProductDocument> getProductsByCategory(String category) {
        try {
            return productRepository.findByCategory(category);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching products by category", e);
        }
    }

    public List<ProductDocument> getProductsByBrand(String brand) {
        try {
            return productRepository.findByBrand(brand);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching products by brand", e);
        }
    }

    public List<ProductDocument> getLowStockProducts(int threshold) {
        try {
            return productRepository.findLowStock(threshold);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching low stock products", e);
        }
    }

    public List<ProductDocument> searchProducts(String query) {
        try {
            return productRepository.searchProducts(query);
        } catch (Exception e) {
            throw new RuntimeException("Error searching products", e);
        }
    }

    public void updateStock(String productId, int quantity) {
        try {
            productRepository.updateStock(productId, quantity);
        } catch (Exception e) {
            throw new RuntimeException("Error updating stock", e);
        }
    }

    public ProductDocument getProductWithRating(String id) {
        try {
            Optional<ProductDocument> product = productRepository.findById(id);
            if (product.isPresent()) {
                ProductDocument p = product.get();
                p.setAverageRating(reviewRepository.getAverageRating(id));
                p.setReviewCount(reviewRepository.getReviewCount(id));
                return p;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching product with rating", e);
        }
    }

    public long getProductCount() {
        try {
            return productRepository.count();
        } catch (Exception e) {
            throw new RuntimeException("Error counting products", e);
        }
    }
}
