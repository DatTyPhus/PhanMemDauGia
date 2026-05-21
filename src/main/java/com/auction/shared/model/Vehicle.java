package com.auction.shared.model;

import java.math.BigDecimal;

public class Vehicle extends Item {
    private String brand; //thương hiệu
    private int itemId;

    public Vehicle(){
        super();
    }
    @Override
    public void printInfo() {
        System.out.println("Vehicle: " + itemName + " | Brand: " + brand + " | itemId:" + itemId);
    }
}