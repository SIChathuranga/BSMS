package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.SupplierDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Supplier operations.
 */
@Repository
public class SupplierFirestoreRepository {

    private static final String COLLECTION_NAME = "suppliers";
    private final Firestore firestore;

    public SupplierFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<SupplierDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection().get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(SupplierDocument.class))
                .collect(Collectors.toList());
    }

    public List<SupplierDocument> findAllActive() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(SupplierDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<SupplierDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(SupplierDocument.class));
        }
        return Optional.empty();
    }

    public SupplierDocument save(SupplierDocument supplier) throws ExecutionException, InterruptedException {
        if (supplier.getId() == null || supplier.getId().isEmpty()) {
            // Create new document
            supplier.setCreatedAt(System.currentTimeMillis());
            supplier.setUpdatedAt(System.currentTimeMillis());
            supplier.setActive(true);
            DocumentReference docRef = getCollection().document();
            supplier.setId(docRef.getId());
            docRef.set(supplier).get();
        } else {
            // Update existing document
            supplier.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(supplier.getId()).set(supplier).get();
        }
        return supplier;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false, "updatedAt", System.currentTimeMillis()).get();
    }

    public List<SupplierDocument> findByCategory(String category) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereArrayContains("productCategories", category)
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(SupplierDocument.class))
                .collect(Collectors.toList());
    }

    public long count() throws ExecutionException, InterruptedException {
        return getCollection().get().get().size();
    }
}
