package com.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
    protected static Integer id;
    private int auctionId;
    private int itemId;
    private BigDecimal StartingPrice;
    private BigDecimal currentPrice;
    private Integer highestBidderId;
    private LocalDateTime startTime = LocalDateTime.now();
    private int durationMinutes;
    private Status status;
    private final ReentrantLock lock = new ReentrantLock();

    public enum Status {
        OPEN,
        RUNNING,
        FINISHED,
        CANCELED
    }

    public Auction(int itemId, BigDecimal startingPrice, int durationMinutes) {
        super();
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
    public LocalDateTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
}