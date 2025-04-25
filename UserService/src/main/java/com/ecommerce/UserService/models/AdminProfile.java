package com.ecommerce.UserService.models;

import jakarta.persistence.Entity;

@Entity
public class AdminProfile extends User {
    public AdminProfile(String username, String email, String password) {
        super(username, email, password,"ADMIN");
    }

    public AdminProfile() {
        super();
    }
}
