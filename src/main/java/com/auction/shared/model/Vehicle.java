package com.auction.shared.model;

import java.math.BigDecimal;
import com.auction.shared.model.Item;

public class Vehicle extends Item {
    private String model;
    private String brand; //thương hiệu

    public Vehicle(int itemId, int sellerId, String itemName, String description,
                   BigDecimal startPrice, String imageUrl, String brand) {
        super(itemId, sellerId, itemName, description, "Vehicle", startPrice, imageUrl);
        this.brand = brand;
    }

    @Override
    public void printInfo() {
        System.out.println("Vehicle: " + itemName + " | Brand: " + brand);
    }
}