package com.ecommerce.UserService.services.factory;

import com.ecommerce.UserService.models.CustomerProfile;
import com.ecommerce.UserService.models.User;

public class Customer {
    public static CustomerProfile create(CustomerProfile data) {
        CustomerProfile customer = new CustomerProfile();
        customer.setUsername(data.getUsername());
        customer.setEmail(data.getEmail());
        customer.setPassword(data.getPassword());
        customer.setRole("CUSTOMER");
        customer.setShippingAddress(data.getShippingAddress());
        customer.setPhoneNumber(data.getPhoneNumber());
        customer.setWallet(0.0);
        return customer;
    }
}
