package com.auction.client.controller.adminController;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/// class Home dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình ,và xử lý các yêu cầu từ server.

public class Home implements NetworkClient.MessageListener {

    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblTotalAuctions;
    @FXML private Label lblTotalUsers; // Biến hiển thị số lượng thành viên online

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Home của Admin được load lên.
    @FXML
    public void initialize() {
        try {
            /// Đăng ký màn hình này vào danh sách nghe tín hiệu mạng từ server
            NetworkClient.getInstance().addListener(this);

            User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

            // Kiểm tra an toàn: Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
            if (currentUser != null && lblUserName != null) {         /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin ở góc phải
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());
            }

            ///  Gửi lệnh yêu cầu Server báo cáo số lượng người kết nối ngay khi vừa mở màn hình lên
            NetworkClient.getInstance().send(new Message("GET_ONLINE_COUNT", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CÁC HÀM CHUYỂN TRANG (ĐÃ THÊM LỆNH GỠ LẮNG NGHE AN TOÀN) =================

    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this); /// Gỡ màn hình khỏi danh sách nghe tín hiệu mạng trước khi rời trang

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_qlphiendaugia.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_qlphiendaugia.fxml");
        }
    }

    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this); /// Gỡ màn hình khỏi danh sách nghe tín hiệu mạng trước khi rời trang

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_history.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_history.fxml");
        }
    }

    @FXML
    public void onUsersManagementClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this); /// Gỡ màn hình khỏi danh sách nghe tín hiệu mạng trước khi rời trang

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlinguoidung.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_quanlinguoidung.fxml");
        }
    }

    @FXML
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this); /// Gỡ màn hình khỏi danh sách nghe tín hiệu mạng trước khi rời trang

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlisanpham.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_quanlisanpham.fxml");
        }
    }

    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this); /// Gỡ màn hình khỏi danh sách nghe tín hiệu mạng trước khi rời trang

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_setting.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_setting.fxml");
        }
    }

    // ================= PHẦN LẮNG NGHE REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        /// Thực hiện các lệnh liên quan đến giao diện phải đi qua luồng của javafx, nếu đi bằng luồng mạng bình thường thif sẽ sập javavFX
        javafx.application.Platform.runLater(() -> {
            switch (msg.getAction()) {              // So sánh các nhãn dán (Action) được gửi lên,nếu có lệnh xử lý sẽ thực hiện thay đổi trong giao diện,nếu không có sẽ bỏ qua nhãn dán ấy.
                case "UPDATE_ONLINE_COUNT":
                    /// Server gửi về con số người dùng đang online, ta đắp nó lên giao diện
                    if (lblTotalUsers != null) {
                        lblTotalUsers.setText(msg.getPayload().toString());
                    }
                    break;
                default:
                    break;
            }
        });
    }
}