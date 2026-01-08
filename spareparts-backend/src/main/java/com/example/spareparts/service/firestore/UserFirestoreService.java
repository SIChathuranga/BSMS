package com.example.spareparts.service.firestore;

import com.example.spareparts.model.firestore.UserDocument;
import com.example.spareparts.model.firestore.ProductDocument;
import com.example.spareparts.repository.firestore.UserFirestoreRepository;
import com.example.spareparts.repository.firestore.ProductFirestoreRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for User operations using Firestore.
 */
@Service
public class UserFirestoreService {

    private final UserFirestoreRepository userRepository;
    private final ProductFirestoreRepository productRepository;

    public UserFirestoreService(UserFirestoreRepository userRepository,
            ProductFirestoreRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public Optional<UserDocument> getUserById(String id) {
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user", e);
        }
    }

    public Optional<UserDocument> getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user by email", e);
        }
    }

    public UserDocument saveUser(UserDocument user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public UserDocument createOrUpdateUser(String firebaseUid, String email, String displayName, String photoUrl) {
        try {
            Optional<UserDocument> existingUser = userRepository.findById(firebaseUid);
            UserDocument user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setEmail(email);
                user.setDisplayName(displayName);
                user.setPhotoUrl(photoUrl);
            } else {
                user = new UserDocument();
                user.setId(firebaseUid);
                user.setEmail(email);
                user.setDisplayName(displayName);
                user.setPhotoUrl(photoUrl);
                user.setRole("CUSTOMER");
                user.setActive(true);
                user.setWishlist(new ArrayList<>());
                user.setAddresses(new ArrayList<>());
            }

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error creating/updating user", e);
        }
    }

    public void updateRole(String userId, String role) {
        try {
            userRepository.updateRole(userId, role);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user role", e);
        }
    }

    // Wishlist operations
    public void addToWishlist(String userId, String productId) {
        try {
            userRepository.addToWishlist(userId, productId);
        } catch (Exception e) {
            throw new RuntimeException("Error adding to wishlist", e);
        }
    }

    public void removeFromWishlist(String userId, String productId) {
        try {
            userRepository.removeFromWishlist(userId, productId);
        } catch (Exception e) {
            throw new RuntimeException("Error removing from wishlist", e);
        }
    }

    public List<ProductDocument> getWishlistProducts(String userId) {
        try {
            List<String> wishlistIds = userRepository.getWishlist(userId);
            List<ProductDocument> products = new ArrayList<>();
            for (String productId : wishlistIds) {
                Optional<ProductDocument> product = productRepository.findById(productId);
                product.ifPresent(products::add);
            }
            return products;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching wishlist", e);
        }
    }

    public boolean isInWishlist(String userId, String productId) {
        try {
            List<String> wishlist = userRepository.getWishlist(userId);
            return wishlist.contains(productId);
        } catch (Exception e) {
            throw new RuntimeException("Error checking wishlist", e);
        }
    }

    // Address operations
    public void addAddress(String userId, UserDocument.AddressDocument address) {
        try {
            userRepository.addAddress(userId, address);
        } catch (Exception e) {
            throw new RuntimeException("Error adding address", e);
        }
    }

    public List<UserDocument> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching users", e);
        }
    }

    public List<UserDocument> getAdmins() {
        try {
            return userRepository.findAdmins();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching admins", e);
        }
    }

    public long getUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            throw new RuntimeException("Error counting users", e);
        }
    }
}
