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

    //Method này dùng để xử lý khi click vào "Thông báo"
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

    //Method này có chức năng xử lý khi click vào nút "Phiên đấu giá"
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {
            // 1. TÌM VÀ NẠP BẢN VẼ: Chỉ đường cho JavaFX đi tìm file giao diện "Phiên đấu giá"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/phiendaugia.fxml"));
            Parent root = loader.load(); // Lệnh load() biến file text fxml thành các hình khối thật trong bộ nhớ

            // 2. LẤY CỬA SỔ HIỆN TẠI:
            // event.getSource() chính là lấy ra cái nút "Phiên đấu giá" mà người dùng vừa bấm vào
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            // Từ cái nút đó, ta lấy ra được Scene (khung cảnh) chứa nó
            javafx.scene.Scene currentScene = source.getScene();

            // 3. THAY RUỘT CỬA SỔ:
            // Giữ nguyên cái vỏ, lột ruột Home cũ ra, nhét ruột Phiên Đấu Giá mới (root) vào
            currentScene.setRoot(root);

            // 4. (Tùy chọn) Đổi lại tiêu đề của cửa sổ cho chuẩn
            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("Phiên đấu giá - Hệ thống");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file phiendaugia.fxml! Hãy kiểm tra lại đường dẫn.");
        }
    }

    // Hàm xử lý khi bấm nút "Trang chủ" từ màn hình Phiên Đấu Giá
    @FXML
    public void onBackToHomeClick(javafx.event.ActionEvent event) {
        try {
            // 1. Tìm bản vẽ home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            // 2. Lấy Scene hiện tại và thay "ruột"
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            // 3. Đổi lại tiêu đề cửa sổ
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("Trang chủ Đấu Giá");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không thể tải trang chủ!");
        }
    }
}