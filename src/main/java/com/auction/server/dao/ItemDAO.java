package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;     
import java.util.ArrayList;

public class ItemDAO  {
    public static ItemDAO instance() {
        return new ItemDAO();
    }

    public void create(Item obj) {
        // BƯỚC 4: Sử dụng PreparedStatement (?) để chống lỗi nháy đơn và SQL Injection.
        // Tuyệt đối không chèn item_id vì nó tự động tăng (AUTO_INCREMENT).
        // Sửa lại cho đúng tên cột trong DB: start_price, item_type, duration_minutes.
        String sql = "INSERT INTO items (seller_id, item_name, description, item_type, start_price, image_url, duration_minutes, status) VALUES (?, ?,   ?, ?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Bơm dữ liệu từ Object vào các dấu hỏi chấm (?) theo đúng thứ tự
            ps.setInt(1, obj.getSellerId());
            ps.setString(2, obj.getName());
            ps.setString(3, obj.getDescription());
            ps.setString(4, obj.getItemType());          // Lưu loại (ART, ELECTRONIC, VEHICLE)
            ps.setBigDecimal(5, obj.getStartingPrice()); // Lưu giá
            ps.setString(6, obj.getImageUrl() != null ? obj.getImageUrl() : ""); // Tránh lỗi null ảnh
            ps.setInt(7, obj.getDurationMinutes());      // Lưu thời gian
            ps.setString(8, obj.getStatus());            // Trạng thái PENDING
            // Thực thi lệnh chèn xuống CSDL    
            int kq = ps.executeUpdate();

            if (kq > 0) {
                System.out.println("[DATABASE] Đã lưu sản phẩm '" + obj.getName() + "' vào kho CHỜ DUYỆT thành công!");
            } else {
                System.out.println("[DATABASE] Thêm thất bại, vui lòng kiểm tra lại dữ liệu.");
            }

        } catch (Exception e) {
            System.err.println("[DATABASE ERROR] Lỗi khi lưu sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }

  
  public static void update(Item obj) {
      String sql = "UPDATE items SET seller_id = '" + obj.getSellerId() + "', "
              + "item_name = '" + obj.getName() + "', "
              + "description = '" + obj.getDescription() + "', "
              + "starting_price = " + obj.getStartingPrice() + ", "
              + "image_url = '" + obj.getImageUrl() + "', "
              + "status = '" + obj.getStatus() + "' "
              + "WHERE item_id = " + obj.getId();
      Connection connection = null;
      try{
          connection = JDBCUtil.getConnection();
          Statement st= connection.createStatement();

          int kq = st.executeUpdate(sql);
          if (kq > 0) {
            System.out.println("Cap nhat san pham thanh cong!");
          } else {
            System.out.println("Cap nhat that bai, vui long kiem tra lai du lieu.");
          }
          JDBCUtil.closeConnection(connection);
      } catch(Exception e){
        e.printStackTrace();
      }
  }

    
    public void delete(Integer id) {
        String sql = "DELETE FROM items WHERE item_id = " + id;
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
              System.out.println("Xoa san pham thanh cong!");
            } else {
              System.out.println("Xoa that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
          e.printStackTrace();
        } 

      }


    public static List<Item> findPendingItems() {
        String sql = "SELECT * FROM items WHERE status = 'PENDING'";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            List<Item> pendingItems = new ArrayList<>();
            while (rs.next()) {
                Item item = null;
                String type = rs.getString("item_type");
                item = Item.createFromType(type);
                item.setId(rs.getInt("item_id"));
                item.setSellerId(rs.getInt("seller_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setStartingPrice(rs.getBigDecimal("starting_price"));
                item.setImageUrl(rs.getString("image_url"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setStatus(rs.getString("status"));
                pendingItems.add(item);
            }
            return pendingItems;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
      }

    
    public static List<Item> findItemsBySellerId(int sellerId) {
        String sql = "SELECT * FROM items WHERE seller_id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sellerId); // Gán giá trị sellerId vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            List<Item> sellerItems = new ArrayList<>();
            while (rs.next()) {
                Item item = null;
                String type = rs.getString("item_type");
                item = Item.createFromType(type);
                item.setId(rs.getInt("item_id"));
                item.setSellerId(rs.getInt("seller_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setStartingPrice(rs.getBigDecimal("starting_price"));
                item.setImageUrl(rs.getString("image_url"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setStatus(rs.getString("status"));
                sellerItems.add(item);
            }
            return sellerItems;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }


    public Item selectByName(String name) {
    String sql = "SELECT * FROM items WHERE item_name = ?";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, name); // Gán giá trị name vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Item item=null;
            String type= rs.getString("item_type");
            item = Item.createFromType(type);
            item.setId(rs.getInt("item_id"));
            item.setSellerId(rs.getInt("seller_id"));
            item.setName(rs.getString("item_name"));
            item.setDescription(rs.getString("description"));
            item.setStartingPrice(rs.getBigDecimal("starting_price"));
            item.setImageUrl(rs.getString("image_url"));
            item.setDurationMinutes(rs.getInt("duration_minutes"));
            item.setStatus(rs.getString("status"));
            return item;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
    }


    public static Item selectById(int id) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id); // Gán giá trị id vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Item item=null;
                String type= rs.getString("item_type");
                item = Item.createFromType(type);
                item.setId(rs.getInt("item_id"));
                item.setSellerId(rs.getInt("seller_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setStartingPrice(rs.getBigDecimal("starting_price"));
                item.setImageUrl(rs.getString("image_url"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setStatus(rs.getString("status"));
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }
}
