package com.ecommerce.UserService.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("merchant")
public class MerchantProfile extends User {
    private String storeName;
    private String storeAddress;

    // Default constructor
    public MerchantProfile() {
    }

    public MerchantProfile(String username, String email, String password, String storeName, String storeAddress) {
        super(username, email, password, "MERCHANT"); // Call the constructor of the superclass (User)
        this.storeAddress = storeAddress;
        this.storeName = storeName;

    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }
}