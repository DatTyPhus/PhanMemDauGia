package com.auction.shared.model;

public class Bidder extends User {

    public Bidder(String username, String password) {
        super(username, password);
    }

    public void placeBid(Auction auction, double amount) {
        auction.placeBid(this, amount);
    }
}
