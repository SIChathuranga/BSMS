package com.example.spareparts.service.firestore;

import com.example.spareparts.model.firestore.ReviewDocument;
import com.example.spareparts.model.firestore.ProductDocument;
import com.example.spareparts.repository.firestore.ReviewFirestoreRepository;
import com.example.spareparts.repository.firestore.ProductFirestoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for Review operations using Firestore.
 */
@Service
public class ReviewFirestoreService {

    private final ReviewFirestoreRepository reviewRepository;
    private final ProductFirestoreRepository productRepository;

    public ReviewFirestoreService(ReviewFirestoreRepository reviewRepository,
            ProductFirestoreRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public List<ReviewDocument> getReviewsByProduct(String productId) {
        try {
            return reviewRepository.findByProductId(productId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching reviews", e);
        }
    }

    public List<ReviewDocument> getReviewsByUser(String userId) {
        try {
            return reviewRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user reviews", e);
        }
    }

    public ReviewDocument createReview(ReviewDocument review) {
        try {
            // Check if user already reviewed this product
            if (reviewRepository.hasUserReviewed(review.getProductId(), review.getUserId())) {
                throw new RuntimeException("User has already reviewed this product");
            }

            ReviewDocument savedReview = reviewRepository.save(review);

            // Update product rating
            updateProductRating(review.getProductId());

            return savedReview;
        } catch (Exception e) {
            throw new RuntimeException("Error creating review: " + e.getMessage(), e);
        }
    }

    public ReviewDocument updateReview(ReviewDocument review) {
        try {
            ReviewDocument updatedReview = reviewRepository.save(review);

            // Update product rating
            updateProductRating(review.getProductId());

            return updatedReview;
        } catch (Exception e) {
            throw new RuntimeException("Error updating review", e);
        }
    }

    public void deleteReview(String reviewId) {
        try {
            Optional<ReviewDocument> review = reviewRepository.findById(reviewId);
            if (review.isPresent()) {
                reviewRepository.deleteById(reviewId);
                updateProductRating(review.get().getProductId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting review", e);
        }
    }

    public void markHelpful(String reviewId) {
        try {
            reviewRepository.incrementHelpful(reviewId);
        } catch (Exception e) {
            throw new RuntimeException("Error marking review as helpful", e);
        }
    }

    private void updateProductRating(String productId) {
        try {
            double avgRating = reviewRepository.getAverageRating(productId);
            int reviewCount = reviewRepository.getReviewCount(productId);

            Optional<ProductDocument> product = productRepository.findById(productId);
            if (product.isPresent()) {
                ProductDocument p = product.get();
                p.setAverageRating(avgRating);
                p.setReviewCount(reviewCount);
                productRepository.save(p);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating product rating", e);
        }
    }
}
