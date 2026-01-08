package com.example.spareparts.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Firebase Firestore connection.
 * This provides a Firestore instance for all repository classes.
 */
@Configuration
public class FirestoreConfig {

    private final FirebaseApp firebaseApp;

    public FirestoreConfig(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
