package com.auction.shared.model;

import java.math.BigDecimal;


public class Seller extends User {

    public Seller( String username, String password, String fullName, BigDecimal balance) {
        super( username, password, fullName, "SELLER", balance);
    }

    public Seller() {
        super();
    }

    public Seller(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    public Seller(String username, String password, String fullName, String role) {
        super(username, password, fullName, role);
    }
}