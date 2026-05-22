package com.auction.server.dao;

import com.auction.shared.model.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    public static AuctionDAO instance() {
        return new AuctionDAO();
    }

    public static void create(Auction obj) {
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
    
    public static Auction selectById(Integer id) {
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

    public static Auction selectStartTime (String endTime){
        String sql = "SELECT * FROM auctions WHERE end_time = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(endTime)); // Gán giá trị endTime vào dấu chấm hỏi
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

    public static Auction selectByItemName(String item_name) {
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

    public static void update(Auction obj) {
    // Câu lệnh SQL update toàn bộ các cột, định danh bằng dấu hỏi chấm (?)
    String sql = "UPDATE auctions SET item_id = ?, item_name = ?, current_price = ?, "
               + "highest_bidder_id = ?, start_time = ?, end_time = ?, "
               + "durationMinutes = ?, status = ? WHERE auction_id = ?";
               
    Connection connection = null;
    try {
        connection = JDBCUtil.getConnection();
        // Sử dụng PreparedStatement để truyền tham số an toàn
        java.sql.PreparedStatement pst = connection.prepareStatement(sql);
        
        // Truyền giá trị vào các dấu hỏi chấm theo đúng thứ tự
        pst.setInt(1, obj.getItemId());              // Giả định itemId là kiểu Int
        pst.setString(2, obj.getItemName());          // item_name (String)
        pst.setBigDecimal(3, obj.getCurrentPrice());      // current_price (Double/Float)
        pst.setString(4, obj.getHighestBidderName());      // highest_bidder_name (String)
        pst.setString(5, obj.getStartTime());      // start_time (DateTime/Timestamp)
        pst.setString(6, obj.getEndTime());        // end_time (DateTime/Timestamp)
        pst.setInt(7, obj.getDurationMinutes());      // durationMinutes (Int)
        pst.setString(8, obj.getStatus());            // status (String)
        
        // Dấu hỏi chấm cuối cùng nằm ở điều kiện WHERE
        pst.setInt(9, obj.getId());                  // auction_id làm khóa chính
        
        int kq = pst.executeUpdate();
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

    public static List<Auction> findFromAuction(int auctionId) {
    List<Auction> result = new ArrayList<>();
    String sql = """
        SELECT * FROM auction 
        WHERE start_time >= (SELECT start_time FROM auction WHERE id = ?)
        AND status = 'PENDING'
        ORDER BY start_time ASC
    """;

    Connection c = JDBCUtil.getConnection();
    try {
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, auctionId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(mapResultSet(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return result;
}
    private static Auction mapResultSet(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setId       (rs.getInt   ("id"));
        auction.setItemId   (rs.getInt   ("item_id"));
        auction.setItemName     (rs.getString("name"));
        auction.setStartingPrice(rs.getBigDecimal("starting_price"));
        auction.setCurrentPrice (rs.getBigDecimal("current_price"));
        auction.setDurationMinutes(rs.getInt("durationMinutes"));
        auction.setStatus   (rs.getString("status"));
        auction.setStartTime(rs.getString("start_time")); // hoặc convert sang LocalDateTime
        auction.setEndTime  (rs.getString("end_time"));
        // thêm các field khác tùy theo bảng DB của bạn
        return auction;
    }


    public static List<Auction> selectByStatus(String status) {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status); // Gán giá trị status vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getInt("auction_id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setItemName(rs.getString("item_name"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setDurationMinutes(rs.getInt("durationMinutes"));
                auction.setStatus(rs.getString("status"));
                auctions.add(auction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auctions; // Trả về danh sách đấu giá có trạng thái tương ứng
    }

    public static void delete(Integer id) {
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
