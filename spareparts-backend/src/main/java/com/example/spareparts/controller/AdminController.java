package com.example.spareparts.controller;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spareparts.dto.ProductRequest;
import com.example.spareparts.model.Category;
import com.example.spareparts.model.Message;
import com.example.spareparts.model.Order;
import com.example.spareparts.model.OrderStatus;
import com.example.spareparts.model.Product;
import com.example.spareparts.repository.CategoryRepository;
import com.example.spareparts.repository.MessageRepository;
import com.example.spareparts.repository.OrderRepository;
import com.example.spareparts.service.ProductService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final MessageRepository messageRepository;
    private final CategoryRepository categoryRepository;

    public AdminController(ProductService productService, OrderRepository orderRepository, 
                          MessageRepository messageRepository, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        this.categoryRepository = categoryRepository;
    }

    // Product CRUD
    @GetMapping("/products")
    public List<Product> allProducts() { return productService.getAll(); }

    @GetMapping("/categories")
    public List<Category> allCategories() { return categoryRepository.findAll(); }

    @PostMapping("/products")
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            // Find the category by name
            Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategory()));
            
            // Create new product
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(category);
            product.setImages(request.getImages() != null ? request.getImages() : List.of());
            
            Product savedProduct = productService.save(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            // Find the category by name
            Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategory()));
            
            // Find existing product
            Product product = productService.getById(id);
            
            // Update product fields
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(category);
            product.setImages(request.getImages() != null ? request.getImages() : List.of());
            
            Product savedProduct = productService.save(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating product: " + e.getMessage());
        }
    }

    @DeleteMapping("/products/{id}")
    public void delete(@PathVariable Long id) { productService.delete(id); }

    @PatchMapping("/products/{id}/images")
    public Product patchImages(@PathVariable Long id, @RequestBody List<String> imageUrls) {
        Product p = productService.getById(id);
        p.setImages(imageUrls);
        return productService.save(p);
    }

    // Orders management
    @GetMapping("/orders")
    public List<Order> orders() { return orderRepository.findAll(); }

    @PatchMapping("/orders/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(status);
        return orderRepository.save(o);
    }

    @GetMapping("/messages")
    public List<Message> messages() { return messageRepository.findAll(); }

    // Simple analytics
    @GetMapping("/analytics/summary")
    public Map<String, Object> summary() {
    List<Order> orders = orderRepository.findAll();
    BigDecimal totalRevenue = orders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

    // monthly revenue (yyyy-MM)
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
    Map<String, BigDecimal> monthlyRevenue = orders.stream().collect(Collectors.groupingBy(
        o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt),
        Collectors.reducing(BigDecimal.ZERO, Order::getTotal, BigDecimal::add)
    ));

    // top products (by quantity)
    Map<String, Integer> topProducts = orders.stream()
        .flatMap(o -> o.getItems().stream())
        .collect(Collectors.groupingBy(
            oi -> oi.getProduct().getName(),
            Collectors.summingInt(oi -> oi.getQuantity())
        ));

    // low stock (<= 5)
    List<Map<String, Object>> lowStock = productService.getAll().stream()
        .filter(p -> p.getStock() <= 5)
        .map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("stock", p.getStock());
            return m;
        }).toList();

    Map<String, Object> resp = new HashMap<>();
    resp.put("totalRevenue", totalRevenue);
    resp.put("totalOrders", orders.size());
    resp.put("monthlyRevenue", monthlyRevenue);
    resp.put("topProducts", topProducts);
    resp.put("lowStock", lowStock);
    return resp;
    }

    // Image upload stub (client can upload direct to Firebase Storage; this is optional server-side upload)
    @PostMapping("/products/{id}/images")
    public Product uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        // For MVP, skip actual upload implementation. Clients should upload to Firebase Storage and PATCH product with URL.
        return productService.getById(id);
    }
}
