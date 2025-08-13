package com.example.spareparts.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.sa.key:}")
    private String firebaseSaKey;

    @Value("${firebase.sa.key.path:}")
    private String firebaseSaKeyPath;

    @Value("${app.cors.allowed-origins:http://localhost:5500,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials;
        if (firebaseSaKey != null && !firebaseSaKey.isBlank()) {
            credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseSaKey.getBytes(StandardCharsets.UTF_8)));
        } else if (firebaseSaKeyPath != null && !firebaseSaKeyPath.isBlank()) {
            if (firebaseSaKeyPath.startsWith("classpath:")) {
                // Load from classpath
                String path = firebaseSaKeyPath.substring("classpath:".length());
                credentials = GoogleCredentials.fromStream(
                    getClass().getClassLoader().getResourceAsStream(path)
                );
            } else {
                // Load from file system
                try (FileInputStream serviceAccount = new FileInputStream(firebaseSaKeyPath)) {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
            }
        } else {
            // Will rely on GOOGLE_APPLICATION_CREDENTIALS or default credentials in runtime
            credentials = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
