/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import com.auction.server.dao.*;
import com.auction.shared.model.*;
import com.auction.shared.network.Message;

public class AccountService {

  // 3 DÒNG NÀY LÀ CỰC KỲ QUAN TRỌNG ĐỂ JAVA NHẬN DIỆN ĐƯỢC DAO (BẠN ĐÃ LỠ XÓA MẤT NÓ)

  public Message login(String username, String password) {

    // 1. Kiểm tra bên kho Bidder
    Bidder bidder = BidderDAO.selectByUsername(username);
    if (bidder != null) {
      if (bidder.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", bidder);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Bidder.");
      }
    }

    // 2. Kiểm tra bên kho Seller
    Seller seller = SellerDAO.selectByUsername(username);
    if (seller != null) {
      if (seller.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", seller);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Seller.");
      }
    }

    // 3. Kiểm tra bên kho Admin
    Admin admin = AdminDAO.selectByUsername(username);
    if (admin != null) {
      if (admin.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", admin);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Admin.");
      }
    }

    // 4. Nếu tìm cả 3 kho đều không thấy
    return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc tài khoản không tồn tại.");
  }

  public Message register(String username, String password, String fullName , String role) {
    System.out.println("\n=== SERVER ĐANG XỬ LÝ ĐĂNG KÝ ===");
    System.out.println("Role nhận được từ Client: [" + role + "]");
    if (role == null) {
      return new Message("REGISTER_FAIL", "Lỗi: Role gửi lên bị trống!");
    }

    // [SỬA] Check username trùng trên CẢ 3 KHO trước khi tạo bất kỳ role nào.
    // Lý do: nếu chỉ check riêng từng kho, username "Lan" có thể tồn tại đồng thời
    // ở cả Bidder lẫn Seller, khiến login không biết trả về ai.
    boolean usernameExists = BidderDAO.selectByUsername(username) != null
            || SellerDAO.selectByUsername(username) != null
            || AdminDAO.selectByUsername(username) != null;           

    if (usernameExists) {
      System.out.println("-> Username [" + username + "] đã tồn tại trong hệ thống.");
      return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
    }

    // Xóa khoảng trắng thừa và không phân biệt hoa thường
    if (role.trim().equalsIgnoreCase("BIDDER")) {
      System.out.println("-> Đang nhảy vào luồng BIDDER...");
      if (BidderDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
      }
      Bidder newUser = new Bidder(username, password, fullName, "BIDDER");
      BidderDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", newUser);

    } else if (role.trim().equalsIgnoreCase("SELLER")) {
      System.out.println("-> Đang nhảy vào luồng SELLER...");
      if (SellerDAO.selectByUsername(username) != null) {
        return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại.");
      }
      Seller newUser = new Seller(username, password, fullName, "SELLER");
      SellerDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", newUser);

    } else {
      return new Message("REGISTER_FAIL", "Lỗi gửi sai vai trò: " + role);
    }
  }
}