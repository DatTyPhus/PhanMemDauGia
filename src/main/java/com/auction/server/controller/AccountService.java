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
  private final BidderDAO bidderDAO = new BidderDAO();
  private final SellerDAO sellerDAO = new SellerDAO();
  private final AdminDAO adminDAO = new AdminDAO();

  public Message login(String username, String password) {

    // 1. Kiểm tra bên kho Bidder
    Bidder bidder = bidderDAO.selectByUsername(username);
    if (bidder != null) {
      if (bidder.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", bidder);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Bidder.");
      }
    }

    // 2. Kiểm tra bên kho Seller
    Seller seller = sellerDAO.selectByUsername(username);
    if (seller != null) {
      if (seller.getPassword().equals(password)) {
        return new Message("LOGIN_SUCCESS", seller);
      } else {
        return new Message("LOGIN_FAIL", "Sai mật khẩu Seller.");
      }
    }

    // 3. Kiểm tra bên kho Admin
    Admin admin = adminDAO.selectByUsername(username);
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

  /// Hàm xử lý logic đăng ký tài khoản từ Client gửi lên
  public Message register(String username, String password, String fullName, String role) {
    System.out.println("-> Bắt đầu xử lý đăng ký cho username: [" + username + "] với vai trò: [" + role + "]");

    if (role == null || role.trim().isEmpty()) {
      return new Message("REGISTER_FAIL", "Lỗi: Vai trò (Role) không được để trống!");
    }

    /// [FIX BUG BẢO MẬT QUAN TRỌNG]: Quét kiểm tra username trên TOÀN BỘ CÁC KHO (Bidder, Seller, Admin)
    /// Phải đảm bảo tên đăng nhập này là DUY NHẤT trên toàn hệ thống, không phân biệt vai trò.
    boolean isUsernameExist = (bidderDAO.selectByUsername(username) != null)
            || (sellerDAO.selectByUsername(username) != null)
            || (adminDAO.selectByUsername(username) != null);

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

      bidderDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", "Đăng ký tài khoản Người mua (Bidder) thành công!");

    } else if (role.trim().equalsIgnoreCase("SELLER")) {
      System.out.println("-> Tiến hành khởi tạo tài khoản vào kho SELLER...");
      Seller newUser = new Seller();
      newUser.setUsername(username);
      newUser.setPassword(password);
      newUser.setFullName(fullName);
      newUser.setRole("SELLER");

      sellerDAO.create(newUser);
      return new Message("REGISTER_SUCCESS", "Đăng ký tài khoản Người bán (Seller) thành công!");

    } else {
      return new Message("REGISTER_FAIL", "Lỗi Server: Nhận diện vai trò không hợp lệ!");
    }
  }
}