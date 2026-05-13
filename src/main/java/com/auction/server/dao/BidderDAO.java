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
  
    public Bidder getUserByUserid(int id) {
        String sql = "SELECT user_id, username, balance FROM bidders WHERE user_id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // gán giá triu id vào dấu chấn hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Bidder user = new Bidder();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("fullName"));
                user.setBalance(rs.getBigDecimal("balance"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }

    public Bidder selectByUsername(String username) {
        // PHẢI ĐỔI users THÀNH bidders
        String sql = "SELECT * FROM bidders WHERE username = ?";
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username); // Gán giá trị username vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Bidder user = new Bidder();
            user.setId(rs.getInt("user_id")); // Lưu ý: Tên cột nên khớp với DB (thường là user_id hoặc id)
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setFullName(rs.getString("full_name")); // Khớp với DB của bạn là full_name
            user.setBalance(rs.getBigDecimal("balance"));
            
            return user;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}
public void create(User obj) {

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

}
