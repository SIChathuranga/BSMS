package com.example.spareparts.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.spareparts.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final UserService userService;

    public TokenAuthFilter(FirebaseAuth firebaseAuth, UserService userService) {
        this.firebaseAuth = firebaseAuth;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("TokenAuthFilter: Processing request to " + request.getRequestURI());
        System.out.println("TokenAuthFilter: Authorization header: " + (authHeader != null ? 
            (authHeader.startsWith("Bearer ") ? "Present (Bearer token)" : "Present (Basic auth)") : "Not present"));
        
        // Only process Bearer tokens, let Basic auth pass through to Spring Security
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7);
            try {
                System.out.println("TokenAuthFilter: Attempting to verify Firebase token");
                FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
                System.out.println("TokenAuthFilter: Token verified successfully for user: " + decoded.getEmail());
                var user = userService.ensureUser(decoded.getUid(), decoded.getEmail(), decoded.getName());
                List<GrantedAuthority> authorities = user.isAdmin() ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) : Collections.emptyList();
                Authentication auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("TokenAuthFilter: Authentication set for user: " + user.getEmail() + ", isAdmin: " + user.isAdmin());
            } catch (IllegalArgumentException | com.google.firebase.auth.FirebaseAuthException e) {
                System.err.println("TokenAuthFilter: Token verification failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token: " + e.getMessage());
                return;
            }
        } else if (authHeader != null && authHeader.startsWith("Basic ")) {
            System.out.println("TokenAuthFilter: Basic auth detected, passing to Spring Security");
        }
        filterChain.doFilter(request, response);
    }
}
