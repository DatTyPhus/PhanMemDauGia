package com.auction.shared.model;
import com.auction.shared.model.Entity;
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

    public Item(int itemId, int sellerId, String itemName, String description,
                String category, BigDecimal startPrice, String imageUrl) {
        this.id = itemId;
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
}