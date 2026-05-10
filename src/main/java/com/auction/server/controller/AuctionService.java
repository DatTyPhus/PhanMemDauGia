/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.server.dao.BidderDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.SellerDAO;
import com.auction.shared.model.Auction;
import com.auction.shared.model.Bidder;
import com.auction.shared.network.Message;;

public class AuctionService {
  private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();
  private final ItemDAO itemDAO = new ItemDAO();
  private final BidderDAO bidderDAO = new BidderDAO();
  private final SellerDAO sellerDAO = new SellerDAO();

  //đao tạo đấu giá mới
  public void createAuction(int itemId, BigDecimal startingPrice, int durationMinutes) {
    Auction auction = new Auction(itemId, startingPrice, durationMinutes);
    activeAuctions.put(auction.getAuctionId(), auction);
  }

    //đặ bit giá cho một đấu giá cụ thể
  public Message processBid(int id, int auctionId, BigDecimal bidAmount ) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new Message("BID_FAIL", "Đấu giá không tồn tại.");
        }
        auction.lock();
        try{
          if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                return new Message("BID_FAIL", "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ").");
          }
          Bidder bidder = bidderDAO.getUserByUserid(id); 
          if (bidder.getBalance().compareTo(bidAmount) < 0) {
            return new Message("BID_FAIL", "Số dư không đủ.");
          }
          auction.setCurrentPrice(bidAmount);
          auction.setHighestBidderId(id);
          return new Message("BID_SUCCESS", "Đặt giá thành công.");
        } finally {
            auction.unlock();
        }
    }
}
