package com.auction.client.session;

import com.auction.shared.model.User;

// Class dùng để lưu thông tin của người dùng.

public class UserSession {
    private static UserSession instance;
    private User LoginUser;

    // Khoá hàm khởi tạo.
    private UserSession(){}

    /// ========== Áp dụng Singleton ==============
    public static UserSession getInstance(){
        if(instance==null){
            instance = new UserSession();
        }
        return instance;
    }

    // Getter , trả về đối tượng người dùng:
    public User getLoginUser(){return  LoginUser;}

    // Setter , sửa role đối tượng người dùng:
    public void setLoginUser(User user){LoginUser=user;}

    // Clean, xoá đối tượng khi đăng xuất:
    public void cleanloginUser(){LoginUser=null;}
}
