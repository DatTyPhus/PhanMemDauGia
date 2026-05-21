package com.auction.shared.model;


import java.math.BigDecimal;
public class Electronics extends Item {
    private String brand;    //thương hiệu
    private int warrantyMonths;       //bảo hành
    private int itemId;

    
    public Electronics(){
        super();
    }

    @Override
    public void printInfo() {
        System.out.println("Electronic Item: " + itemName + " | Brand: " + brand + " | itemId:" + itemId);
    }
}