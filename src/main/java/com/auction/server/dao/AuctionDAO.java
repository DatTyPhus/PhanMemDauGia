package com.auction.server.dao;

import com.auction.shared.model.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/// Class dùng để lấy thông tin ở bảng Auction
public class AuctionDAO {
    public static AuctionDAO instance() {
        return new AuctionDAO();
    }

    /// Hàm tạo lưu thông tin của một cuộc đấu giá xuống bảng database.
    public static void create(Auction obj) {
        String sql = "INSERT INTO auctions (item_id, item_name, current_price, durationMinutes, status, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = JDBCUtil.getConnection();
             /// Thêm cờ RETURN_GENERATED_KEYS để xin lại cái ID vừa tạo
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, obj.getItemId());
            ps.setString(2, obj.getItemName() != null ? obj.getItemName() : "");
            ps.setBigDecimal(3, obj.getCurrentPrice());
            ps.setInt(4, obj.getDurationMinutes());
            ps.setString(5, obj.getStatus() != null ? obj.getStatus() : "PENDING");
            ps.setString(6, obj.getStartTime());
            ps.setString(7, obj.getEndTime());

            int kq = ps.executeUpdate();
            if (kq > 0) {
                /// Lấy ID do MySQL tự sinh ra và gán ngược lại cho đối tượng Auction
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    obj.setId(rs.getInt(1));
                }
                System.out.println("[DATABASE] Đã tạo phiên đấu giá thành công với ID Độc Nhất: " + obj.getId());
            } else {
                System.out.println("[DATABASE] Thêm phiên đấu giá thất bại.");
            }
        } catch (Exception e) {
            System.err.println("[DAO ERROR] Lỗi tạo Auction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    ///  Hàm này lấy thông tin cuộc đấu giá bằng ID do database cung cấp.
    public static Auction selectById(Integer id) {
        String sql = "SELECT * FROM auctions WHERE auction_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // Gán giá trị id vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                //Tạo đối tượng để lấy các thông tin từ database gán vào các thuộc tính.
                Auction auction = new Auction();
                auction.setId(rs.getInt("auction_id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setItemName(rs.getString("item_name"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setDurationMinutes(rs.getInt("durationMinutes"));
                auction.setStatus(rs.getString("status"));
                auction.setStartTime(rs.getString("start_time"));
                auction.setEndTime(rs.getString("end_time"));
                auction.setHighestBidderName(rs.getString("highest_bidder_id"));

                return auction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /// Hàm dùng Tìm kiếm phiên đấu giá nối đuôi tiếp theo trong danh sách.
    public static Auction selectStartTime (String endTime){
        /// Tìm phiên đấu giá MỚI CÓ GIỜ BẮT ĐẦU trùng khớp với giờ kết thúc của phiên vừa xong.
        String sql = "SELECT * FROM auctions WHERE start_time = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(endTime));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Tạo đối tượng để lấy các thông tin từ database gán vào các thuộc tính.
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
        return null; // Trả về null nếu không tìm thấy phiên tiếp theo
    }

    /// Hàm dùng Tra cứu thông tin phiên đấu giá thông qua tên sản phẩm.
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

    /// Hàm Ghi đè toàn bộ dữ liệu mới nhất của một phiên đấu giá xuống Database.
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
        pst.setInt(1, obj.getItemId());              // itemId là kiểu Int
        pst.setString(2, obj.getItemName());          // item_name (String)
        pst.setBigDecimal(3, obj.getCurrentPrice());      // current_price (Double/Float)
        pst.setString(4, obj.getHighestBidderName());      // highest_bidder_name (String)
        pst.setString(5, obj.getStartTime());      // start_time (DateTime/Timestamp)
        pst.setString(6, obj.getEndTime());        // end_time (DateTime/Timestamp)
        pst.setInt(7, obj.getDurationMinutes());      // durationMinutes (Int)
        pst.setString(8, obj.getStatus());            // status (String)
        

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

    /// Hàm dùng Tìm danh sách các phiên đấu giá bị ảnh hưởng lịch trình phía sau.
    public static List<Auction> findFromAuction(int auctionId) {
        List<Auction> result = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE start_time >= (SELECT start_time FROM auctions WHERE auction_id = ?) AND auction_id != ? AND status = 'WAITING' ORDER BY start_time ASC";

        try (Connection c = JDBCUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.setInt(2, auctionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //Tạo đối tượng để lấy các thông tin từ database gán vào các thuộc tính.
                Auction auction = new Auction();
                auction.setId(rs.getInt("auction_id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setItemName(rs.getString("item_name"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setDurationMinutes(rs.getInt("durationMinutes"));
                auction.setStatus(rs.getString("status"));
                auction.setStartTime(rs.getString("start_time"));
                auction.setEndTime(rs.getString("end_time"));
                result.add(auction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    ///
    private static Auction mapResultSet(ResultSet rs) throws SQLException {
        //Tạo đối tượng để lấy các thông tin từ database gán vào các thuộc tính.
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


    /// Hàm dùng Lọc danh sách các phiên đấu giá theo một trạng thái cụ thể.
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

    /// Hàm lấy toàn bộ danh sách các phiên đấu giá để hiển thị lên màn hình Quản lý của Admin và thẻ Card của User
    public static List<Auction> getAllAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT a.*, i.start_price FROM auctions a LEFT JOIN items i ON a.item_id = i.item_id ORDER BY a.auction_id DESC";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getInt("auction_id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setItemName(rs.getString("item_name"));

                /// Giá hiện tại (Biến động theo người đấu giá)
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));

                ///  Gắn Giá khởi điểm (Giá gốc) bốc từ bảng items sang
                auction.setStartingPrice(rs.getBigDecimal("start_price"));

                auction.setDurationMinutes(rs.getInt("durationMinutes"));
                auction.setStatus(rs.getString("status"));
                auction.setStartTime(rs.getString("start_time"));
                auction.setEndTime(rs.getString("end_time"));

                list.add(auction);
            }
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi khi lấy danh sách toàn bộ Auction: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /// Hàm xóa cuộc đấu giá.
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

    /// Quét Database để tìm ra thời điểm kết thúc muộn nhất của các phiên đang đợi/đang chạy
    public static java.time.LocalDateTime getLatestEndTime() {
        String sql = "SELECT MAX(end_time) AS max_end FROM auctions WHERE status IN ('OPEN', 'WAITING')";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String maxEndStr = rs.getString("max_end");
                if (maxEndStr != null && !maxEndStr.isEmpty()) {
                    /// Cắt bỏ phần thập phân .0 nếu MySQL tự động nối vào
                    if (maxEndStr.endsWith(".0")) {
                        maxEndStr = maxEndStr.substring(0, maxEndStr.length() - 2);
                    }
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return java.time.LocalDateTime.parse(maxEndStr, formatter);
                }
            }
        } catch (Exception e) {
            System.err.println("[DAO ERROR] Lỗi khi lấy mốc thời gian lớn nhất: " + e.getMessage());
        }
        return null; /// Trả về null nếu hiện tại sàn đấu giá đang trống trơn
    }
}
