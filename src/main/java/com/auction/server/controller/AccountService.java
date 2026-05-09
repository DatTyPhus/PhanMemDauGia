/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import com.auction.server.dao.SellerDAO;
import com.auction.server.dao.BidderDAO;
import com.auction.shared.model.Bidder;
import com.auction.shared.model.Seller;
import com.auction.shared.network.Message;;

public class AccountService {
  private final BidderDAO bidderDAO = new BidderDAO();
  private final SellerDAO sellerDAO = new SellerDAO();
  
  public Message login (String username, String password) {
    Bidder user = bidderDAO.selectByUsername(username);
    Seller seller = sellerDAO.selectByUsername(username);
    if (user == null && seller == null) {
      return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc mật khẩu.");
    }
    if (!user.getPassword().equals(password) && !seller.getPassword().equals(password)) {
      return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc mật khẩu.");
    }
    if (user != null) {
      return new Message("LOGIN_SUCCESS", user);
    }
    return new Message("LOGIN_SUCCESS", seller);
  }

  public Message register(String username, String password, String fullName , String role) {
    if (role == "BIDDER") {
      if (bidderDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
      }
      Bidder newUser = new Bidder(username, password, fullName);
      bidderDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", newUser);
    } 
    if (sellerDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
    }
    Seller newUser = new Seller(username, password, fullName);
    sellerDAO.create(newUser);
    return new Message("REGISTER_SUCCESS", newUser);
    } 
  }
