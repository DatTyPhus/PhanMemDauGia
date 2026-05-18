package com.auction.shared.model;

public interface AuctionObserver {
    // Hàm này sẽ tự động kích hoạt khi có ai đó đặt giá mới thành công
    void onBidUpdated(int productId, double newPrice, String bidderName);
}