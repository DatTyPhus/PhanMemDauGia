package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/// Class lấy thông tin từ bảng Admin ở database.
public class AdminDAO {

    /// Lấy thông tin admin theo tên đăng nhập
    public static Admin selectByUsername(String username) {
        String sql = "SELECT * FROM admin WHERE username = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Admin user = new Admin();

                user.setId(rs.getInt("admin_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));

                user.setRole("Admin");

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}