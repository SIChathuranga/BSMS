package com.example.spareparts.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spareparts.model.Message;
import com.example.spareparts.model.User;
import com.example.spareparts.repository.MessageRepository;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message create(User user, String content) {
        Message m = new Message();
        m.setUser(user);
        m.setContent(content);
        m.setCreatedAt(Instant.now());
        return messageRepository.save(m);
    }

    public List<Message> byUser(User user) { return messageRepository.findByUser(user); }
}
