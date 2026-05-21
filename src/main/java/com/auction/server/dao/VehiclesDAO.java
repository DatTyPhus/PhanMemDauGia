package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.auction.shared.model.Vehicle;

public class VehiclesDAO {
  public Vehicle getItemByUserid(int id) {
        String sql = "SELECT item_id, seller_id, item_name, description,  start_price, image_url, created_at FROM vehicles WHERE id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // gán giá triu id vào dấu chấn hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setId(rs.getInt("id"));
                vehicle.setSellerId(rs.getInt("seller_id"));
                vehicle.setName(rs.getString("item_name"));
                vehicle.setDescription(rs.getString("description"));
                vehicle.setStartingPrice(rs.getBigDecimal("start_price"));
                vehicle.setImageUrl(rs.getString("image_url"));
                return vehicle;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }
  
}