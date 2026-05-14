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

  public Message login(String username, String password) {
    Bidder bidder = bidderDAO.selectByUsername(username);
    Seller seller = sellerDAO.selectByUsername(username);

    // 1. Nếu tìm thấy trong bảng Bidder
    if (bidder != null) {
      if (bidder.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", bidder);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Bidder.");
      }
    }
    // 2. Nếu tìm thấy trong bảng Seller
    else if (seller != null) {
      if (seller.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", seller);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Seller.");
      }
    }
    // 3. Nếu không tìm thấy ở cả 2 bảng
    else {
      return new Message("LOGIN_FAIL", "Tài khoản không tồn tại.");
    }
  }

  public Message register(String username, String password, String fullName , String role) {
    System.out.println("\n=== SERVER ĐANG XỬ LÝ ĐĂNG KÝ ===");
    System.out.println("Role nhận được từ Client: [" + role + "]");

    if (role == null) {
      return new Message("REGISTER_FAIL", "Lỗi: Role gửi lên bị trống!");
    }

    // Xóa khoảng trắng thừa và không phân biệt hoa thường
    if (role.trim().equalsIgnoreCase("BIDDER")) {
      System.out.println("-> Đang nhảy vào luồng BIDDER...");
      if (bidderDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
      }
      Bidder newUser = new Bidder(username, password, fullName, "BIDDER");
      bidderDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", newUser);

    } else if (role.trim().equalsIgnoreCase("SELLER")) {
      System.out.println("-> Đang nhảy vào luồng SELLER...");
      if (sellerDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
      }
      Seller newUser = new Seller(username, password, fullName, "SELLER");
      sellerDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", newUser);

    } else {
      return new Message("REGISTER_FAIL", "Lỗi gửi sai vai trò: " + role);
    }
  }
}
