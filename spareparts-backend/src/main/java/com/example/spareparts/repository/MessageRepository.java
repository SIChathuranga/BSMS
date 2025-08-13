package com.example.spareparts.repository;

import com.example.spareparts.model.Message;
import com.example.spareparts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByUser(User user);
}
