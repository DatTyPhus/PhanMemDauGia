/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import java.math.BigDecimal;
import java.util.Map;

import com.auction.server.dao.*;
import com.auction.shared.model.*;
import com.auction.shared.network.Message;;

public class AuctionService {
  private static int auctionIdCounter = 0; 
  protected static Auction currentAuction; // Biến để lưu trữ đấu giá hiện tại đang diễn ra
  private final BidderDAO bidderDAO = new BidderDAO();
  private final AuctionDAO auctionDAO = new AuctionDAO();
  private final AuctionSchedular auctionSchedular = new AuctionSchedular();
  protected static Map<Integer, Auction> waitingAuctions; // Giả sử có một map để quản lý các đấu giá đang chờ xử lý

  //đao tạo đấu giá mới
  public Auction createAuction(Item item) {
    Auction auction = new Auction();
    auction.setId(++auctionIdCounter); // Tăng counter và gán làm ID cho đấu giá mới
    auction.setItemId(item.getId());
    auction.setItemName(item.getName());
    auction.setCurrentPrice(item.getStartingPrice());
    auction.setCurrentPrice(item.getStartingPrice());
    auction.setDurationMinutes(item.getDurationMinutes());
    if (auctionSchedular.getTimeline() == null || java.time.LocalDateTime.now().isAfter(auctionSchedular.getTimeline())) {
        auctionSchedular.setTimeline(java.time.LocalDateTime.now().plusMinutes(item.getDurationMinutes()));
        auction.setStartTime(auction.changeTimetoString(java.time.LocalDateTime.now()));
        auction.setEndTime(auction.changeTimetoString(java.time.LocalDateTime.now().plusMinutes(item.getDurationMinutes())));
    }
    else {
        auction.setStartTime(auction.changeTimetoString(auctionSchedular.getTimeline()));
        auction.setEndTime(auction.changeTimetoString(auctionSchedular.getTimeline().plusMinutes(item.getDurationMinutes())));
        auctionSchedular.setTimeline(auctionSchedular.getTimeline().plusMinutes(item.getDurationMinutes()));
    }
    return auction;
  }
// public void updateTimeLine (Auction auction) {
//     LocalDateTime endTime = null;
//     LocalDateTime startTime =null;
//     if (timeline == null || LocalDateTime.now().isAfter(timeline)) {
//         timeline = LocalDateTime.now().plusMinutes(auction.getDurationMinutes());
//     }
//     else{
//         startTime = timeline;
//         endTime = timeline.plusMinutes(auction.getDurationMinutes());
//     } 
//     String startTimeStr = auction.changeTimetoString(startTime);
//     String endTimeStr = auction.changeTimetoString(endTime);
//     auctionDAO.updateDateTime(auction, startTimeStr, endTimeStr);
//   }

    //đặ bit giá cho một đấu giá cụ thể
  public Message processBid(String bidder_name, BigDecimal bidAmount ) {
        Auction auction =  auctionDAO.selectByItemName(currentAuction.getItemName());
        if (auction == null) {
            return new Message("BID_FAIL", "Đấu giá không tồn tại.");
        }
        auction.lock();
        try{
          if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                return new Message("BID_FAIL", "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ").");
          }
          Bidder bidder = bidderDAO.selectByUsername(bidder_name);
          if (bidder.getBalance().compareTo(bidAmount) < 0) {
            return new Message("BID_FAIL", "Số dư không đủ.");
          }
          Bidder previousHighestBidder = bidderDAO.selectByUsername(auction.getHighestBidderName());
          auction.setCurrentPrice(bidAmount);
          auction.setHighestBidderName(bidder_name);
          return new Message("BID_SUCCESS", previousHighestBidder);
        } finally {
            auction.unlock();
        }
    }


    public void setCurrentAuction(Auction auction) {
        currentAuction = auction;
    }
    public Auction getCurrentAuction() {
        return currentAuction;
    }
}
