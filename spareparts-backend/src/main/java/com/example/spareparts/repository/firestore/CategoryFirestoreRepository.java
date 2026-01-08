package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.CategoryDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Category operations.
 */
@Repository
public class CategoryFirestoreRepository {

    private static final String COLLECTION_NAME = "categories";
    private final Firestore firestore;

    public CategoryFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<CategoryDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .orderBy("displayOrder")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(CategoryDocument.class))
                .collect(Collectors.toList());
    }

    public List<CategoryDocument> findAllActive() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("active", true)
                .orderBy("displayOrder")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(CategoryDocument.class))
                .collect(Collectors.toList());
    }

    public List<CategoryDocument> findRootCategories() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("parentId", null)
                .whereEqualTo("active", true)
                .orderBy("displayOrder")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(CategoryDocument.class))
                .collect(Collectors.toList());
    }

    public List<CategoryDocument> findSubcategories(String parentId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("active", true)
                .orderBy("displayOrder")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(CategoryDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<CategoryDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(CategoryDocument.class));
        }
        return Optional.empty();
    }

    public Optional<CategoryDocument> findBySlug(String slug) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("slug", slug)
                .limit(1)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return Optional.of(documents.get(0).toObject(CategoryDocument.class));
        }
        return Optional.empty();
    }

    public CategoryDocument save(CategoryDocument category) throws ExecutionException, InterruptedException {
        if (category.getId() == null || category.getId().isEmpty()) {
            // Create new document
            category.setCreatedAt(System.currentTimeMillis());
            category.setUpdatedAt(System.currentTimeMillis());
            category.setActive(true);
            if (category.getSlug() == null) {
                category.setSlug(generateSlug(category.getName()));
            }
            DocumentReference docRef = getCollection().document();
            category.setId(docRef.getId());
            docRef.set(category).get();
        } else {
            // Update existing document
            category.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(category.getId()).set(category).get();
        }
        return category;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false, "updatedAt", System.currentTimeMillis()).get();
    }

    private String generateSlug(String name) {
        if (name == null)
            return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
