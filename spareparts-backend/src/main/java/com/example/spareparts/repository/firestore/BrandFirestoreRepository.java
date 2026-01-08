package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.BrandDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Brand operations (motorcycle brands).
 */
@Repository
public class BrandFirestoreRepository {

    private static final String COLLECTION_NAME = "brands";
    private final Firestore firestore;

    public BrandFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<BrandDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .orderBy("name")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(BrandDocument.class))
                .collect(Collectors.toList());
    }

    public List<BrandDocument> findAllActive() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("active", true)
                .orderBy("name")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(BrandDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<BrandDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(BrandDocument.class));
        }
        return Optional.empty();
    }

    public Optional<BrandDocument> findByName(String name) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("name", name)
                .limit(1)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return Optional.of(documents.get(0).toObject(BrandDocument.class));
        }
        return Optional.empty();
    }

    public BrandDocument save(BrandDocument brand) throws ExecutionException, InterruptedException {
        if (brand.getId() == null || brand.getId().isEmpty()) {
            // Create new document
            brand.setCreatedAt(System.currentTimeMillis());
            brand.setUpdatedAt(System.currentTimeMillis());
            brand.setActive(true);
            DocumentReference docRef = getCollection().document();
            brand.setId(docRef.getId());
            docRef.set(brand).get();
        } else {
            // Update existing document
            brand.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(brand.getId()).set(brand).get();
        }
        return brand;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false, "updatedAt", System.currentTimeMillis()).get();
    }

    public long count() throws ExecutionException, InterruptedException {
        return getCollection().get().get().size();
    }
}
