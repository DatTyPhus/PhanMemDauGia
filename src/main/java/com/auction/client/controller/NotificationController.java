package com.auction.client.controller;

import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;

/// Class NotificationController này dùng để xử lý các yêu cầu của người dùng qua thao tác,và nhận tín hiệu phản hồi từ server để hiển thị lên màn hình.

public class NotificationController {
    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {

        User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

        // Kiểm tra an toàn: Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
        if (currentUser != null && lblUserName != null) {         /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin ở góc phải
            lblUserName.setText(currentUser.getFullName());
            lblUserRole.setText(currentUser.getRole());
        }
    }

    /// Hàm này thực hiện khi thao tác quay về trang chủ(home.fxml)
    @FXML
    public void onBackToHomeClick(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml")); //Tải lên file home.fxml
            Parent root = loader.load();

            // Thay đổi cửa sổ thành giao diện home.
            Node source = (Node) event.getSource();
            source.getScene().setRoot(root);

            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /// Hàm này thực hiện khi thao tác chuyển sang trang phiên đấu giá "phiendaugia.fxml"
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/phiendaugia.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  //Thay màn notification.fxml thành màn phiendaugia.fxml
            source.getScene().setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /// Hàm này thực hiện khi click vào ô hộp thư ở góc phải màn
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        System.out.println("Bạn đang ở trang Thông báo rồi!");
    }

    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        try {
            // 1. Tìm bản vẽ setting.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/setting.fxml"));
            Parent root = loader.load();

            // 2. Lấy Scene hiện tại và thay "ruột" bằng trang Cài đặt
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            // 3. Đổi tiêu đề cửa sổ
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("Cài đặt - Hệ thống đấu giá");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file setting.fxml!");
        }
    }

    @FXML
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        // 1. Lấy thông tin người dùng hiện tại từ Session
        com.auction.shared.model.User currentUser = com.auction.client.session.UserSession.getInstance().getLoginUser();

        // 2. KIỂM TRA QUYỀN (Chỉ cho phép Seller)
        if (currentUser != null && "Seller".equalsIgnoreCase(currentUser.getRole())) {
            // Đủ điều kiện -> Cho phép chuyển sang trang Quản lý sản phẩm
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/product_management.fxml"));
                Parent root = loader.load();

                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                source.getScene().setRoot(root);

                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
                currentStage.setTitle("Quản lý sản phẩm - Hệ thống đấu giá");
            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi: Không tìm thấy file product_management.fxml!");
            }
        } else {
            // Nếu là Bidder (hoặc role khác) -> Bật thông báo từ chối
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Từ chối truy cập");
            alert.setHeaderText(null);
            alert.setContentText("Xin lỗi, tính năng Quản lý sản phẩm chỉ dành riêng cho Người Bán (Seller)!");
            alert.showAndWait();
        }
    }


}