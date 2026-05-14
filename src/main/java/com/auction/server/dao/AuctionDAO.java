package com.auction.server.dao;

import com.auction.shared.model.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuctionDAO {
    public static AuctionDAO instance() {
        return new AuctionDAO();
    }

    public void create(Auction obj) {
      String sql = "INSERT INTO auctions ( auction_id, item_id, current_price, durationMinutes) VALUES ('"
                + obj.getId() + "', "
                + obj.getItemId() + "', "
                + obj.getCurrentPrice() + ", "
                + obj.getDurationMinutes() + ")";
      Connection connection = null;
      try{
          connection = JDBCUtil.getConnection();
          Statement st= connection.createStatement();

          int kq = st.executeUpdate(sql);
          if (kq > 0) {
            System.out.println("Them dau gia thanh cong!");
          } else {
            System.out.println("Them that bai, vui long kiem tra lai du lieu.");
          }
          JDBCUtil.closeConnection(connection);
      } catch(Exception e){
        e.printStackTrace();
      }
    }
    
    public Auction selectById(Integer id) {
        String sql = "SELECT * FROM auctions WHERE auction_id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // Gán giá trị id vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getInt("auction_id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setItemName(rs.getString("item_name"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setDurationMinutes(rs.getInt("durationMinutes"));
                auction.setStatus(rs.getString("status"));

                return auction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }

    public Auction selectByItemName(String item_name) {
    String sql = "SELECT * FROM auctions WHERE item_name = ?";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, item_name); // Gán giá trị item_name vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Auction auction = new Auction();
            auction.setId(rs.getInt("auction_id"));
            auction.setItemId(rs.getInt("item_id"));
            auction.setCurrentPrice(rs.getBigDecimal("current_price"));
            auction.setDurationMinutes(rs.getInt("durationMinutes"));
            auction.setItemName(rs.getString("item_name"));

            return auction;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}


    // có thể phải sửa lại để update được giá và người thắng
    public void update(Auction obj) {
        String sql = "UPDATE auctions SET current_price = " + obj.getCurrentPrice() +
                ", highest_bidder_id = " + obj.getHighestBidderId() +
                ", status = '" + obj.getStatus() + "' WHERE auction_id = " + obj.getId();
        Connection connection = null;
        try {
            connection = JDBCUtil.getConnection();
            Statement st = connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
                System.out.println("Cập nhật đấu giá thành công!");
            } else {
                System.out.println("Cập nhật thất bại, vui lòng kiểm tra lại dữ liệu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public void delete(Integer id) {
        String sql = "DELETE FROM auctions WHERE auction_id = " + id;
        Connection connection = null;
        try {
            connection = JDBCUtil.getConnection();
            Statement st = connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
                System.out.println("Xoa dau gia thanh cong!");
            } else {
                System.out.println("Xoa that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
