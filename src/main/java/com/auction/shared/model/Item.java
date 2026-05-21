package com.auction.shared.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Item extends Entity {
    protected int sellerId;
    protected String itemName;
    protected String description;
    protected String category;
    protected BigDecimal startPrice;
    protected String imageUrl;
    protected String itemType;
    protected int durationMinutes;

    public static Item createFromType(String type) {
        return switch (type) {
            case "ARTS"         -> new Art();
            case "ELECTRONICS"  -> new Electronics();
            case "VEHICLES"     -> new Vehicle();
            default -> throw new IllegalArgumentException("Loại item không hợp lệ: " + type);
        };
    }

    // public Item(int sellerId, String itemName, String description,
    //             String category, BigDecimal startPrice, String imageUrl) {
    //     super();
    //     this.sellerId = sellerId;
    //     this.itemName = itemName;
    //     this.description = description;
    //     this.category = category;
    //     this.startPrice = startPrice;
    //     this.imageUrl = imageUrl;
    //     this.createdAt = LocalDateTime.now();
    // }

    public Item() {
    }

    public abstract void printInfo();
    // Getters and Setters
    public int getSellerId() {return sellerId;}
    public String getName() {return itemName;}
    public String getDescription() {return description;}
    public String getCategory() {return category;}
    public BigDecimal getStartingPrice() {return startPrice;}
    public String getImageUrl() {return imageUrl;}
    public String getItemType() {return itemType;}
    public int getDurationMinutes() {return durationMinutes;}
    
    public void setSellerId(int sellerId) {this.sellerId = sellerId;}
    public void setName(String itemName) {this.itemName = itemName;}
    public void setDescription(String description) {this.description = description;}
    public void setCategory(String category) {this.category = category;}
    public void setStartingPrice(BigDecimal startPrice) {this.startPrice = startPrice;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public void setItemType(String itemType) {this.itemType = itemType;}
    public void setDurationMinutes(int durationMinutes) {this.durationMinutes = durationMinutes;}

}