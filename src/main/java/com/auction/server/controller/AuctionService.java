/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.shared.model.Auction;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;;

public class AuctionService {
  private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();
  private final ItemDAO itemDAO = new ItemDAO();
  private final UserDAO userDAO = new UserDAO();

  //đao tạo đấu giá mới
  public void createAuction(int itemId, BigDecimal startingPrice, int durationMinutes) {
    Auction auction = new Auction(itemId, startingPrice, durationMinutes);
    activeAuctions.put(auction.getAuctionId(), auction);
  }

    //đặ bit giá cho một đấu giá cụ thể
  public Message processBid(int id, int auctionId, BigDecimal bidAmount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new Message("BID_FAIL", "Đấu giá không tồn tại.");
        }
        auction.lock();
        try{
          if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                return new Message("BID_FAIL", "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ").");
          }
          User bidder = userDAO.getUserByUserid(id); 
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
  
    public Message login (String username, String password) {
      User user = userDAO.selectByUsername(username);
      if (user == null) {
        return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc mật khẩu."); 
      }
      if (!user.getPassword().equals(password)) {
        return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc mật khẩu.");
      }
      return new Message("LOGIN_SUCCESS", user );
    }
}
