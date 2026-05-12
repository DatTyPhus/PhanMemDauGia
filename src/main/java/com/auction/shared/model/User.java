package com.auction.shared.model;

import java.math.BigDecimal;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected String fullName;
    protected String role;
    protected BigDecimal balance;
    public User( String username, String password,
                String fullName,String role,
                BigDecimal balance) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.balance = balance;
    }

    public User (String username, String password) {
        this.username = username;
        this.password = password;
 }

    public User(){
    }

    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    // Getter
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public BigDecimal getBalance() { return balance; }
    public String getPassword() { return password; }
    //setter
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}