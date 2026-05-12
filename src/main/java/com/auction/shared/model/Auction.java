package com.auction.shared.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
    private int auctionId;
    private int itemId;
    private BigDecimal StartingPrice = BigDecimal.ZERO;
    private BigDecimal currentPrice = BigDecimal.ZERO;
    private Integer highestBidderId = null;
    private String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    private int durationMinutes;
    private Status status = Status.OPEN;
    private final ReentrantLock lock = new ReentrantLock();

    public enum Status {
        OPEN,
        RUNNING,
        FINISHED,
        CANCELED
    }

    public Auction( int id, int itemId, BigDecimal startingPrice, int durationMinutes) {
        this.id =id;
        this.itemId = itemId;
        this.StartingPrice = startingPrice;
        this.durationMinutes = durationMinutes;
    }
    public synchronized void close() {
        status = Status.FINISHED;

        System.out.println("Winner: " +(highestBidderId != null ? highestBidderId : "None"));
    }
    public synchronized void cancel() {
        status = Status.CANCELED;
        System.out.println("Auction canceled");
    }
    public void lock() {lock.lock();}
    public void unlock() {lock.unlock();}
    // Getter/Setter 
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public Integer getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(Integer highestBidderId) { this.highestBidderId = highestBidderId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getId() {return id;} 
    public int getAuctionId() { return auctionId; }
    public int getItemId() { return itemId; }
    public String getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
}