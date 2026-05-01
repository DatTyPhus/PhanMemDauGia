package com.auction.sever.dao;

import com.auction.shared.model.User;

import java.sql.Connection;
import java.sql.Statement;

public class UserDAO implements DAOinterface<User> {
    public static UserDAO instance() {
        return new UserDAO();
    }
  
    @Override
    public void create(User obj) {

      String sql = "INSERT INTO users (user_id, username, password, full_name, role, balance, created_at) VALUES ('"
                + obj.getId() + "', '"
                + obj.getUsername() + "', '"
                + obj.getPassword() + "', '"
                + obj.getFullName() + "', '"
                + obj.getRole() + "', "
                + obj.getBalance() + ", '"
                + obj.getCreatedAt().toString() + "')";
      Connection connection = null;
      try{
          connection = JDBCUtil.getConnection();
          Statement st= connection.createStatement();

          int kq = st.executeUpdate(sql);
          if (kq > 0) {
            System.out.println("Them nguoi dung thanh cong!");
          } else {
            System.out.println("Them that bai, vui long kiem tra lai du lieu.");
          }
          JDBCUtil.closeConnection(connection);
      } catch(Exception e){
        e.printStackTrace();
      }
    }

    @Override
    public User read(Integer id) {
        return null;
    }

    @Override
    public void update(User obj) {
        String sql = "UPDATE users SET username = '" + obj.getUsername() + "', "
                + "password = '" + obj.getPassword() + "', "
                + "full_name = '" + obj.getFullName() + "', "
                + "role = '" + obj.getRole() + "', "
                + "balance = " + obj.getBalance() + ", "
                + "created_at = '" + obj.getCreatedAt().toString() + "' "
                + "WHERE user_id = " + obj.getId();
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
              System.out.println("Cap nhat nguoi dung thanh cong!");
            } else {
              System.out.println("Cap nhat that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
          e.printStackTrace();
        }
    }   

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = " + id;
        Connection connection = null;
        try{
            connection = JDBCUtil.getConnection();
            Statement st= connection.createStatement();

            int kq = st.executeUpdate(sql);
            if (kq > 0) {
              System.out.println("Xoa nguoi dung thanh cong!");
            } else {
              System.out.println("Xoa that bai, vui long kiem tra lai du lieu.");
            }
            JDBCUtil.closeConnection(connection);
        } catch(Exception e){
          e.printStackTrace();
        }
    }

    public boolean login(String username, String password) {
        // Implementation for user login
        return false;
    }
}
