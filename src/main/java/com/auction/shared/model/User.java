package com.auction.shared.model;

import java.time.LocalDateTime;

enum Role {
    ADMIN,
    SELLER,
    BIDDER
}

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected String fullName;
    protected Role role;
    protected double balance;
    protected LocalDateTime createdAt;
    protected int userId;
    public User( String username, String password,
                String fullName, Role role,
                double balance, LocalDateTime createdAt) {
        super();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.balance = balance;
        this.createdAt = createdAt;
        this.userId = getId();                   //mỗi người co một id, lưu id vào userId
    }

    // Getter
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public double getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPassword() { return password; }
}