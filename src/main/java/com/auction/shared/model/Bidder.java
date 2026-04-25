package com.auction.shared.model;

import java.time.LocalDateTime;
//sau này xây dựng thêm tính năng đặt cuọc tự động

public class Bidder extends User {

    public Bidder( String username, String password,
                  String fullName, double balance,
                  LocalDateTime createdAt) {
        super(username, password, fullName,
              Role.BIDDER, balance, createdAt);
    }

    public void placeBid(Auction auction, double amount) {
        auction.placeBid(this, amount);
    }

    public void deposit(double amount) {
    if (amount <= 0) {
        System.out.println("Invalid amount!");
        return;
    }
    this.balance += amount;
}
}
