package com.auction.sever.dao;

import com.auction.shared.model.Auction;

import java.sql.Connection;
import java.sql.Statement;

public class AuctionDAO implements DAOinterface<Auction> {
    public static AuctionDAO instance() {
        return new AuctionDAO();
    }

    @Override
    public void create(Auction obj) {
      String sql = "INSERT INTO auctions (auction_id, item_id, current_price, highest_bidder_id, end_time, status, created_at) VALUES ('"
                + obj.getId() + "', '"
                + obj.getItemId() + "', "
                + obj.getCurrentPrice() + ", "
                + obj.getHighestBidderId() + ", '"
                + obj.getEndTime().toString() + "', '"
                + obj.getStatus() + "', '"
                + obj.getCreatedAt().toString() + "')";
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
    @Override
    public Auction read(Integer id) {
        return null;
    }

    // có thể phải sửa lại để update được giá và người thắng
    @Override
    public void update(Auction obj) {
        String sql = "UPDATE auctions SET current_price = " + obj.getCurrentPrice() +
                ", highest_bidder_id = " + obj.getHighestBidderId() +
                ", status = '" + obj.getStatus() + "' WHERE auction_id = " + obj.getAuctionId();
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

    @Override
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
