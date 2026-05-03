package com.auction.sever.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class JDBCUtil {
    public static Connection getConnection() {
        Connection c=null;
        String url = "jdbc:mysql://localhost:3306/quan_li_giao_dich";
        String user = "root";
        String password = "huyhoang1109"; 

        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            c = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
    public static void closeConnection(Connection c) {
        try {
            if (c!=null){
                c.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}