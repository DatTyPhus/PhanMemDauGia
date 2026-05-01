package com.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction extends Entity {
    protected static Integer id;
    private int auctionId;
    private int itemId;
    private BigDecimal currentPrice;
    private Integer highestBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private LocalDateTime createdAt;

    public enum Status {
        OPEN,
        RUNNING,
        FINISHED,
        PAID,
        CANCELED
    }

public Auction(int auctionId, int itemId, BigDecimal currentPrice,
               Integer highestBidderId, LocalDateTime startTime,
               LocalDateTime endTime, Status status,
               LocalDateTime createdAt) {
    id=id+1;
    this.auctionId = auctionId;
    this.itemId = itemId;
    this.currentPrice = currentPrice;
    this.highestBidderId = highestBidderId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = status;
    this.createdAt = createdAt;
}

    // ================= BID =================
    public synchronized void placeBid(Bidder bidder, double amount) {

        if (status != Status.OPEN && status != Status.RUNNING) {
            System.out.println("Auction closed!");
            return;
        }

        BigDecimal bidAmount = BigDecimal.valueOf(amount);

        if (currentPrice != null && bidAmount.compareTo(currentPrice) <= 0) {
            System.out.println("Bid must be higher than current price!");
            return;
        }

        // cập nhật giá và người thắng
        currentPrice = bidAmount;
        highestBidderId = bidder.getId();

        System.out.println(bidder.getUsername() + " bid: " + amount);
    }

    // ================= CLOSE =================
    public synchronized void close() {
        status = Status.FINISHED;

        System.out.println("Winner: " +
                (highestBidderId != null ? highestBidderId : "None"));
    }

    // ================= CANCEL =================
    public synchronized void cancel() {
        status = Status.CANCELED;
        System.out.println("Auction canceled");
    }

    // Getter/Setter 
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public Integer getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(Integer highestBidderId) { this.highestBidderId = highestBidderId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getId() {return id;} 
    public int getAuctionId() { return auctionId; }
    public int getItemId() { return itemId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}