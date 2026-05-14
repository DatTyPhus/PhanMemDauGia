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
  private final ItemDAO itemDAO = new ItemDAO();
  private final BidderDAO bidderDAO = new BidderDAO();
  private final SellerDAO sellerDAO = new SellerDAO();
  private final AuctionDAO auctionDAO = new AuctionDAO();
  public static Map<Integer, Auction> waitingAuctions; // Giả sử có một map để quản lý các đấu giá đang chờ xử lý

  //đao tạo đấu giá mới
  public Message createAuction(String itemName, BigDecimal startingPrice, int durationMinutes) {
    Item item = itemDAO.selectByName(itemName);
    Auction auction = new Auction(item.getId(), itemName, startingPrice, durationMinutes);
    waitingAuctions.put(auction.getId(), auction);
    return new Message("ADD_ITEM_REQUEST", item);
  }

    //đặ bit giá cho một đấu giá cụ thể
  public Message processBid(String bidder_name, String item_name, BigDecimal bidAmount ) {
        Auction auction =  auctionDAO.selectByItemName(item_name);
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
          auction.setCurrentPrice(bidAmount);
          auction.setHighestBidderId(auction.getId());
          return new Message("BID_SUCCESS", "Đặt giá thành công.");
        } finally {
            auction.unlock();
        }
    }
}
