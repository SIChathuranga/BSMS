package com.example.spareparts.service.firestore;

import com.example.spareparts.model.firestore.*;
import com.example.spareparts.repository.firestore.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for Analytics and Dashboard data.
 */
@Service
public class AnalyticsFirestoreService {

    private final ProductFirestoreRepository productRepository;
    private final OrderFirestoreRepository orderRepository;
    private final UserFirestoreRepository userRepository;
    private final SupplierFirestoreRepository supplierRepository;

    public AnalyticsFirestoreService(ProductFirestoreRepository productRepository,
            OrderFirestoreRepository orderRepository,
            UserFirestoreRepository userRepository,
            SupplierFirestoreRepository supplierRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
    }

    public Map<String, Object> getDashboardSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();

            // Product stats
            List<ProductDocument> products = productRepository.findAllActive();
            summary.put("totalProducts", products.size());
            summary.put("lowStockProducts", productRepository.findLowStock(5).size());
            summary.put("outOfStockProducts", products.stream().filter(p -> p.getStock() <= 0).count());

            // Order stats
            List<OrderDocument> orders = orderRepository.findAll();
            summary.put("totalOrders", orders.size());
            summary.put("pendingOrders", orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
            summary.put("processingOrders", orders.stream().filter(o -> "PROCESSING".equals(o.getStatus())).count());
            summary.put("completedOrders", orders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());

            // Revenue
            double totalRevenue = orders.stream()
                    .filter(o -> "DELIVERED".equals(o.getStatus()) || "PAID".equals(o.getPaymentStatus()))
                    .mapToDouble(OrderDocument::getTotal)
                    .sum();
            summary.put("totalRevenue", totalRevenue);

            // User stats
            summary.put("totalCustomers", userRepository.count());

            // Supplier stats
            summary.put("totalSuppliers", supplierRepository.count());

            // Recent orders
            summary.put("recentOrders", orderRepository.findRecentOrders(5));

            // Low stock alerts
            summary.put("lowStockAlerts", productRepository.findLowStock(10));

            return summary;
        } catch (Exception e) {
            throw new RuntimeException("Error generating dashboard summary", e);
        }
    }

    public Map<String, Object> getSalesAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            List<OrderDocument> orders = orderRepository.findAll();

            // Total sales
            double totalSales = orders.stream()
                    .filter(o -> !"CANCELLED".equals(o.getStatus()))
                    .mapToDouble(OrderDocument::getTotal)
                    .sum();
            analytics.put("totalSales", totalSales);

            // Order count by status
            Map<String, Long> ordersByStatus = new HashMap<>();
            ordersByStatus.put("pending", orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
            ordersByStatus.put("confirmed", orders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count());
            ordersByStatus.put("processing", orders.stream().filter(o -> "PROCESSING".equals(o.getStatus())).count());
            ordersByStatus.put("shipped", orders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count());
            ordersByStatus.put("delivered", orders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());
            ordersByStatus.put("cancelled", orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count());
            analytics.put("ordersByStatus", ordersByStatus);

            // Average order value
            double avgOrderValue = orders.isEmpty() ? 0 : totalSales / orders.size();
            analytics.put("averageOrderValue", avgOrderValue);

            return analytics;
        } catch (Exception e) {
            throw new RuntimeException("Error generating sales analytics", e);
        }
    }

    public Map<String, Object> getInventoryAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            List<ProductDocument> products = productRepository.findAllActive();

            // Total inventory value
            double inventoryValue = products.stream()
                    .mapToDouble(p -> p.getPrice() * p.getStock())
                    .sum();
            analytics.put("inventoryValue", inventoryValue);

            // Products by category
            Map<String, Long> productsByCategory = new HashMap<>();
            for (ProductDocument p : products) {
                String category = p.getCategory() != null ? p.getCategory() : "Uncategorized";
                productsByCategory.merge(category, 1L, Long::sum);
            }
            analytics.put("productsByCategory", productsByCategory);

            // Stock status distribution
            Map<String, Long> stockStatus = new HashMap<>();
            stockStatus.put("inStock", products.stream().filter(p -> p.getStock() > 10).count());
            stockStatus.put("lowStock", products.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 10).count());
            stockStatus.put("outOfStock", products.stream().filter(p -> p.getStock() <= 0).count());
            analytics.put("stockStatus", stockStatus);

            return analytics;
        } catch (Exception e) {
            throw new RuntimeException("Error generating inventory analytics", e);
        }
    }
}
