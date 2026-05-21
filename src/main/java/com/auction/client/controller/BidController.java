package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.shared.network.Message;

import javafx.fxml.FXML;
import java.awt.*;
import javafx.scene.control.TextField;

public class BidController {


    @FXML private TextField txtBidAmount; // Ô nhập số tiền đấu giá trên FXML của bạn

    @FXML
    public void onPlaceBidClick() {
        try {
            // 1. Thu thập dữ liệu cần thiết để gửi đi
            int productId = 15; // Giả sử ID sản phẩm hiện tại là 15 (sau này bạn lấy động theo sản phẩm đang xem)
            String bidAmountStr = txtBidAmount.getText().trim();

            // Lấy tên đầy đủ của người đang đăng nhập từ Session nội bộ của Client
            String bidderName = com.auction.client.session.UserSession.getInstance().getLoginUser().getFullName();

            // Kiểm tra nhanh tránh trường hợp người dùng để trống ô nhập
            if (bidAmountStr.isEmpty()) {
                System.out.println("Vui lòng nhập số tiền muốn đấu giá!");
                return;
            }

            // 2. Đóng gói dữ liệu thành chuỗi payload phẳng (Format phân tách: productId,sốTiền,tênNgườiĐặt)
            String payload = productId + "," + bidAmountStr + "," + bidderName;

            // 3. Tạo gói tin Message với Action là PLACE_BID và bắn thẳng lên Server
            Message bidMsg = new Message("PLACE_BID", payload);
            NetworkClient.getInstance().send(bidMsg);

            System.out.println("👉 Client đã bắn lệnh PLACE_BID lên Server: " + payload);

            // Xóa trắng ô nhập tiền để chuẩn bị cho lần ra giá tiếp theo
            txtBidAmount.clear();

        } catch (Exception e) {
            System.err.println("Lỗi khi thực hiện đặt giá: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
