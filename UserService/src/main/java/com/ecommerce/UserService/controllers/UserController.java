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
        return null;
    }

    // Helper method to validate the token
    private boolean isTokenValid(String token) {
        return token != null && userService.isTokenValid(token);
    }

    @PostMapping("/deposit/{userId}")
    public ResponseEntity<String> deposit(@PathVariable Long userId, @RequestBody Double amount) {
        try {
            userService.deposit(userId, amount);
            return ResponseEntity.ok("Deposit successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deposit failed: " + e.getMessage());
        }
    }


    @PostMapping("/deduct/{userId}")
    public ResponseEntity<String> deduct(@RequestHeader("Authorization") String token, @PathVariable Long userId, @RequestBody Double amount) {
        try {
            userService.deduct(extractToken(token), userId, amount);
            return ResponseEntity.status(HttpStatus.CREATED).body("Amount deducted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deduction failed: " + e.getMessage());
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("role") UserRole role, @RequestBody Object userData) {
        try {
            User user = userService.registerUser(role, userData);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            String token = userService.loginUser(username, password);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Logout failed: " + e.getMessage());
        }
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSessionFromToken(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);

            if (!isTokenValid(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
            }

            UserSession session = userService.getSessionByToken(actualToken);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error retrieving session: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);

            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Invalid token.", HttpStatus.UNAUTHORIZED);
            }

            Optional<User> user = userService.getUserById(id, token);

            if (user.isEmpty()) {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(user.get(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching user: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Object userData, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Unauthorized - invalid token", HttpStatus.UNAUTHORIZED);
            }

            User updated = userService.updateUser(id, userData, token);
            return new ResponseEntity<>(updated, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Update failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Unauthorized - invalid token", HttpStatus.UNAUTHORIZED);
            }

            userService.deleteUser(id, token);
            return new ResponseEntity<>("User deleted", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Delete failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/ban/{id}")
    public ResponseEntity<String> banUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Unauthorized - invalid token", HttpStatus.UNAUTHORIZED);
            }

            userService.banUser(id, token);
            return new ResponseEntity<>("User banned", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Ban failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/unban/{id}")
    public ResponseEntity<String> unbanUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Unauthorized - invalid token", HttpStatus.UNAUTHORIZED);
            }

            userService.unbanUser(id, token);
            return new ResponseEntity<>("User unbanned", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Unban failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMyAccount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);

            if (!isTokenValid(token)) {
                return new ResponseEntity<>("Unauthorized - invalid token", HttpStatus.UNAUTHORIZED);
            }

            userService.deleteMyAccount(token);
            return new ResponseEntity<>("Account deleted successfully", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting account: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // Request Password Reset (Step 1)
    @PostMapping("/reset-request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String username) {
        try {
            userService.requestPasswordReset(username);
            return ResponseEntity.ok("Password reset link sent.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset request failed: " + e.getMessage());
        }
    }

    // Reset Password (Step 3)
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password has been reset.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset failed: " + e.getMessage());
        }
    }


    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            boolean isVerified = userService.verifyEmail(token);
            if (isVerified) {
                return ResponseEntity.ok("Email verified successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification failed: " + e.getMessage());
        }
    }

}
