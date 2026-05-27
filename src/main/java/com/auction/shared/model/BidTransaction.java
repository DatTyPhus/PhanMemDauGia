package com.auction.shared.model;

import java.math.BigDecimal;
//BidTransaction: lịch sử đặt giá

public class BidTransaction {
    private int auctionId;
    private String biddername;
    private BigDecimal bidAmount;
    private BigDecimal step;

    //tạo mới một lượt đặt giá trong một phiên đấu giá
    public BidTransaction(int auctionId, String biddername, BigDecimal bidAmount) {
        this.auctionId = auctionId;     //id phiên đấu giá
        this.biddername = biddername;         //khách hàng
        this.bidAmount = bidAmount;        //số tiền đặt
    }

    public BidTransaction(int auctionId, String biddername, BigDecimal bidAmount, BigDecimal step) {
        this.auctionId = auctionId;     //id phiên đấu giá
        this.biddername = biddername;         //khách hàng
        this.bidAmount = bidAmount;        //số tiền đặt
        this.step = step;                  //bước giá
    }

    public BidTransaction() {
        // Constructor mặc định
    }
    // Getter và Setter
    public int getAuctionId() { return auctionId; }
    public String getBiddername() { return biddername; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public BigDecimal getStep() { return step; }

    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }
    public void setBiddername(String biddername) { this.biddername = biddername; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    public void setStep(BigDecimal step) { this.step = step; }
}