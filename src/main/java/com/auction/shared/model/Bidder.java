package com.auction.shared.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Bidder extends User {

    public Bidder( String username, String password,
                  String fullName, BigDecimal balance,
                  LocalDateTime createdAt) {
        super(username, password, fullName,
              "BIDDER", balance, createdAt);
    }

    public Bidder() {
        super();
    }

    public Bidder(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    public Bidder(String username, String password, String fullName, String role) {
        super(username, password, fullName, role);
    }

    public void deposit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("Invalid amount!");
        return;
    }
    this.balance = this.balance.add(amount);
    }


    
}
