package com.example.spareparts.service.firestore;

import com.example.spareparts.model.firestore.OrderDocument;
import com.example.spareparts.model.firestore.ProductDocument;
import com.example.spareparts.repository.firestore.OrderFirestoreRepository;
import com.example.spareparts.repository.firestore.ProductFirestoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for Order operations using Firestore.
 */
@Service
public class OrderFirestoreService {

    private final OrderFirestoreRepository orderRepository;
    private final ProductFirestoreRepository productRepository;

    public OrderFirestoreService(OrderFirestoreRepository orderRepository,
            ProductFirestoreRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderDocument> getAllOrders() {
        try {
            return orderRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching orders", e);
        }
    }

    public List<OrderDocument> getOrdersByUser(String userId) {
        try {
            return orderRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user orders", e);
        }
    }

    public Optional<OrderDocument> getOrderById(String id) {
        try {
            return orderRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching order", e);
        }
    }

    public OrderDocument createOrder(OrderDocument order) {
        try {
            // Validate and reduce stock for each item
            for (OrderDocument.OrderItemDocument item : order.getItems()) {
                Optional<ProductDocument> product = productRepository.findById(item.getProductId());
                if (product.isEmpty()) {
                    throw new RuntimeException("Product not found: " + item.getProductId());
                }
                if (product.get().getStock() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for: " + item.getProductName());
                }
                // Reduce stock
                productRepository.updateStock(item.getProductId(), -item.getQuantity());
            }

            // Calculate totals
            double subtotal = order.getItems().stream()
                    .mapToDouble(OrderDocument.OrderItemDocument::getTotalPrice)
                    .sum();
            order.setSubtotal(subtotal);
            order.setTotal(subtotal + order.getTax() + order.getShippingCost());

            // Set estimated delivery (7 days from now)
            order.setEstimatedDelivery(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

            return orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }

    public void updateOrderStatus(String orderId, String status) {
        try {
            orderRepository.updateStatus(orderId, status);
        } catch (Exception e) {
            throw new RuntimeException("Error updating order status", e);
        }
    }

    public void updatePaymentStatus(String orderId, String paymentStatus) {
        try {
            orderRepository.updatePaymentStatus(orderId, paymentStatus);
        } catch (Exception e) {
            throw new RuntimeException("Error updating payment status", e);
        }
    }

    public void updateTrackingNumber(String orderId, String trackingNumber) {
        try {
            orderRepository.updateTrackingNumber(orderId, trackingNumber);
        } catch (Exception e) {
            throw new RuntimeException("Error updating tracking number", e);
        }
    }

    public void cancelOrder(String orderId) {
        try {
            Optional<OrderDocument> order = orderRepository.findById(orderId);
            if (order.isPresent()) {
                // Restore stock for each item
                for (OrderDocument.OrderItemDocument item : order.get().getItems()) {
                    productRepository.updateStock(item.getProductId(), item.getQuantity());
                }
                orderRepository.updateStatus(orderId, "CANCELLED");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cancelling order", e);
        }
    }

    public List<OrderDocument> getOrdersByStatus(String status) {
        try {
            return orderRepository.findByStatus(status);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching orders by status", e);
        }
    }

    public List<OrderDocument> getRecentOrders(int limit) {
        try {
            return orderRepository.findRecentOrders(limit);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching recent orders", e);
        }
    }

    public long getOrderCount() {
        try {
            return orderRepository.count();
        } catch (Exception e) {
            throw new RuntimeException("Error counting orders", e);
        }
    }

    public double getTotalRevenue() {
        try {
            return orderRepository.getTotalRevenue();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating revenue", e);
        }
    }
}
