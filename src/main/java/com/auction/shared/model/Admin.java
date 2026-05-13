package com.auction.shared.model;

import java.math.BigDecimal;

public class Admin extends User {

    public Admin( String username, String password,
                 String fullName) {
        super( username, password, fullName,
              "ADMIN", BigDecimal.ZERO);
    }

    public void cancelAuction(Auction auction) {
        if (auction == null){
            System.out.println("Auction is null!");
        }
        auction.cancel();
    }
}