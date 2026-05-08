package com.auction.server.dao;

import com.auction.shared.model.Bidder;
import com.auction.shared.model.User;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO implements DAOinterface<User> {
    public static UserDAO instance() {
        return new UserDAO();
    }
  
    @Override
    public void create(User obj) {

      String sql = "INSERT INTO users (user_id, username, password, full_name, role, balance, created_at) VALUES ('"
                + obj.getId() + "', '"
                + obj.getUsername() + "', '"
                + obj.getPassword() + "', '"
                + obj.getFullName() + "', '"
                + obj.getRole() + "', "
                + obj.getBalance() + ", '"
                + obj.getCreatedAt().toString() + "')";
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

    @Override
    public User read(Integer id) {
        return null;
    }

    public User getUserByUserid(int id) {
        String sql = "SELECT id, username, balance FROM users WHERE id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // gán giá triu id vào dấu chấn hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("fullName"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }
    @Override
    public void update(User obj) {
        String sql = "UPDATE users SET username = '" + obj.getUsername() + "', "
                + "password = '" + obj.getPassword() + "', "
                + "full_name = '" + obj.getFullName() + "', "
                + "role = '" + obj.getRole() + "', "
                + "balance = " + obj.getBalance() + ", "
                + "created_at = '" + obj.getCreatedAt().toString() + "' "
                + "WHERE user_id = " + obj.getId();
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
              System.out.println("Cap nhat nguoi dung thanh cong!");
            } else {
              System.out.println("Cap nhat that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
          e.printStackTrace();
        }
    }   

    public User selectByUsername(String user) {
    User result = null;
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(
             "SELECT * FROM users WHERE username = ?")) {
        
        pstmt.setString(1, user);
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            // Tùy vào Role trong DB mà khởi tạo Bidder, Seller hoặc Admin
            // Ví dụ khởi tạo Bidder:
            result = new Bidder();
            result.setUsername(rs.getString("username"));
            result.setPassword(rs.getString("password"));
            result.setFullName(rs.getString("full_name"));
            // Lấy balance kiểu BigDecimal từ SQL
            result.setBalance(rs.getBigDecimal("balance")); 
            result.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return result;
}

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = " + id;
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
              System.out.println("Xoa nguoi dung thanh cong!");
            } else {
              System.out.println("Xoa that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
          e.printStackTrace();
        }
    }

    public boolean login(String username, String password) {
        // Implementation for user login
        return false;
    }
}
