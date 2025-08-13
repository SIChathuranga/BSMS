package com.example.spareparts.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.spareparts.model.User;
import com.example.spareparts.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User ensureUser(String firebaseUid, String email, String displayName) {
        return userRepository.findByFirebaseUid(firebaseUid).orElseGet(() -> {
            User u = new User();
            u.setFirebaseUid(firebaseUid);
            u.setEmail(email != null ? email : "unknown@example.com");
            u.setDisplayName(displayName);
            u.setAdmin(false);
            u.setCreatedAt(Instant.now());
            return userRepository.save(u);
        });
    }
}
