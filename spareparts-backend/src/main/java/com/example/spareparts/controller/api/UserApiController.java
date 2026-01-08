package com.example.spareparts.controller.api;

import com.example.spareparts.model.firestore.*;
import com.example.spareparts.service.firestore.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User API endpoints - requires authentication.
 */
@RestController
@RequestMapping("/api/v2/user")
@CrossOrigin(origins = "*")
public class UserApiController {

    private final UserFirestoreService userService;
    private final OrderFirestoreService orderService;
    private final ReviewFirestoreService reviewService;

    public UserApiController(UserFirestoreService userService,
            OrderFirestoreService orderService,
            ReviewFirestoreService reviewService) {
        this.userService = userService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    // ============== USER PROFILE ==============

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDocument> getUserProfile(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profile")
    public ResponseEntity<UserDocument> createOrUpdateProfile(@RequestBody UserDocument user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @PostMapping("/profile/sync")
    public ResponseEntity<UserDocument> syncProfile(@RequestBody Map<String, String> payload) {
        String uid = payload.get("uid");
        String email = payload.get("email");
        String displayName = payload.get("displayName");
        String photoUrl = payload.get("photoUrl");

        return ResponseEntity.ok(userService.createOrUpdateUser(uid, email, displayName, photoUrl));
    }

    // ============== ORDERS ==============

    @GetMapping("/orders/{userId}")
    public ResponseEntity<List<OrderDocument>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/orders/{userId}/{orderId}")
    public ResponseEntity<OrderDocument> getOrderById(@PathVariable String userId,
            @PathVariable String orderId) {
        return orderService.getOrderById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDocument> createOrder(@RequestBody OrderDocument order) {
        try {
            OrderDocument createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
            @RequestParam String userId) {
        try {
            // Verify order belongs to user
            OrderDocument order = orderService.getOrderById(orderId).orElse(null);
            if (order == null || !order.getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }
            if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
                return ResponseEntity.badRequest().build();
            }
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============== WISHLIST ==============

    @GetMapping("/wishlist/{userId}")
    public ResponseEntity<List<ProductDocument>> getWishlist(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getWishlistProducts(userId));
    }

    @PostMapping("/wishlist/{userId}/{productId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable String userId,
            @PathVariable String productId) {
        userService.addToWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/wishlist/{userId}/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String userId,
            @PathVariable String productId) {
        userService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wishlist/{userId}/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(@PathVariable String userId,
            @PathVariable String productId) {
        boolean inWishlist = userService.isInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    // ============== ADDRESSES ==============

    @PostMapping("/addresses/{userId}")
    public ResponseEntity<Void> addAddress(@PathVariable String userId,
            @RequestBody UserDocument.AddressDocument address) {
        userService.addAddress(userId, address);
        return ResponseEntity.ok().build();
    }

    // ============== REVIEWS ==============

    @GetMapping("/reviews/{userId}")
    public ResponseEntity<List<ReviewDocument>> getUserReviews(@PathVariable String userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewDocument> createReview(@RequestBody ReviewDocument review) {
        try {
            return ResponseEntity.ok(reviewService.createReview(review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewDocument> updateReview(@PathVariable String reviewId,
            @RequestBody ReviewDocument review) {
        review.setId(reviewId);
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId,
            @RequestParam String userId) {
        try {
            // Verify review belongs to user
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewHelpful(@PathVariable String reviewId) {
        reviewService.markHelpful(reviewId);
        return ResponseEntity.ok().build();
    }
}
