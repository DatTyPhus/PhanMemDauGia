package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.auction.shared.model.Art;

public class ArtsDAO {
  public Art getItemByUserid(int id) {
        String sql = "SELECT item_id, seller_id, item_name, description, category, start_price, image_url FROM arts WHERE id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // gán giá triu id vào dấu chấn hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Art art = new Art();
                art.setId(rs.getInt("id"));
                art.setSellerId(rs.getInt("seller_id"));
                art.setName(rs.getString("item_name"));
                art.setDescription(rs.getString("description"));
                art.setCategory(rs.getString("category"));
                art.setStartingPrice(rs.getBigDecimal("start_price"));
                art.setImageUrl(rs.getString("image_url"));
                return art;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }
  
}