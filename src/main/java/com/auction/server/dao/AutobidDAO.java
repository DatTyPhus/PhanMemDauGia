package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutobidDAO {
  public static void createAutobid(int idAuction) {
    // Chỉ chỉ định cột id_auction, các cột còn lại tự động lấy giá trị mặc định (DEFAULT)
    String sql = "INSERT INTO `autobid` (`id_auction`) VALUES (?);";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        // Truyền giá trị idAuction được truyền từ tham số hàm vào dấu hỏi chấm (?)
        stmt.setInt(1, idAuction);
        
        // Thực thi câu lệnh ghi dữ liệu vào Database
        int rowsAffected = stmt.executeUpdate(); 
        
        if (rowsAffected > 0) {
            System.out.println("Đã tạo autobid thành công cho cuộc đấu giá ID: " + idAuction);
        }
        
    } catch (SQLException e) {
        System.err.println("Lỗi khi tạo autobid: " + e.getMessage());
        e.printStackTrace();
    }
}

  public static BidTransaction getAutoBidsByAuctionId(int auctionId) {
    BidTransaction autoBid = new BidTransaction();
    String sql = "SELECT * FROM autobids WHERE auction_id = ?";
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, auctionId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            BidTransaction autoBid1 = new BidTransaction(
                rs.getInt("id_auction"),
                rs.getString("AutoBidderName"),
                rs.getBigDecimal("autoBidAmount"),
                rs.getBigDecimal("autoBidStep")
            );
            autoBid = autoBid1; // Giả sử mỗi phiên đấu giá chỉ có một AutoBid, nếu có nhiều thì cần sửa lại kiểu trả về thành List<AutoBid>
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return autoBid;
  }
}
