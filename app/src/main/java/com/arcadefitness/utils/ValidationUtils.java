package com.arcadefitness.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * ValidationUtils.java
 * Reusable input validation methods used across all form screens.
 * Returns null if valid, or an error message string if invalid.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) return "Email is required";
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address";
        return null; // valid
    }

    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) return "Password is required";
        if (password.length() < AppConstants.PASSWORD_MIN_LENGTH)
            return "Password must be at least " + AppConstants.PASSWORD_MIN_LENGTH + " characters";
        return null;
    }

    public static String validateConfirmPassword(String password, String confirm) {
        if (TextUtils.isEmpty(confirm)) return "Please confirm your password";
        if (!password.equals(confirm)) return "Passwords do not match";
        return null;
    }

    public static String validateFullName(String name) {
        if (TextUtils.isEmpty(name)) return "Full name is required";
        if (name.trim().length() < AppConstants.NAME_MIN_LENGTH) return "Enter your full name";
        return null;
    }

    public static String validateAge(String ageStr) {
        if (TextUtils.isEmpty(ageStr)) return "Age is required";
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age < AppConstants.AGE_MIN || age > AppConstants.AGE_MAX)
                return "Enter a valid age (" + AppConstants.AGE_MIN + "–" + AppConstants.AGE_MAX + ")";
        } catch (NumberFormatException e) {
            return "Enter a valid age";
        }
        return null;
    }

    public static String validateGender(int spinnerPosition) {
        if (spinnerPosition == 0) return "Please select a gender";
        return null;
    }
}
