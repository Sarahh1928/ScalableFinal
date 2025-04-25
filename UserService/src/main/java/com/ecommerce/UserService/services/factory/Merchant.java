package com.ecommerce.UserService.services.factory;

import com.ecommerce.UserService.models.MerchantProfile;
import com.ecommerce.UserService.models.User;

public class Merchant {
    public static MerchantProfile create(MerchantProfile data) {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setUsername(data.getUsername());
        merchant.setEmail(data.getEmail());
        merchant.setPassword(data.getPassword());
        merchant.setStoreName(data.getStoreName());
        merchant.setStoreAddress(data.getStoreAddress());
        merchant.setRole("MERCHANT");
        return merchant;
    }
}
