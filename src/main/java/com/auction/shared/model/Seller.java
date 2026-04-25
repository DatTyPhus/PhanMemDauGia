package com.auction.shared.model;

public class Seller extends User {

    public Seller(String username, String password) {
        super(username, password);
    }

    public Auction createAuction(Item item) {
        return new Auction(item, this);
    }
}