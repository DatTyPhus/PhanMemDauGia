package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.auction.shared.model.Electronics;

public class ElectronicsDAO {
  public Electronics getItemByUserid(int id) {
        String sql = "SELECT item_id, seller_id, item_name, description, category, start_price, image_url FROM electronics WHERE id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // gán giá triu id vào dấu chấn hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Electronics electronic = new Electronics();
                electronic.setId(rs.getInt("id"));
                electronic.setSellerId(rs.getInt("seller_id"));
                electronic.setName(rs.getString("item_name"));
                electronic.setDescription(rs.getString("description"));
                electronic.setCategory(rs.getString("category"));
                electronic.setStartingPrice(rs.getBigDecimal("start_price"));
                electronic.setImageUrl(rs.getString("image_url"));
                return electronic;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }
  
}