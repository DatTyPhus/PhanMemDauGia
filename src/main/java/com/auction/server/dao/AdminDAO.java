package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {
    public Admin selectByUsername(String username) {
        // SỬA: Đổi "admins" thành "admin"
        String sql = "SELECT * FROM admin WHERE username = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Admin user = new Admin();
                // SỬA: Lấy đúng tên cột "admin_id" trong bảng admin
                user.setId(rs.getInt("admin_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                // BẮT BUỘC: Phải gán Role để Client còn biết đường mà rẽ nhánh
                user.setRole("Admin");

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}