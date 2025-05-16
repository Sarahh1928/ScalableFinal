package com.ecommerce.UserService.services;

import com.ecommerce.UserService.models.AdminProfile;
import com.ecommerce.UserService.models.CustomerProfile;
import com.ecommerce.UserService.models.MerchantProfile;
import com.ecommerce.UserService.models.User;
import com.ecommerce.UserService.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserSeederService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String seedUsers() {
        if (userRepository.count() > 0) {
            return "⚠️ User data already exists.";
        }

        Random rand = new Random();

        // === Customers ===
        CustomerProfile cust1 = new CustomerProfile("Customer1", "customer1@gmail.com", "12345", "123 Customer St", "01000000001");
        cust1.setWallet(10 + rand.nextDouble() * 990); // wallet 10-1000
        CustomerProfile cust2 = new CustomerProfile("Customer2", "customer2@gmail.com", "12345", "456 Customer St", "01000000002");
        cust2.setWallet(10 + rand.nextDouble() * 990);
        CustomerProfile cust3 = new CustomerProfile("Customer3", "customer3@gmail.com", "12345", "789 Customer St", "01000000003");
        cust3.setWallet(10 + rand.nextDouble() * 990);

        // === Merchants ===
        MerchantProfile merch1 = new MerchantProfile("Merchant1", "merchant1@gmail.com", "12345", "Store One", "123 Merchant Ave");
        MerchantProfile merch2 = new MerchantProfile("Merchant2", "merchant2@gmail.com", "12345", "Store Two", "456 Merchant Ave");
        MerchantProfile merch3 = new MerchantProfile("Merchant3", "merchant3@gmail.com", "12345", "Store Three", "789 Merchant Ave");

        // === Admin ===
        AdminProfile admin = new AdminProfile("admin", "admin@gmail.com", "admin");

        // === Encode passwords + verify emails ===
        for (User user : new User[]{cust1, cust2, cust3, merch1, merch2, merch3, admin}) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEmailVerified(true);
        }

        // === Save to DB ===
        userRepository.save(cust1);
        userRepository.save(cust2);
        userRepository.save(cust3);
        userRepository.save(merch1);
        userRepository.save(merch2);
        userRepository.save(merch3);
        userRepository.save(admin);

        return "✅ Users seeded with encrypted passwords, verified emails, and random wallet balances.";
    }
}
