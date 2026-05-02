package com.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//BidTransaction: lịch sử đặt giá

public class BidTransaction {
    private int auctionId;
    private int bidderId;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;

    public BidTransaction( int auctionId, int bidderId, BigDecimal bidAmount, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    //phần này để lưu lịch sử giao dịch
    // khi gọi constructor 3 tham số ở dưới, hệ thống sẽ lưu vào database thành 1 hàng gồm auctionid, bidderid, bidamount và bidtime
    //mỗi hàng nhận được sẽ gán vào constructor 4 tham số này để hiển thị ra màn hình
    }

    //tạo mới một lượt đặt giá trong một phiên đấu giá
    public BidTransaction(int auctionId, int bidderId, BigDecimal bidAmount) {
        this.auctionId = auctionId;     //id phiên đấu giá
        this.bidderId = bidderId;         //id khách hàng
        this.bidAmount = bidAmount;        //số tiền đặt
        this.bidTime = LocalDateTime.now();
    }

    // Getter và Setter
    public int getAuctionId() { return auctionId; }
    public int getBidderId() { return bidderId; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public LocalDateTime getBidTime() { return bidTime; }

    @Override
    public String toString() {          //in ra thông tin
        return String.format("Bid[Auction: %d, User: %d, Amount: %s, Time: %s]",
                auctionId, bidderId, bidAmount, bidTime);
    }
}