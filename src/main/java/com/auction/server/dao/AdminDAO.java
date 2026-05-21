package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {
  public static Admin selectByUsername(String username) {
    String sql = "SELECT * FROM admins WHERE username = ?";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username); // Gán giá trị username vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Admin user = new Admin();
            user.setId(rs.getInt("user_id")); // Lưu ý: Tên cột nên khớp với DB (thường là user_id hoặc id)
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            
            return user;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}
}