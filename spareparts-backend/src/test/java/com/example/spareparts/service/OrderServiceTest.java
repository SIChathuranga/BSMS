package com.example.spareparts.service;

import com.example.spareparts.model.Order;
import com.example.spareparts.model.OrderItem;
import com.example.spareparts.model.Product;
import com.example.spareparts.model.User;
import com.example.spareparts.repository.OrderRepository;
import com.example.spareparts.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class OrderServiceTest {
    @Test
    void placeOrder_decrementsStock_andSaves() {
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderService orderService = new OrderService(orderRepository, productRepository);

        Product p = new Product();
        p.setId(1L);
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(5);

        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem oi = new OrderItem();
        Product ref = new Product(); ref.setId(1L);
        oi.setProduct(ref); oi.setQuantity(2);

        User u = new User();
        Order o = orderService.placeOrder(u, List.of(oi));
        assertEquals(new BigDecimal("20.00"), o.getTotal());
        assertEquals(3, p.getStock());
    }
}
