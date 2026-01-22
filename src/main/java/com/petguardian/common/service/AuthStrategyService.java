package com.petguardian.common.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy Service interface for user authentication (Strategy Pattern).
 * Allows switching between mock auth (URL param) and Spring Security
 * (HttpSession).
 */
public interface AuthStrategyService {

    /**
     * Get current authenticated user's ID.
     * 
     * @param request the HTTP request
     * @return user ID, or null if not authenticated
     */
    Integer getCurrentUserId(HttpServletRequest request);

    /**
     * Get current authenticated user's name.
     * 
     * @param request the HTTP request
     * @return user name, or null if not authenticated
     */
    String getCurrentUserName(HttpServletRequest request);

    /**
     * Check if user is authenticated.
     * 
     * @param request the HTTP request
     * @return true if authenticated
     */
    default boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }
}
