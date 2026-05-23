package com.auction.server.dao;

import com.auction.shared.model.Bidder;
import com.auction.shared.model.User;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// sửa tên trên database thành bidder
public class BidderDAO {
    public static BidderDAO instance() {
        return new BidderDAO();
    }

    /// Hàm lấy thông tin user bằng ID (Đã sửa lại tên cột cho khớp với database)
    public static Bidder getUserByUserid(int id) {
        // Sửa chữ 'id' thành 'user_id' cho khớp với cấu trúc bảng bidders
        String sql = "SELECT user_id, username, balance FROM bidders WHERE user_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // gán giá trị id vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Bidder user = new Bidder();
                // BẮT BUỘC: Lấy đúng tên cột là user_id từ database
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                // Bỏ phần set password và fullName vì câu SQL trên không SELECT 2 cột này,
                // chỉ cần lấy balance để phục vụ việc hiển thị số dư là đủ.
                user.setBalance(rs.getBigDecimal("balance"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }

    /// Hàm tìm kiếm Bidder theo tên đăng nhập (username) để kiểm tra trùng lặp khi đăng ký
    public static Bidder selectByUsername(String username) {
        String sql = "SELECT * FROM bidders WHERE username = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Bidder user = new Bidder();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setRole("BIDDER");
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; /// Trả về null nếu tên đăng nhập này chưa ai dùng
    }
public static void create(User obj) {

      String sql = "INSERT INTO bidders (username, password, full_name, role) VALUES ('"
                + obj.getUsername() + "', '"
                + obj.getPassword() + "', '"
                + obj.getFullName() + "', '"
                + obj.getRole() + "')";
      Connection connection = null;
      try{
          connection = JDBCUtil.getConnection();
          Statement st= connection.createStatement();

          int kq = st.executeUpdate(sql);
          if (kq > 0) {
            System.out.println("Them nguoi dung thanh cong!");
          } else {
            System.out.println("Them that bai, vui long kiem tra lai du lieu.");
          }
          JDBCUtil.closeConnection(connection);
      } catch(Exception e){
        e.printStackTrace();
      }
    }
    /// Hàm này để cộng thêm tiền vào tài khoản dưới database
    public static boolean updateBalance(int userId, java.math.BigDecimal amountToAdd) {
        String sql = "UPDATE bidders SET balance = balance + ? WHERE user_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, amountToAdd);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu update thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /// Hàm lấy toàn bộ danh sách Bidder từ database phục vụ màn hình Admin
    public static java.util.List<com.auction.shared.model.Bidder> getAllBidders() {
        java.util.List<com.auction.shared.model.Bidder> list = new java.util.ArrayList<>();
        String sql = "SELECT user_id, username, full_name, balance FROM bidders";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                com.auction.shared.model.Bidder user = new com.auction.shared.model.Bidder();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setRole("BIDDER");
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /// Hàm thực hiện xóa vĩnh viễn tài khoản Bidder khỏi database dựa vào ID
    public static boolean deleteBidder(int userId) {
        String sql = "DELETE FROM bidders WHERE user_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
