package com.ecommerce.PaymentService.dto;


public class WalletUpdateRequest {
    private double walletBalance;

    public WalletUpdateRequest() {}

    public WalletUpdateRequest(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }
}
