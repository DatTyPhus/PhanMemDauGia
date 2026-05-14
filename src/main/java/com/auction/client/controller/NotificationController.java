package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class NotificationController {

    @FXML
    public void onBackToHomeClick(ActionEvent event) {
        try {
            // Đi tìm lại file home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            // Lấy cửa sổ hiện tại và thay ruột
            Node source = (Node) event.getSource();
            source.getScene().setRoot(root);

            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.setTitle("Trang chủ Đấu Giá");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 1. Hàm lật sang trang Phiên Đấu Giá
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/phiendaugia.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("Phiên đấu giá - Hệ thống");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // 2. Hàm khi bấm vào nút Thông báo (Vì đang ở trang Thông báo rồi nên không cần lật trang nữa)
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        System.out.println("Bạn đang ở trang Thông báo rồi!");
        // Hoặc bạn có thể code thêm logic làm mới (refresh) danh sách thông báo ở đây
    }
}