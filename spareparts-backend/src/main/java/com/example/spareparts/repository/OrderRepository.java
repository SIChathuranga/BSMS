package com.example.spareparts.repository;

import com.example.spareparts.model.Order;
import com.example.spareparts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
