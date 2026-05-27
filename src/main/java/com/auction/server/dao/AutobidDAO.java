package com.auction.server.dao;
import com.auction.shared.model.BidTransaction;
import java.sql.*;
import java.math.BigDecimal;

/// Class này dùng để lưu và lấy thông tin từ bảng auto-bid cô một phòng đấu giá

public class AutobidDAO {

    /// Hàm tạo rỗng lúc duyệt sản phẩm (Đã có sẵn của bạn)
    public static void createAutobid(int auctionId) {
        String sql = "INSERT INTO autobid (id_auction, autoBidAmount, autoBidStep, AutoBidderName) VALUES (?, 0, 0, NULL)";
        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /// Lấy thông tin Bot hiện tại của phòng
    public static BidTransaction getAutoBidsByAuctionId(int auctionId) {
        String sql = "SELECT * FROM autobid WHERE id_auction = ?";
        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String botName = rs.getString("AutoBidderName");
                if (botName != null && !botName.isEmpty()) {
                    return new BidTransaction(auctionId, botName, rs.getBigDecimal("autoBidAmount"), rs.getBigDecimal("autoBidStep"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; // Không có ai cài Bot
    }

    /// Cập nhật (Ghi đè) Bot mới, hoặc Xóa Bot (khi truyền số 0)
    public static void updateAutobid(int auctionId, BigDecimal amount, BigDecimal step, String bidderName) {
        String sql = "UPDATE autobid SET autoBidAmount = ?, autoBidStep = ?, AutoBidderName = ? WHERE id_auction = ?";
        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, step);
            ps.setString(3, bidderName);
            ps.setInt(4, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}