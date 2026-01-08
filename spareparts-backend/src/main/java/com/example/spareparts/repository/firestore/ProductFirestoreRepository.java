package com.example.spareparts.repository.firestore;

import com.example.spareparts.model.firestore.ProductDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Firestore repository for Product operations.
 */
@Repository
public class ProductFirestoreRepository {

    private static final String COLLECTION_NAME = "products";
    private final Firestore firestore;

    public ProductFirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public List<ProductDocument> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection().get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ProductDocument.class))
                .collect(Collectors.toList());
    }

    public List<ProductDocument> findAllActive() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ProductDocument.class))
                .collect(Collectors.toList());
    }

    public Optional<ProductDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(ProductDocument.class));
        }
        return Optional.empty();
    }

    public ProductDocument save(ProductDocument product) throws ExecutionException, InterruptedException {
        if (product.getId() == null || product.getId().isEmpty()) {
            // Create new document
            product.setCreatedAt(System.currentTimeMillis());
            product.setUpdatedAt(System.currentTimeMillis());
            product.setActive(true);
            DocumentReference docRef = getCollection().document();
            product.setId(docRef.getId());
            docRef.set(product).get();
        } else {
            // Update existing document
            product.setUpdatedAt(System.currentTimeMillis());
            getCollection().document(product.getId()).set(product).get();
        }
        return product;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).delete().get();
    }

    public void softDelete(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false, "updatedAt", System.currentTimeMillis()).get();
    }

    public List<ProductDocument> findByCategory(String category) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("category", category)
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ProductDocument.class))
                .collect(Collectors.toList());
    }

    public List<ProductDocument> findByBrand(String brand) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereEqualTo("brand", brand)
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ProductDocument.class))
                .collect(Collectors.toList());
    }

    public List<ProductDocument> findLowStock(int threshold) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getCollection()
                .whereLessThanOrEqualTo("stock", threshold)
                .whereEqualTo("active", true)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(ProductDocument.class))
                .collect(Collectors.toList());
    }

    public List<ProductDocument> searchProducts(String query) throws ExecutionException, InterruptedException {
        // Firestore doesn't support full-text search natively
        // This is a simple implementation - for production use Algolia or similar
        List<ProductDocument> allProducts = findAllActive();
        String lowerQuery = query.toLowerCase();
        return allProducts.stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerQuery)) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery)) ||
                        (p.getCategory() != null && p.getCategory().toLowerCase().contains(lowerQuery)) ||
                        (p.getBrand() != null && p.getBrand().toLowerCase().contains(lowerQuery)) ||
                        (p.getSku() != null && p.getSku().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    public void updateStock(String productId, int quantity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(productId);
        docRef.update("stock", FieldValue.increment(quantity), "updatedAt", System.currentTimeMillis()).get();
    }

    public long count() throws ExecutionException, InterruptedException {
        return getCollection().get().get().size();
    }
}
