package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.UserDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for User operations.
 */
@Repository
public class UserFirestoreRepository {

    private static final String COLLECTION_NAME = "users";
    private final Firestore firestore;

    public UserFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<UserDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection().get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(UserDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<UserDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(UserDocument.class));
        }
        return Optional.empty();
    }

    public Optional<UserDocument> findByEmail(String email) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("email", email)
                .limit(1)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return Optional.of(documents.get(0).toObject(UserDocument.class));
        }
        return Optional.empty();
    }

    public UserDocument save(UserDocument user) throws ExecutionException, InterruptedException {
        if (user.getId() == null || user.getId().isEmpty()) {
            // This shouldn't happen as user ID should be Firebase UID
            throw new IllegalArgumentException("User ID (Firebase UID) is required");
        }

        // Check if user exists
        Optional<UserDocument> existingUser = findById(user.getId());
        if (existingUser.isEmpty()) {
            // Create new user
            user.setCreatedAt(System.currentTimeMillis());
            user.setLastLoginAt(System.currentTimeMillis());
            user.setActive(true);
            if (user.getRole() == null) {
                user.setRole("CUSTOMER");
            }
        } else {
            // Update last login
            user.setLastLoginAt(System.currentTimeMillis());
            user.setCreatedAt(existingUser.get().getCreatedAt());
        }

        getCollection().document(user.getId()).set(user).get();
        return user;
    }

    public void updateRole(String userId, String role) throws ExecutionException, InterruptedException {
        getCollection().document(userId).update("role", role).get();
    }

    public void addToWishlist(String userId, String productId) throws ExecutionException, InterruptedException {
        getCollection().document(userId).update("wishlist", FieldValue.arrayUnion(productId)).get();
    }

    public void removeFromWishlist(String userId, String productId) throws ExecutionException, InterruptedException {
        getCollection().document(userId).update("wishlist", FieldValue.arrayRemove(productId)).get();
    }

    public List<String> getWishlist(String userId) throws ExecutionException, InterruptedException {
        Optional<UserDocument> user = findById(userId);
        if (user.isPresent() && user.get().getWishlist() != null) {
            return user.get().getWishlist();
        }
        return new ArrayList<>();
    }

    public void addAddress(String userId, UserDocument.AddressDocument address)
            throws ExecutionException, InterruptedException {
        if (address.getId() == null) {
            address.setId(UUID.randomUUID().toString());
        }
        getCollection().document(userId).update("addresses", FieldValue.arrayUnion(address)).get();
    }

    public List<UserDocument> findAdmins() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("role", "ADMIN")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(UserDocument.class))
                .collect(Collectors.toList());
    }

    public long count() throws ExecutionException, InterruptedException {
        return getCollection().get().get().size();
    }
}
