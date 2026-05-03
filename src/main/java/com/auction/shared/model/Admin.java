package com.auction.shared.model;

import java.time.LocalDateTime;

public class Admin extends User {

    public Admin( String username, String password,
                 String fullName,
                 LocalDateTime createdAt) {
        super( username, password, fullName,
              Role.ADMIN, 0, createdAt);
    }

    public void cancelAuction(Auction auction) {
        if (auction == null){
            System.out.println("Auction is null!");
        }
        auction.cancel();
    }
}