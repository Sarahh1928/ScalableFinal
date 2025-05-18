package com.ecommerce.UserService.services.factory;

import com.ecommerce.UserService.models.*;
import com.ecommerce.UserService.models.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    public User createUser(UserRole role, Object userData) {
        ObjectMapper objectMapper = new ObjectMapper();
        return switch (role) {
            case CUSTOMER -> {
                CustomerProfile customerProfile = objectMapper.convertValue(userData, CustomerProfile.class);
                yield Customer.create(customerProfile);
            }
            case MERCHANT -> {
                MerchantProfile merchantProfile = objectMapper.convertValue(userData, MerchantProfile.class);
                yield Merchant.create(merchantProfile);
            }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}