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

  public Message login(String username, String password) {

    // 1. Kiểm tra bên kho Bidder
    Bidder bidder = BidderDAO.selectByUsername(username);
    if (bidder != null) {
      if (bidder.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", bidder);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu!");
      }
    }

    // 2. Kiểm tra bên kho Seller
    Seller seller = SellerDAO.selectByUsername(username);
    if (seller != null) {
      if (seller.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", seller);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu!");
      }
    }

    // 3. Kiểm tra bên kho Admin
    Admin admin = AdminDAO.selectByUsername(username);
    if (admin != null) {
      if (admin.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", admin);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu!");
      }
    }

    // 4. Nếu tìm cả 3 kho đều không thấy
    return new Message("LOGIN_FAIL", "Sai tên đăng nhập hoặc tài khoản không tồn tại.");
  }

  /// Hàm xử lý logic đăng ký tài khoản từ Client gửi xuống
  public Message register(String username, String password, String fullName, String role) {
    System.out.println("-> Bắt đầu xử lý đăng ký cho username: [" + username + "] với vai trò: [" + role + "]");

    if (role == null || role.trim().isEmpty()) {
      return new Message("REGISTER_FAIL", "Lỗi: Vai trò (Role) không được để trống!");
    }

    /// Quét kiểm tra username trên TOÀN BỘ CÁC KHO (Bidder, Seller, Admin)
    /// Phải đảm bảo tên đăng nhập này là DUY NHẤT trên toàn hệ thống, không phân biệt vai trò.

    boolean isUsernameExist = (BidderDAO.selectByUsername(username) != null)
                            || (SellerDAO.selectByUsername(username) != null)
                            || (AdminDAO.selectByUsername(username) != null);

    if (isUsernameExist) {
      System.out.println("-> CẢNH BÁO TỪ CHỐI: Username [" + username + "] đã bị người khác sử dụng trong hệ thống.");
      return new Message("REGISTER_FAIL", "Tên đăng nhập đã tồn tại trong hệ thống. Vui lòng chọn tên khác!");
    }

    /// Nếu vượt qua bài kiểm tra trùng lặp (Cả 3 kho đều null), tiến hành phân luồng tạo tài khoản
    if (role.trim().equalsIgnoreCase("BIDDER")) {
      System.out.println("-> Tiến hành khởi tạo tài khoản vào kho BIDDER...");
      Bidder newUser = new Bidder();
      newUser.setUsername(username);
      newUser.setPassword(password);
      newUser.setFullName(fullName);
      newUser.setRole("BIDDER");

      BidderDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", "Đăng ký tài khoản Người mua (Bidder) thành công!");

    } else if (role.trim().equalsIgnoreCase("SELLER")) {
      System.out.println("-> Tiến hành khởi tạo tài khoản vào kho SELLER...");
      Seller newUser = new Seller();
      newUser.setUsername(username);
      newUser.setPassword(password);
      newUser.setFullName(fullName);
      newUser.setRole("SELLER");

      SellerDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", "Đăng ký tài khoản Người bán (Seller) thành công!");

    } else {
      return new Message("REGISTER_FAIL", "Lỗi Server: Nhận diện vai trò không hợp lệ!");
    }
  }
}