package com.example.spareparts.controller;

import com.example.spareparts.dto.OrderItemRequest;
import com.example.spareparts.model.Message;
import com.example.spareparts.model.Order;
import com.example.spareparts.model.OrderItem;
import com.example.spareparts.model.Product;
import com.example.spareparts.model.User;
import com.example.spareparts.service.MessageService;
import com.example.spareparts.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final OrderService orderService;
    private final MessageService messageService;

    public UserController(OrderService orderService, MessageService messageService) {
        this.orderService = orderService;
        this.messageService = messageService;
    }

    @GetMapping("/orders")
    public List<Order> myOrders(@AuthenticationPrincipal User user) {
        return orderService.findByUser(user);
    }

    @GetMapping("/me")
    public User me(@AuthenticationPrincipal User user) { return user; }

    @PostMapping("/orders")
    public Order placeOrder(@AuthenticationPrincipal User user, @Valid @RequestBody List<OrderItemRequest> items) {
        List<OrderItem> mapped = items.stream().map(r -> {
            OrderItem oi = new OrderItem();
            Product p = new Product();
            p.setId(r.getProductId());
            oi.setProduct(p);
            oi.setQuantity(r.getQuantity());
            return oi;
        }).collect(Collectors.toList());
        return orderService.placeOrder(user, mapped);
    }

    @PostMapping("/messages")
    public Message sendMessage(@AuthenticationPrincipal User user, @RequestBody String content) {
        return messageService.create(user, content);
    }

    @GetMapping("/messages")
    public List<Message> myMessages(@AuthenticationPrincipal User user) {
        return messageService.byUser(user);
    }
}
