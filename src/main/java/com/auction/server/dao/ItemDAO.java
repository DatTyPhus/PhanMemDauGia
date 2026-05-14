package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ItemDAO  {
    public static ItemDAO instance() {
        return new ItemDAO();
    }

    public void create(Item obj) {
        String sql = "INSERT INTO items (item_id, seller_id, item_name, description, category, starting_price, image_url, created_at) VALUES ('"
                + obj.getId() + "', '"
                + obj.getSellerId() + "', '"
                + obj.getName() + "', '"
                + obj.getDescription() + "', "
                + obj.getCategory() + ", "
                + obj.getStartingPrice() + ", "
                + obj.getImageUrl() + ", '"
                + obj.getCreatedAt().toString() + "')";
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
                System.out.println("Them san pham thanh cong!");
            } else {
                System.out.println("Them that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
            e.printStackTrace();
    }
  }

  
  public void update(Item obj) {
      String sql = "UPDATE items SET seller_id = '" + obj.getSellerId() + "', "
              + "item_name = '" + obj.getName() + "', "
              + "description = '" + obj.getDescription() + "', "
              + "category = '" + obj.getCategory() + "', "
              + "starting_price = " + obj.getStartingPrice() + ", "
              + "image_url = '" + obj.getImageUrl() + "', "
              + "created_at = '" + obj.getCreatedAt().toString() + "' "
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





    public Item selectByName(String name) {
    String sql = "SELECT * FROM items WHERE item_name = ?";
    
    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, name); // Gán giá trị name vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Item item=null;
            String type= rs.getString("item_type");
            if (type.equals("ARTS")){ item = new Art(); }
            else if (type.equals("ELECTRONICS")) { item = new Electronics(); }
            else if (type.equals("VEHICLES")) { item = new Vehicle(); }
            
            item.setId(rs.getInt("item_id"));
            item.setSellerId(rs.getInt("seller_id"));
            item.setName(rs.getString("item_name"));
            item.setDescription(rs.getString("description"));
            item.setCategory(rs.getString("category"));
            item.setStartingPrice(rs.getBigDecimal("starting_price"));
            return item;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}
}
