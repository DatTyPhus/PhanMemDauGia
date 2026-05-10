package com.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//sau này xây dựng lớp item rôi làm thêm các chức năng
//  1. Tạo cuộc đấu giá (đưa 1 vật phẩm mới và return 1 cuộc đấu giá)
//  2. Cập nhất vật phẩm
//  3. Xóa vật phẩm
//  4. Coi lại các cuộc giao dịch

public class Seller extends User {
    protected int sellerId;
    public Seller( String username, String password, String fullName, double balance, LocalDateTime createdAt) {
        super(username, password, fullName, Role.SELLER, balance, createdAt);
        this.sellerId = getId();
    }

    //1. Tạo cuộc đấu giá mới.

    public void createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        System.out.println("Người bán " + getUsername() + " đang tạo phiên đấu giá cho: " + item.getName());

        // Sau đó Server sẽ khởi tạo đối tượng Auction
    }

    /**
     * 2. Cập nhật thông tin vật phẩm.
     * chỉnh sửa tên, mô tả hoặc giá khởi điểm trước khi phiên đấu giá bắt đầu.
     */
    public void updateItem(Item item, String newName, String newDesc, BigDecimal newStartingPrice) {
        if (item != null) {
            item.setItemName(newName);
            item.setItemDescription(newDesc);
            item.setItemStartPrice(newStartingPrice);
            System.out.println("Đã cập nhật thông tin sản phẩm: " + item.getName());
        }
    }

    /**
     * 3. Xóa vật phẩm.
     * Seller có thể xóa sản phẩm nếu sản phẩm đó chưa được đưa vào phiên đấu giá đang chạy.
     */
    public void deleteItem(Item item) {
        System.out.println("Đã xóa sản phẩm: " + item.getName());
    }

    /**
     * 4. Xem lại các cuộc giao dịch.
     * Xem lịch sử các phiên đấu giá mà người bán này đã tổ chức.
     */
    public void viewMyTransactions() {
        // Logic truy vấn danh sách BidTransaction liên quan đến các Auction của Seller này.
        System.out.println("Đang truy xuất lịch sử giao dịch của người bán: " + getUsername());
    }


}