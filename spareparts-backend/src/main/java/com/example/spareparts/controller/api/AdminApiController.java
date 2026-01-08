package com.example.spareparts.controller.api;

import com.example.spareparts.model.firestore.*;
import com.example.spareparts.service.firestore.*;
import com.example.spareparts.repository.firestore.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API endpoints - requires admin authentication.
 */
@RestController
@RequestMapping("/api/v2/admin")
@CrossOrigin(origins = "*")
public class AdminApiController {

    private final ProductFirestoreService productService;
    private final OrderFirestoreService orderService;
    private final UserFirestoreService userService;
    private final AnalyticsFirestoreService analyticsService;
    private final CategoryFirestoreRepository categoryRepository;
    private final BrandFirestoreRepository brandRepository;
    private final SupplierFirestoreRepository supplierRepository;

    public AdminApiController(ProductFirestoreService productService,
            OrderFirestoreService orderService,
            UserFirestoreService userService,
            AnalyticsFirestoreService analyticsService,
            CategoryFirestoreRepository categoryRepository,
            BrandFirestoreRepository brandRepository,
            SupplierFirestoreRepository supplierRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.analyticsService = analyticsService;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
    }

    // ============== DASHBOARD ==============

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/analytics/sales")
    public ResponseEntity<Map<String, Object>> getSalesAnalytics() {
        return ResponseEntity.ok(analyticsService.getSalesAnalytics());
    }

    @GetMapping("/analytics/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryAnalytics() {
        return ResponseEntity.ok(analyticsService.getInventoryAnalytics());
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

    @PostMapping("/products")
    public ResponseEntity<ProductDocument> createProduct(@RequestBody ProductDocument product) {
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDocument> updateProduct(@PathVariable String id,
            @RequestBody ProductDocument product) {
        product.setId(id);
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<ProductDocument>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<Void> updateStock(@PathVariable String id,
            @RequestParam int quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    // ============== ORDERS ==============

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDocument>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDocument> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<OrderDocument>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<List<OrderDocument>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderService.getRecentOrders(limit));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable String id,
            @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/orders/{id}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable String id,
            @RequestParam String paymentStatus) {
        orderService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/orders/{id}/tracking")
    public ResponseEntity<Void> updateTrackingNumber(@PathVariable String id,
            @RequestParam String trackingNumber) {
        orderService.updateTrackingNumber(id, trackingNumber);
        return ResponseEntity.ok().build();
    }

    // ============== CATEGORIES ==============

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDocument>> getAllCategories() {
        try {
            return ResponseEntity.ok(categoryRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDocument> createCategory(@RequestBody CategoryDocument category) {
        try {
            return ResponseEntity.ok(categoryRepository.save(category));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDocument> updateCategory(@PathVariable String id,
            @RequestBody CategoryDocument category) {
        try {
            category.setId(id);
            return ResponseEntity.ok(categoryRepository.save(category));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        try {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== BRANDS ==============

    @GetMapping("/brands")
    public ResponseEntity<List<BrandDocument>> getAllBrands() {
        try {
            return ResponseEntity.ok(brandRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/brands")
    public ResponseEntity<BrandDocument> createBrand(@RequestBody BrandDocument brand) {
        try {
            return ResponseEntity.ok(brandRepository.save(brand));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<BrandDocument> updateBrand(@PathVariable String id,
            @RequestBody BrandDocument brand) {
        try {
            brand.setId(id);
            return ResponseEntity.ok(brandRepository.save(brand));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable String id) {
        try {
            brandRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== SUPPLIERS ==============

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDocument>> getAllSuppliers() {
        try {
            return ResponseEntity.ok(supplierRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDocument> getSupplierById(@PathVariable String id) {
        try {
            return supplierRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/suppliers")
    public ResponseEntity<SupplierDocument> createSupplier(@RequestBody SupplierDocument supplier) {
        try {
            return ResponseEntity.ok(supplierRepository.save(supplier));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDocument> updateSupplier(@PathVariable String id,
            @RequestBody SupplierDocument supplier) {
        try {
            supplier.setId(id);
            return ResponseEntity.ok(supplierRepository.save(supplier));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable String id) {
        try {
            supplierRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== USERS ==============

    @GetMapping("/users")
    public ResponseEntity<List<UserDocument>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDocument> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable String id,
            @RequestParam String role) {
        userService.updateRole(id, role);
        return ResponseEntity.ok().build();
    }
}
