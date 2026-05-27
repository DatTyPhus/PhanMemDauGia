package com.auction.server.dao;

import com.auction.shared.model.Auction;
import com.auction.shared.model.BidTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/// class BidTransactionDAO dùng để thao tác với bảng lịch sử đặt giá dưới Database
public class BidTransactionDAO {

    /// Hàm ghi nhận một lượt đặt giá mới vào lịch sử
    public static boolean insert(BidTransaction bid, String bidTime) {
        /// Giả định bảng của bạn tên là bid_transactions (gồm: auction_id, bidder_name, bid_amount, bid_time)
        String sql = "INSERT INTO bid_transactions (auction_id, bidder_name, bid_amount, bid_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bid.getAuctionId());
            ps.setString(2, bid.getBiddername());
            ps.setBigDecimal(3, bid.getBidAmount());
            ps.setString(4, bidTime);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi lưu lịch sử đặt giá: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /// Hàm lấy  toàn bộ lịch sử của một phiên đấu giá lên để hiển thị cho người dùng mới vào phòng
    public static List<String[]> getHistoryByAuctionId(int auctionId) {
        List<String[]> historyList = new ArrayList<>();
        /// Lấy lịch sử và sắp xếp mới nhất lên đầu
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY bid_amount DESC";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String time = rs.getString("bid_time");
                String name = rs.getString("bidder_name");
                String amount = rs.getBigDecimal("bid_amount").toPlainString();

                historyList.add(new String[]{time, name, amount});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    /// Lấy danh sách các phiên đấu giá mà người dùng đã THẮNG (Trạng thái CLOSED + User là người dẫn đầu)
    public static List<Auction> getWonAuctionsByUsername(String username) {
        List<Auction> wonAuctions = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status = 'CLOSED' AND highest_bidder_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            boolean isAuctionId = false;
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if ("auction_id".equalsIgnoreCase(rsmd.getColumnName(i))) {
                    isAuctionId = true;
                    break;
                }
            }

            while (rs.next()) {
                Auction auction = new Auction();

                /// Bốc ID dựa theo tên cột thực tế dưới Database
                if (isAuctionId) {
                    auction.setId(rs.getInt("auction_id"));
                } else {
                    auction.setId(rs.getInt("id"));
                }

                auction.setItemName(rs.getString("item_name"));
                auction.setEndTime(rs.getString("end_time"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));

                wonAuctions.add(auction);
            }
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi khi lấy danh sách phiên thắng: " + e.getMessage());
        }
        return wonAuctions;
    }
}
