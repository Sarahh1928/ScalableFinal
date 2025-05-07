package com.ecommerce.UserService.controllers;

import com.ecommerce.UserService.authUtilities.JwtUtil;
import com.ecommerce.UserService.models.User;
import com.ecommerce.UserService.models.UserSession;
import com.ecommerce.UserService.models.enums.UserRole;
import com.ecommerce.UserService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to extract the token from the Authorization header
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }

    // Helper method to validate the token
    private boolean isTokenValid(String token) {
        return token != null && userService.isTokenValid(token);
    }

    @PostMapping("/deposit/{userId}")
    public void deposit(@PathVariable Long userId, @RequestBody Double amount) {
        userService.deposit( userId, amount);
    }

    @PostMapping("/deduct/{userId}")
    public ResponseEntity<User> deduct(@RequestHeader("Authorization") String token, @PathVariable Long userId, @RequestBody Double amount) {
        userService.deduct(extractToken(token), userId, amount);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestParam("role") UserRole role, @RequestBody Object userData) {
        User user = userService.registerUser(role, userData);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        return userService.loginUser(username, password);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);

            if (!isTokenValid(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
            }

            userService.logoutUser(actualToken);
            return ResponseEntity.ok("User logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error logging out: " + e.getMessage());
        }
    }

    @GetMapping("/session")
    public ResponseEntity<UserSession> getSessionFromToken(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);

            if (actualToken == null || !userService.isTokenValid(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);  // Invalid or expired token
            }

            UserSession session = userService.getSessionByToken(actualToken);

            if (session != null) {
                return ResponseEntity.ok(session);  // Return userId if valid
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // Token is valid but userId is not found
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // Handle other errors
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> user = userService.getUserById(id, token);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody Object userData, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User updated = userService.updateUser(id, userData, token);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (!isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userService.deleteUser(id, token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/ban/{id}")
    public ResponseEntity<Void> banUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (!isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userService.banUser(id, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/unban/{id}")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (!isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userService.unbanUser(id, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteMyAccount(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        // Check if token is valid
        if (token == null || !isTokenValid(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);  // Token is invalid or missing
        }

        // Proceed to delete the account
        userService.deleteMyAccount(token);  // Delete the user's account

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // Return No Content response on success
    }

    // Request Password Reset (Step 1)
    @PostMapping("/reset-request")
    public void requestPasswordReset(@RequestParam String username) {
        userService.requestPasswordReset(username);
    }

    // Reset Password (Step 3)
    @PostMapping("/reset")
    public void resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        boolean isVerified = userService.verifyEmail(token);
        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }
}
