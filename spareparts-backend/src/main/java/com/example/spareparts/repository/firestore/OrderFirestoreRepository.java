package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.OrderDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Order operations.
 */
@Repository
public class OrderFirestoreRepository {

    private static final String COLLECTION_NAME = "orders";
    private final Firestore firestore;

    public OrderFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<OrderDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(OrderDocument.class))
                .collect(Collectors.toList());
    }

    public List<OrderDocument> findByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(OrderDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<OrderDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(OrderDocument.class));
        }
        return Optional.empty();
    }

    public OrderDocument save(OrderDocument order) throws ExecutionException, InterruptedException {
        if (order.getId() == null || order.getId().isEmpty()) {
            // Create new document
            order.setCreatedAt(System.currentTimeMillis());
            order.setUpdatedAt(System.currentTimeMillis());
            if (order.getStatus() == null) {
                order.setStatus("PENDING");
            }
            if (order.getPaymentStatus() == null) {
                order.setPaymentStatus("PENDING");
            }
            DocumentReference docRef = getCollection().document();
            order.setId(docRef.getId());
            docRef.set(order).get();
        } else {
            // Update existing document
            order.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(order.getId()).set(order).get();
        }
        return order;
    }

    public void updateStatus(String orderId, String status) throws ExecutionException, InterruptedException {
        getCollection().document(orderId).update(
                "status", status,
                "updatedAt", System.currentTimeMillis()).get();
    }

    public void updatePaymentStatus(String orderId, String paymentStatus)
            throws ExecutionException, InterruptedException {
        getCollection().document(orderId).update(
                "paymentStatus", paymentStatus,
                "updatedAt", System.currentTimeMillis()).get();
    }

    public void updateTrackingNumber(String orderId, String trackingNumber)
            throws ExecutionException, InterruptedException {
        getCollection().document(orderId).update(
                "trackingNumber", trackingNumber,
                "status", "SHIPPED",
                "updatedAt", System.currentTimeMillis()).get();
    }

    public List<OrderDocument> findByStatus(String status) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(OrderDocument.class))
                .collect(Collectors.toList());
    }

    public List<OrderDocument> findRecentOrders(int limit) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(OrderDocument.class))
                .collect(Collectors.toList());
    }

    public long count() throws ExecutionException, InterruptedException {
        return getCollection().get().get().size();
    }

    public double getTotalRevenue() throws ExecutionException, InterruptedException {
        List<OrderDocument> orders = findAll();
        return orders.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()) || "PAID".equals(o.getPaymentStatus()))
                .mapToDouble(OrderDocument::getTotal)
                .sum();
    }
}
