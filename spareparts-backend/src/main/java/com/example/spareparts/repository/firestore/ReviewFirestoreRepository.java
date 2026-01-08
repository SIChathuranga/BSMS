package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.ReviewDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Review operations.
 */
@Repository
public class ReviewFirestoreRepository {

    private static final String COLLECTION_NAME = "reviews";
    private final Firestore firestore;

    public ReviewFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<ReviewDocument> findByProductId(String productId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("productId", productId)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ReviewDocument.class))
                .collect(Collectors.toList());
    }

    public List<ReviewDocument> findByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ReviewDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<ReviewDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(ReviewDocument.class));
        }
        return Optional.empty();
    }

    public ReviewDocument save(ReviewDocument review) throws ExecutionException, InterruptedException {
        if (review.getId() == null || review.getId().isEmpty()) {
            // Create new document
            review.setCreatedAt(System.currentTimeMillis());
            review.setUpdatedAt(System.currentTimeMillis());
            review.setActive(true);
            review.setHelpfulCount(0);
            DocumentReference docRef = getCollection().document();
            review.setId(docRef.getId());
            docRef.set(review).get();
        } else {
            // Update existing document
            review.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(review.getId()).set(review).get();
        }
        return review;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false, "updatedAt", System.currentTimeMillis()).get();
    }

    public void incrementHelpful(String reviewId) throws ExecutionException, InterruptedException {
        getCollection().document(reviewId).update("helpfulCount", FieldValue.increment(1)).get();
    }

    public double getAverageRating(String productId) throws ExecutionException, InterruptedException {
        List<ReviewDocument> reviews = findByProductId(productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(ReviewDocument::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewCount(String productId) throws ExecutionException, InterruptedException {
        return findByProductId(productId).size();
    }

    public boolean hasUserReviewed(String productId, String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get();
        return !future.get().getDocuments().isEmpty();
    }
}
