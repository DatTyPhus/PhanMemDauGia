package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class HomeController {

    @FXML
    public void initialize() {
        // Hàm này sẽ tự động chạy khi giao diện home.fxml được mở lên
        System.out.println("Giao diện Home đã được load thành công!");
    }

    // Các hàm trống này để chống lỗi khi bạn bấm vào các nút Filter trên giao diện
    @FXML
    public void filterAll(ActionEvent event) { }

    @FXML
    public void filterRunning(ActionEvent event) { }

    @FXML
    public void filterUpcoming(ActionEvent event) { }

    @FXML
    public void filterFinished(ActionEvent event) { }

    // Lệnh @FXML để báo cho Java biết hàm này được liên kết với giao diện FXML
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        try {
            // 1. TÌM VÀ NẠP BẢN VẼ: Chỉ đường cho Java đi lấy file thông báo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/notification.fxml"));

            // Ép bản vẽ thành một khối giao diện thực tế (gọi là root)
            Parent root = loader.load();

            // 2. TÌM CỬA SỔ HIỆN TẠI:
            // "event.getSource()" chính là cái nút ✉️ mà bạn vừa bấm vào.
            // Từ cái nút đó, ta suy ngược ra cái Scene (khung cảnh) chứa nó.
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();

            // 3. THAY RUỘT: Giữ nguyên vỏ cửa sổ, lột ruột Home ra, nhét ruột Notification vào
            currentScene.setRoot(root);

            // 4. (Tùy chọn) Đổi tiêu đề cửa sổ ở góc trên cùng bên trái
            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("Thông báo - Hệ thống đấu giá");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file notification.fxml");
        }
    }
}