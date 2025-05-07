package com.ecommerce.UserService.services;

import com.ecommerce.UserService.authUtilities.JwtUtil;
import com.ecommerce.UserService.models.*;
import com.ecommerce.UserService.models.enums.UserRole;
import com.ecommerce.UserService.repositories.PasswordResetTokenRepository;
import com.ecommerce.UserService.repositories.UserRepository;
import com.ecommerce.UserService.services.factory.UserFactory;
import com.ecommerce.UserService.services.singleton.SessionManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_MERCHANT = "MERCHANT";
    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private UserFactory userFactory;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RedisTemplate<String, UserSession> redisTemplate;
    @Autowired private EmailService emailService;

    // UTILITY
    private UserSession getSessionOrThrow(String token) {
        UserSession session = redisTemplate.opsForValue().get(token);
        if (session == null) {
            throw new IllegalStateException("Invalid session or user not logged in.");
        }
        return session;
    }

    private PasswordResetToken getValidPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalStateException("Invalid or expired password reset token."));
    }

    private User getUserOrThrow(String token, Long id) {
        User user= userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        UserSession session = getSessionOrThrow(token);
        if (!session.getRole().equalsIgnoreCase(ROLE_ADMIN)&&!user.getId().equals(session.getUserId())) {
            throw new IllegalStateException("You are not to access this user info.");
        }
        return user;
    }

    // PASSWORD RESET
    public void requestPasswordReset(String username) {


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user found with the provided email."));
        PasswordResetToken resetToken = new PasswordResetToken(
                UUID.randomUUID().toString(),
                user,
                LocalDateTime.now().plusHours(1)
        );
        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
    }


    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = getValidPasswordResetToken(token);
        User user = resetToken.getUser();
        UserSession session = getSessionOrThrow(token);
        if (!user.getId().equals(session.getUserId())) {
            throw new IllegalStateException("You are not to access this user info.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    // REGISTRATION
    @Transactional
    public User registerUser(UserRole role, Object userData) {
        User user = userFactory.createUser(role, userData);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);
        emailService.sendEmailVerification(user.getEmail(), user.getEmailVerificationToken());
        return user;
    }

    public boolean verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    // AUTH
    @Transactional
    public String loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if(user.isBanned()){
            throw new IllegalStateException("User is banned.");
        }

        if (!user.getRole().equalsIgnoreCase("ADMIN") && !user.isEmailVerified()) {
            throw new IllegalStateException("Please verify your email before logging in.");
        }

        if (SessionManager.getInstance().getSessionByUserId(user.getId()) != null) {
            throw new IllegalStateException("User already logged in.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        UserSession session = new UserSession(token, user.getId(), user.getRole(), user.getEmail());
        redisTemplate.opsForValue().set(token, session);
        SessionManager.getInstance().addSession(token, session);

        return token;
    }

    public void logoutUser(String token) {
        UserSession session = SessionManager.getInstance().getSession(token);
        if (session == null) {
            throw new IllegalStateException("Invalid or expired token.");
        }
        redisTemplate.delete(token);
        SessionManager.getInstance().invalidateToken(token);
    }

    public boolean isTokenValid(String token) {
        return SessionManager.getInstance().isTokenValid(token);
    }

    public UserSession getSessionByToken(String token) {
        return getSessionOrThrow(token);
    }

    // USER MANAGEMENT
    public Optional<User> getUserById(Long id, String token) {
        UserSession session = getSessionOrThrow(token);
        if (session.getRole().equalsIgnoreCase("ADMIN") || session.getUserId().equals(id)) {
            return userRepository.findById(id);
        } else {
            throw new IllegalStateException("Unauthorized access.");
        }
    }

    @Transactional
    public User updateUser(Long id, Object updatedData, String token) {
        UserSession session = getSessionOrThrow(token);

        if (!session.getRole().equalsIgnoreCase("ADMIN") && !session.getUserId().equals(id)) {
            throw new IllegalStateException("Unauthorized update attempt.");
        }

        User existingUser = getUserOrThrow(token, id);

        // If the data is a Map of fields
        if (!(updatedData instanceof Map)) {
            throw new IllegalArgumentException("Updated data is not a valid map.");
        }

        Map<String, Object> data = (Map<String, Object>) updatedData;

        if (existingUser.getRole().equalsIgnoreCase("CUSTOMER")) {
            updateCustomerProfile((CustomerProfile) existingUser, data);
        } else if (existingUser.getRole().equalsIgnoreCase("MERCHANT")) {
            updateMerchantProfile((MerchantProfile) existingUser, data);
        } else {
            throw new IllegalArgumentException("Mismatched profile types or invalid role.");
        }

        return userRepository.save(existingUser);
    }

    private void updateCustomerProfile(CustomerProfile existingCustomer, Map<String, Object> data) {
        if (data.containsKey("username")) {
            existingCustomer.setUsername((String) data.get("username"));
        }
        if (data.containsKey("email")) {
            existingCustomer.setEmail((String) data.get("email"));
        }
        if (data.containsKey("password")) {
            existingCustomer.setPassword(passwordEncoder.encode((String) data.get("password")));
        }
        if (data.containsKey("phoneNumber")) {
            existingCustomer.setPhoneNumber((String) data.get("phoneNumber"));
        }
        if (data.containsKey("shippingAddress")) {
            existingCustomer.setShippingAddress((String) data.get("shippingAddress"));
        }
    }

    private void updateMerchantProfile(MerchantProfile existingMerchant, Map<String, Object> data) {
        if (data.containsKey("username")) {
            existingMerchant.setUsername((String) data.get("username"));
        }
        if (data.containsKey("email")) {
            existingMerchant.setEmail((String) data.get("email"));
        }
        if (data.containsKey("password")) {
            existingMerchant.setPassword(passwordEncoder.encode((String) data.get("password")));
        }
        if (data.containsKey("storeName")) {
            existingMerchant.setStoreName((String) data.get("storeName"));
        }
        if (data.containsKey("storeAddress")) {
            existingMerchant.setStoreAddress((String) data.get("storeAddress"));
        }
    }

    @Transactional
    public void deleteUser(Long id, String token) {
        UserSession session = getSessionOrThrow(token);

        if (!session.getRole().equalsIgnoreCase("ADMIN")) {
            throw new IllegalStateException("Only admins can delete users.");
        }
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            // Don't care, already deleted
        }
    }

    @Transactional
    public void deleteMyAccount(String token) {
        UserSession session = getSessionOrThrow(token);
        userRepository.findById(session.getUserId()).ifPresent(userRepository::delete);
    }

    @Transactional
    public void banUser(Long id, String token) {
        UserSession session = getSessionOrThrow(token);

        if (!session.getRole().equalsIgnoreCase("ADMIN")) {
            throw new IllegalStateException("Only admins can ban users.");
        }

        User user = getUserOrThrow(token, id);
        user.setBanned(true);
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(Long id, String token) {
        UserSession session = getSessionOrThrow(token);

        if (!session.getRole().equalsIgnoreCase("ADMIN")) {
            throw new IllegalStateException("Only admins can unban users.");
        }

        User user = getUserOrThrow(token, id);
        user.setBanned(false);
        userRepository.save(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public void deposit(Long userId, Double amount) {
        CustomerProfile customer = (CustomerProfile) getUser(userId);

        customer.setWallet(customer.getWallet() + amount);
        userRepository.save(customer);
    }

    @Transactional
    public void deduct(String token, Long userId, Double amount) {
        getSessionOrThrow(token);

        CustomerProfile customer = (CustomerProfile) getUser(userId);

        customer.setWallet(amount);
        userRepository.save(customer);
    }
}
