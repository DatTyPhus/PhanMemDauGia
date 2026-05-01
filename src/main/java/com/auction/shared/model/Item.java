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
    protected LocalDateTime createdAt;

    public Item(int sellerId, String itemName, String description,
                String category, BigDecimal startPrice, String imageUrl) {
        this.sellerId = sellerId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.startPrice = startPrice;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }
    public abstract void printInfo();
    // Getters and Setters
    public int getSellerId() {return sellerId;}
    public String getName() {return itemName;}
    public String getDescription() {return description;}
    public String getCategory() {return category;}
    public BigDecimal getStartingPrice() {return startPrice;}
    public String getImageUrl() {return imageUrl;}
    public LocalDateTime getCreatedAt() {return createdAt;}    
}