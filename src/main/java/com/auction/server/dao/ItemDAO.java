package com.auction.server.dao;

import com.auction.shared.model.Item;

import java.sql.Connection;
import java.sql.Statement;

public class ItemDAO implements DAOinterface<Item> {
    public static ItemDAO instance() {
        return new ItemDAO();
    }

    @Override
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
  @Override
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

    @Override
    public Item read(Integer id) {
        return null;
    }
    @Override
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
}
