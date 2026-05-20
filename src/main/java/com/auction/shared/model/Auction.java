package com.auction.shared.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
    private int itemId;
    private String itemName;
    private BigDecimal StartingPrice = BigDecimal.ZERO;
    private BigDecimal currentPrice = BigDecimal.ZERO;
    private Integer highestBidderId = null;
    private String startTime = null;
    private String endTime = null;
    private int durationMinutes;
    private String status = "OPEN";
    private final ReentrantLock lock = new ReentrantLock();

    public Auction() {}

    public Auction( int id, int itemId, String itemName, BigDecimal startingPrice, int durationMinutes) {
        this.id =id;
        this.itemId = itemId;
        this.StartingPrice = startingPrice;
        this.durationMinutes = durationMinutes;
    }

    public Auction(int itemId, String itemName, BigDecimal startingPrice, int durationMinutes) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.StartingPrice = startingPrice;
        this.durationMinutes = durationMinutes;
    }

    public synchronized void cancel() {
        status = "CANCELED";
        System.out.println("Auction canceled");
    }
    public void lock() {lock.lock();}
    public void unlock() {lock.unlock();}



    // Getter/Setter 
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getStartingPrice() { return StartingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.StartingPrice = startingPrice;}

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public Integer getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(Integer highestBidderId) { this.highestBidderId = highestBidderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}