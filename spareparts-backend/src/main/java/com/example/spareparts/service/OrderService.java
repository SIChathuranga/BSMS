package com.example.spareparts.service;

import com.example.spareparts.model.*;
import com.example.spareparts.repository.OrderRepository;
import com.example.spareparts.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> findByUser(User user) { return orderRepository.findByUser(user); }

    @Transactional
    public Order placeOrder(User user, List<OrderItem> items) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product p = productRepository.findById(item.getProduct().getId()).orElseThrow();
            if (p.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product " + p.getId());
            }
            p.setStock(p.getStock() - item.getQuantity());
            productRepository.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(p.getPrice());
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotal(total);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
