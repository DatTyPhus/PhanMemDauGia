package com.auction.server.dao;

import com.auction.shared.model.Seller;
import com.auction.shared.model.User;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// sửa tên trên database thành seller
public class SellerDAO {
    public static SellerDAO instance() {
        return new SellerDAO();
    }
    
    public static void create(User obj) {

      String sql = "INSERT INTO sellers (username, password, full_name, role) VALUES ('"
                + obj.getUsername() + "', '"
                + obj.getPassword() + "', '"
                + obj.getFullName() + "', '"
                + obj.getRole() + "') ";
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

    /// Hàm lấy thông tin seller bằng ID (Đã sửa lại tên cột cho khớp với database)
    public static Seller getSellersByUserid(int id) {
        // Chỉ lấy user_id, username, balance
        String sql = "SELECT user_id, username, balance FROM sellers WHERE user_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Seller user = new Seller();
                // BẮT BUỘC: Lấy đúng tên cột là user_id từ database
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setBalance(rs.getBigDecimal("balance"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }

    /// Hàm này để cộng thêm tiền vào tài khoản Seller dưới database
    public static boolean updateBalance(int userId, java.math.BigDecimal amountToAdd) {
        String sql = "UPDATE sellers SET balance = balance + ? WHERE user_id = ?";
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
    
    public static Seller selectByUsername(String username) {
    String sql = "SELECT * FROM sellers WHERE username = ?";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username); // Gán giá trị username vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Seller user = new Seller();
            user.setId(rs.getInt("user_id")); // Lưu ý: Tên cột nên khớp với DB (thường là user_id hoặc id)
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setFullName(rs.getString("full_name")); // Khớp với DB của bạn là full_name
            user.setBalance(rs.getBigDecimal("balance"));
            user.setRole("Seller");

            return user;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}

}
