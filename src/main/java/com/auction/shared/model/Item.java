package com.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Item extends Entity {
    protected int sellerId; 
    protected String itemName;  
    protected String description;  
    protected BigDecimal startPrice;  
    protected String imageUrl;  
    protected String itemType;  
    protected int durationMinutes;
    protected String status; // Trạng thái đặc biệt, có thể dùng để lưu thông tin riêng cho từng loại item, ví dụ như "condition" cho đồ điện tử, "artist" cho tranh, v.v.
    protected String special_info;


    public static Item createFromType(String type) {
        return switch (type) {
            case "ART"         -> new Art();
            case "ELECTRONIC"  -> new Electronics();
            case "VEHICLE"     -> new Vehicle();
            default -> throw new IllegalArgumentException("Loại item không hợp lệ: " + type);
        };
    }

    public Item(int sellerId, String itemName, String description,
                String itemType, BigDecimal startPrice, String imageUrl) {
        this.sellerId = sellerId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.description = description;
        this.startPrice = startPrice;
        this.imageUrl = imageUrl;
    }

    public Item() {
    }

    // Getters and Setters
    public int getSellerId() {return sellerId;}
    public String getName() {return itemName;}
    public String getDescription() {return description;}
    public BigDecimal getStartingPrice() {return startPrice;}
    public String getImageUrl() {return imageUrl;}
    public String getItemType() {return itemType;}
    public int getDurationMinutes() {return durationMinutes;}
    public String getStatus() {return special_info;}
    
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
    public void setName(String itemName) {this.itemName = itemName;}
    public void setDescription(String description) {this.description = description;}
    public void setStartingPrice(BigDecimal startPrice) {this.startPrice = startPrice;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public void setItemType(String itemType) {this.itemType = itemType;}
    public void setDurationMinutes(int durationMinutes) {this.durationMinutes = durationMinutes;}
    public void setStatus(String status) {this.special_info = status;}

}