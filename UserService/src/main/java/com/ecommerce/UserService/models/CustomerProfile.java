package com.ecommerce.UserService.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("customer")
public class CustomerProfile extends User {
    private String shippingAddress;
    private String phoneNumber;
    private Double wallet;

    // Default constructor
    public CustomerProfile() {
        this.wallet = 0.0;
        // You can use this if you need any specific initialization
    }

    // Constructor with parameters to initialize fields
    public CustomerProfile(String username, String email, String password, String shippingAddress, String phoneNumber) {
        super(username, email, password, "CUSTOMER"); // Call the constructor of the superclass (User)
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.wallet = 0.0;
    }

    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }
}
