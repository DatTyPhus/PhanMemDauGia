package com.auction.shared.model;

import java.math.BigDecimal;

public class Vehicle extends Item {
    private String brand; //thương hiệu
    private int itemId;

    public Vehicle(int sellerId, String itemName, String description,
                   BigDecimal startPrice, String imageUrl, String brand) {
        super(sellerId, itemName, description, "Vehicle", startPrice, imageUrl);
        this.brand = brand;
        this.itemId=getId();                         //mỗi item có một id, lưu vào biến itemId
    }

    @Override
    public void printInfo() {
        System.out.println("Vehicle: " + itemName + " | Brand: " + brand + " | itemId:" + itemId);
    }
}