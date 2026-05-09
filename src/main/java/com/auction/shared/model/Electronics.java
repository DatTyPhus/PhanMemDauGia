package com.auction.shared.model;


import java.math.BigDecimal;
public class Electronics extends Item {
    private String brand;    //thương hiệu
    private int warrantyMonths;       //bảo hành
    private int itemId;

    public Electronics(int sellerId, String itemName, String description,
                       BigDecimal startPrice, String imageUrl, String brand, int warrantyMonths) {
        super(sellerId, itemName, description, "Electronics", startPrice, imageUrl);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
        this.itemId = getId();                       //mỗi item có một id, lưu vào biến itemId
    }
    public Electronics(){
        super();
    }

    @Override
    public void printInfo() {
        System.out.println("Electronic Item: " + itemName + " | Brand: " + brand + " | itemId:" + itemId);
    }
}