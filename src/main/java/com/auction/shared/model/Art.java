package com.auction.shared.model;

import java.math.BigDecimal;
public class Art extends Item {
    private String artist;                      //tác giả
    private int itemId;

    public Art(int sellerId, String itemName, String description,
               BigDecimal startPrice, String imageUrl, String artist) {
        super(sellerId, itemName, description, "Art", startPrice, imageUrl);
        this.artist = artist;
        this.itemId = getId();         //mỗi item có một id, lưu vào biến itemId
    }

    public Art(){
        super();
    }    
    @Override
    public void printInfo() {
        System.out.println("Art Work: " + itemName + " | Artist: " + artist + " | itemId:" + itemId);
    }
}