package com.auction.client.controller.adminController;

import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/// class ProductsManagement dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình ,và xử lý các yêu cầu từ server.

public class ProductsManagement {

    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    @FXML private TableView<?> reviewTable;
    @FXML private TableColumn<?, ?> idColumn;
    @FXML private TableColumn<?, ?> productColumn;
    @FXML private TableColumn<?, ?> sellerColumn;
    @FXML private TableColumn<?, ?> descriptionColumn;
    @FXML private TableColumn<?, ?> specialColumn;
    @FXML private TableColumn<?, ?> priceColumn;
    @FXML private TableColumn<?, ?> statusColumn;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {

        User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

        // Kiểm tra an toàn: Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
        if (currentUser != null && lblUserName != null) {         /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin ở góc phải
            lblUserName.setText(currentUser.getFullName());
            lblUserRole.setText(currentUser.getRole().toUpperCase());
        }
    }

    /// Method này thc hiện khi admin click vào TRANG CHỦ
    @FXML
    public void onBackHomeClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_home.fxml")); // Tải màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi thao tác click vào Phiên đấu giá
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_qlphiendaugia.fxml")); // Tải màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_qlphiendaugia.fxml!");
        }
    }

    /// Method này thực hiện khi click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_history.fxml")); // Tải màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file history.fxml! Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào nút QUẢN LÝ SẢN PHẨM.
    @FXML
    public void onUsersManagementClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlinguoidung.fxml")); // Tải màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào nút CÀI ĐẶT.
    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_setting.fxml")); //Tải file màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }
    
}