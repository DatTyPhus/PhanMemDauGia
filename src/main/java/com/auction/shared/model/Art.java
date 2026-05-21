package com.auction.shared.model;

import java.math.BigDecimal;
public class Art extends Item {
    private String artist;                      //tác giả
    private int itemId;

    public Art(){
        super();
    }    
    @Override
    public void printInfo() {
        System.out.println("Art Work: " + itemName + " | Artist: " + artist + " | itemId:" + itemId);
    }
}