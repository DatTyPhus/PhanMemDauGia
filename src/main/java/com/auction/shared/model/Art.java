package com.auction.shared.model;

import com.auction.shared.model.Item;

import java.math.BigDecimal;
public class Art extends Item {
    private String artist; //tác giả

    public Art(int itemId, int sellerId, String itemName, String description,
               BigDecimal startPrice, String imageUrl, String artist) {
        super(itemId, sellerId, itemName, description, "Art", startPrice, imageUrl);
        this.artist = artist;
    }

    @Override
    public void printInfo() {
        System.out.println("Art Work: " + itemName + " | Artist: " + artist);
    }
}