/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import java.math.BigDecimal;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.auction.server.dao.*;
import com.auction.shared.model.*;
import com.auction.shared.network.Message;

public class AuctionService {
    //các biến lưu thông số của Autobid hiện tại để xử lí cần xài k thì th
    private static BigDecimal autoBidAmount = BigDecimal.ZERO;
    private static String autoBidderName = null;
    private static BigDecimal autoBiddStep = BigDecimal.ZERO;


    private static int auctionIdCounter = 0; 
    private static Auction currentAuction; // Biến tĩnh để lưu phiên đấu giá hiện tại
    protected static Map<Integer, Auction> waitingAuctions; // Giả sử có một map để quản lý các đấu giá đang chờ xử lý

  //đao tạo đấu giá mới
  public static Auction createAuction(Item item) {
    Auction auction = new Auction();
    auction.setId(++auctionIdCounter); // Tăng counter và gán làm ID cho đấu giá mới
    auction.setItemId(item.getId());
    auction.setItemName(item.getName());
    auction.setCurrentPrice(item.getStartingPrice());
    auction.setCurrentPrice(item.getStartingPrice());
    auction.setDurationMinutes(item.getDurationMinutes());
    if (AuctionSchedular.getTimeline() == null || java.time.LocalDateTime.now().isAfter(AuctionSchedular.getTimeline())) {
        AuctionSchedular.setTimeline(java.time.LocalDateTime.now().plusMinutes(item.getDurationMinutes()));
        auction.setStartTime(auction.changeTimetoString(java.time.LocalDateTime.now()));
        auction.setEndTime(auction.changeTimetoString(java.time.LocalDateTime.now().plusMinutes(item.getDurationMinutes())));
    }
    else {
        auction.setStartTime(auction.changeTimetoString(AuctionSchedular.getTimeline()));
        auction.setEndTime(auction.changeTimetoString(AuctionSchedular.getTimeline().plusMinutes(item.getDurationMinutes())));
        AuctionSchedular.setTimeline(AuctionSchedular.getTimeline().plusMinutes(item.getDurationMinutes()));
    }
    auction.setStatus("PENDING");
    AuctionDAO.create(auction);
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


public static Message processAutoBid(BidTransaction bidTransaction) {
    Auction auction = AuctionDAO.selectById(bidTransaction.getAuctionId());
    // Xem xét các trg hợp ngoại lệ và không đạt tiêu chuẩn
    if (auction == null) {
        return new Message("AUTO_BID_FAIL", "Đấu giá không tồn tại.");
    }
    Bidder bidder = BidderDAO.selectByUsername(bidTransaction.getBiddername());
    if (bidder == null) {
        return new Message("AUTO_BID_FAIL", "Người đặt không tồn tại.");
    }
    if (bidder.getBalance().compareTo(bidTransaction.getBidAmount()) < 0) {
        return new Message("AUTO_BID_FAIL", "Số dư không đủ.");
    }
    if (bidTransaction.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
        return new Message("AUTO_BID_FAIL", "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ").");
    }
    if (bidTransaction.getBidAmount().compareTo(AuctionService.getAutoBidAmount()) <= 0) {
        return new Message("AUTO_BID_FAIL", "Giá đặt phải vượt quá mức đặt tự động đã thiết lập (" + AuctionService.getAutoBidAmount() + ").");
    }
    //coi như lần đầu đặt autobid là 1 cuộc đấu giá bth

    autoBidAmount = bidTransaction.getBidAmount();
    autoBidderName = bidTransaction.getBiddername();
    autoBiddStep = bidTransaction.getStep();
    BidTransaction bidTransaction2 = new BidTransaction(bidTransaction.getAuctionId(), bidTransaction.getBiddername(), auction.getCurrentPrice().add(bidTransaction.getStep()));
    processBid(bidTransaction2);

    return new Message("AUTO_BID_SUCCESS", bidTransaction);
}

    //đặ bit giá cho một đấu giá cụ thể
  public static Message processBid( BidTransaction bidTransaction) {
        Auction auction =  AuctionDAO.selectById(bidTransaction.getAuctionId());
        LocalDateTime now = LocalDateTime.now();
        int secondsBetween = (int) ChronoUnit.SECONDS.between(now, auction.changeStringToTime(auction.getEndTime()));
        if (auction == null) {
            return new Message("BID_FAIL", "Đấu giá không tồn tại.");
        }
        if (secondsBetween <=60){
            AuctionSchedular.delay(auction.getId(), 5);
            AuctionSchedular.delayFromAuction(auction.getId(), 5);
        }
        auction.lock();
        try{
          if (bidTransaction.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
                return new Message("BID_FAIL", "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ").");
          }
          Bidder bidder = BidderDAO.selectByUsername(bidTransaction.getBiddername());
          if (bidder.getBalance().compareTo(bidTransaction.getBidAmount()) < 0) {
            return new Message("BID_FAIL", "Số dư không đủ.");
          }
          // So sánh dữ liệu với các biến dùng để ám chỉ đến các thông số của Autobid này như là autobidAmount
          // trong trg hợp có người khác đặt bid nhỏ hơn autobid hiện tại
          if (bidTransaction.getBidAmount().compareTo(autoBidAmount)<=0){
            // nếu mà bược nhảy nhỏ hơn khoảng cách giữa đặt bid à auto bid thì lấy giá trị lơn nhất của autobid
            if (autoBidAmount.compareTo(bidTransaction.getBidAmount().add(autoBiddStep)) <=0){
               auction.setCurrentPrice(autoBidAmount);           
                return new Message("BID_FAILED", "Có người đặt giá tự động cao hơn bạn, giá tự dộng cập nhật là: "+ autoBidAmount);
            }
            //nếu mà bược nhảy lớn hơnd ...
            else{
                auction.setCurrentPrice(bidTransaction.getBidAmount().add(autoBiddStep));
                return new Message("BID_FAILED", "Có người đặt giá tự động cao hơn bạn, giá tự dộng cập nhật là: "+ bidTransaction.getBidAmount().add(autoBiddStep));
            }
          }
          //đặt bid bth
          Bidder previousHighestBidder = BidderDAO.selectByUsername(auction.getHighestBidderName());
          auction.setCurrentPrice(bidTransaction.getBidAmount());
          auction.setHighestBidderName(bidTransaction.getBiddername());
          return new Message("BID_SUCCESS", previousHighestBidder);
        } finally {
            auction.unlock();
        }
    }


    public static void setCurrentAuction(Auction auction) {currentAuction = auction;}
    public static Auction getCurrentAuction() {return currentAuction;}

    public static BigDecimal getAutoBidAmount() { return autoBidAmount; }
    public static void setAutoBidAmount(BigDecimal amount) { autoBidAmount = amount; }
    public static String getAutoBidderName() { return autoBidderName; }
    public static void setAutoBidderName(String name) { autoBidderName = name; }
    public static BigDecimal getAutoBidStep() { return autoBiddStep; }
    public static void setAutoBidStep(BigDecimal step) { autoBiddStep = step; }
}
