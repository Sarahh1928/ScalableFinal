package com.ecommerce.UserService.services;

import com.ecommerce.UserService.authUtilities.JwtUtil;
import com.ecommerce.UserService.models.PasswordResetToken;
import com.ecommerce.UserService.models.UserSession;
import com.ecommerce.UserService.repositories.PasswordResetTokenRepository;
import com.ecommerce.UserService.repositories.UserRepository;
import com.ecommerce.UserService.models.User;
import com.ecommerce.UserService.models.enums.UserRole;
import com.ecommerce.UserService.services.factory.UserFactory;
import com.ecommerce.UserService.services.singleton.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Inject PasswordEncoder

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, UserSession> redisTemplate;

    @Autowired
    private EmailService emailService;

    // Step 1: Request Password Reset (Generate Reset Token)
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Generate a unique reset token
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour

            // Save the token in the database
            passwordResetTokenRepository.save(passwordResetToken);

            // Send the token to the user's email
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } else {
            throw new RuntimeException("No user found with the provided email.");
        }
    }

    // Step 2: Validate Password Reset Token
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenOpt.isPresent()) {
            PasswordResetToken passwordResetToken = passwordResetTokenOpt.get();

            // Check if the token is expired
            if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired.");
            }

            return true; // Token is valid
        } else {
            throw new RuntimeException("Invalid or expired token.");
        }
    }

    // Step 3: Reset User's Password
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenOpt.isPresent()) {
            PasswordResetToken passwordResetToken = passwordResetTokenOpt.get();

            // Check if the token is expired
            if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired.");
            }

            // Get the user associated with the token
            User user = passwordResetToken.getUser();

            // Update the user's password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Optionally, remove the token after it is used
            passwordResetTokenRepository.delete(passwordResetToken);
        } else {
            throw new RuntimeException("Invalid token.");
        }
    }

    // Register user and hash password
    public User registerUser(UserRole role, Object userData) {
        // Create user using the factory
        User user = userFactory.createUser(role, userData);

        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Save user to the database
        user = userRepository.save(user);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        userRepository.save(user);

        // Send verification email
        emailService.sendEmailVerification(user.getEmail(), verificationToken);

        return user;
    }
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Mark the user's email as verified
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null); // Clear the verification token after successful verification
            userRepository.save(user);
            return true;  // Email verified successfully
        }
        return false;  // Invalid token
    }


    // Login user, generate JWT, store in Redis, and add to session manager
    public String loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Validate password
            if (passwordEncoder.matches(password, user.getPassword())) {

                if (!user.getRole().equalsIgnoreCase("ADMIN")&&!user.isEmailVerified()) {
                    throw new RuntimeException("Please verify your email before logging in.");
                }
                UserSession existingSession = SessionManager.getInstance().getSessionByUserId(user.getId());
                // If there's an existing session, prevent login
                if (existingSession != null) {
                    throw new RuntimeException("User is already logged in.");
                }

                // Generate JWT
                String token = jwtUtil.generateToken(user.getId(), user.getRole());

                // Create UserSession object with the token, userId, and role
                UserSession userSession = new UserSession(token, user.getId(), user.getRole(), user.getEmail());

                // Store the UserSession object in Redis (using token as the key)
                redisTemplate.opsForValue().set(token, userSession);  // Store by token in Redis

                // Add to SessionManager to manage active sessions in-memory by userId
                SessionManager.getInstance().addSession(token, userSession);

                return token;
            }
        }
        throw new RuntimeException("Invalid credentials");
    }

    // Validate token using SessionManager
    public boolean isTokenValid(String token) {
        return SessionManager.getInstance().isTokenValid(token);
    }

    // Get user session by token (using token as key in Redis)
    public UserSession getSessionByToken(String token) {
        // Retrieve session from Redis using token as the key
        UserSession session = redisTemplate.opsForValue().get(token);
        if (session != null) {
            return session;
        }
        throw new RuntimeException("Session not found for the provided token.");
    }

    // Get user session by userId (fetch token from SessionManager)
    public UserSession getSessionByUserId(Long userId) {
        // Get token from SessionManager using userId
        UserSession session = SessionManager.getInstance().getSessionByUserId(userId);
        if (session != null) {
            return redisTemplate.opsForValue().get(session.getToken());  // Retrieve session using token from Redis
        }
        throw new RuntimeException("No active session for user.");
    }

    // Logout user
    public void logoutUser(String token) {
        UserSession existingSession = SessionManager.getInstance().getSession(token);
        if (existingSession == null) {
            throw new RuntimeException("Invalid or expired token. User is not logged in.");
        }

        // Remove the session from Redis using token
        redisTemplate.delete(token);

        // Invalidate the token using SessionManager
        SessionManager.getInstance().invalidateToken(token);
    }

    // Fetch user by userId and validate authorization
    public Optional<User> getUserById(Long id, String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);
        if (currentSession == null) {
            throw new RuntimeException("User is not logged in.");
        }

        // If the session is for an admin or the user is fetching their own data
        if (currentSession.getRole().equalsIgnoreCase("ADMIN") || currentSession.getUserId().equals(id)) {
            return userRepository.findById(id);
        } else {
            throw new RuntimeException("You are not authorized to access this user's data.");
        }
    }

    // Update user details
    public User updateUser(Long id, User updatedData, String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);
        if (currentSession == null) {
            throw new RuntimeException("User is not logged in.");
        }

        // If the session is for an admin or the user is updating their own data
        if (currentSession.getRole().equalsIgnoreCase("ADMIN") || currentSession.getUserId().equals(id)) {
            return userRepository.findById(id).map(user -> {
                user.setUsername(updatedData.getUsername());
                user.setEmail(updatedData.getEmail());
                user.setPassword(passwordEncoder.encode(updatedData.getPassword())); // Rehash the password
                return userRepository.save(user);
            }).orElseThrow();
        } else {
            throw new RuntimeException("You are not authorized to update this user's data.");
        }
    }

    // Delete user by id (only admins can perform this action)
    public void deleteUser(Long id, String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);

        if (currentSession == null || !currentSession.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("You must be an admin to perform this action.");
        }
        userRepository.deleteById(id);
    }

    // Ban a user
    public void banUser(Long id, String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);

        if (currentSession == null || !currentSession.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("You must be an admin to perform this action.");
        }

        userRepository.findById(id).ifPresent(user -> {
            user.setBanned(true);
            userRepository.save(user);
        });
    }

    // Unban a user
    public void unbanUser(Long id, String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);

        if (currentSession == null || !currentSession.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("You must be an admin to perform this action.");
        }

        userRepository.findById(id).ifPresent(user -> {
            user.setBanned(false);
            userRepository.save(user);
        });
    }

    // Delete current user's account
    public void deleteMyAccount(String token) {
        UserSession currentSession = redisTemplate.opsForValue().get(token);

        if (currentSession == null) {
            throw new RuntimeException("User is not logged in.");
        }

        userRepository.findById(currentSession.getUserId()).ifPresent(user -> {
            userRepository.delete(user);  // Delete the user from the database
        });
    }
}
