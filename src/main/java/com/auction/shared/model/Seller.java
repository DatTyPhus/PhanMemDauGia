package com.auction.shared.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

//sau này xây dựng lớp item rôi làm thêm các chức năng
//  1. Tạo cuộc đấu giá (đưa 1 vật phẩm mới và return 1 cuộc đấu giá)
//  2. Cập nhất vật phẩm
//  3. Xóa vật phẩm
//  4. Coi lại các cuộc giao dịch

public class Seller extends User {

    public Seller( String username, String password, String fullName, BigDecimal balance, LocalDateTime createdAt) {
        super( username, password, fullName, "SELLER", balance, createdAt);
    }

    public Seller() {
        super();
    }

    public Seller(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    public Seller(String username, String password, String fullName, String role) {
        super(username, password, fullName, role);
    }
}