package com.salma.pharmacie.frontend.utils;

import com.salma.pharmacie.frontend.model.UserResponse;

public final class Session {
    private static UserResponse currentUser;

    private Session() {}

    public static void setUser(UserResponse user) {
        currentUser = user;
    }

    public static UserResponse getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null
                && currentUser.getRole() != null
                && currentUser.getRole().equalsIgnoreCase(role);
    }
}
