package com.auction.shared.model;


public class Admin extends User {

    public Admin( String username, String password) {
        super(username, password);
    }
    
    public Admin() {
        super();
    }
    public void cancelAuction(Auction auction) {
        if (auction == null){
            System.out.println("Auction is null!");
        }
        auction.cancel();
    }
}