package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;

/// Class NotificationController này dùng để xử lý các yêu cầu của người dùng qua thao tác,và nhận tín hiệu phản hồi từ server để hiển thị lên màn hình.

public class NotificationController implements NetworkClient.MessageListener {
    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

            // Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
            if (currentUser != null && lblUserName != null) {         /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin ở góc phải
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());

                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());      /// Hiển thị số dư.
                lblTopBalance.setText("Số dư: " + formattedBalance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /// Hàm này thực hiện khi thao tác quay về trang chủ(home.fxml)
    @FXML
    public void onBackToHomeClick(ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

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
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

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
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        com.auction.shared.model.User currentUser = com.auction.client.session.UserSession.getInstance().getLoginUser();

        if (currentUser != null && "Seller".equalsIgnoreCase(currentUser.getRole())) {
            try {
                NetworkClient.getInstance().removeListener(this);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/product_management.fxml"));
                Parent root = loader.load();

                javafx.scene.Node source = (javafx.scene.Node) event.getSource();

                // BÍ QUYẾT LÀ ĐÂY: Lấy Cửa sổ (Window) TRƯỚC KHI thay ruột
                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

                // Sau đó mới thay giao diện mới vào
                source.getScene().setRoot(root);
                currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi: Không tìm thấy file product_management.fxml!");
            }
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Từ chối truy cập");
            alert.setHeaderText(null);
            alert.setContentText("Xin lỗi, tính năng TÀI SẢN ĐẤU GIÁ chỉ dành riêng cho Người Bán (Seller)!");
            alert.showAndWait();
        }
    }

    /// Method này thực hiện khi người dùng click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/history.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  //Thay đổi màn home thành màn phiendaugia
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file history.fxml! Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi nguười dùng click vào nút HỒ SƠ CỦA TÔI.
    @FXML
    public void onProfileClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            // Tìm file profile.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/profile.fxml"));
            Parent root = loader.load();

            // Thay cửa sổ sang màn Profile
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            // Đặt tiêu đề cửa sổ
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file profile.fxml!");
        }
    }

    /// Method này thực hiện khi click vào nút CÀI ĐẶT
    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            // 1. Tìm bản vẽ setting.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/setting.fxml"));
            Parent root = loader.load();

            // 2. Lấy Scene hiện tại và thay "ruột" bằng trang Cài đặt
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);

            // 3. Đổi tiêu đề cửa sổ
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file setting.fxml!");
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        //  Phải đưa lệnh đổi giao diện vào Platform.runLater vì tin nhắn đến từ luồng mạng (Thread khác), nếu đổi trực tiếp sẽ làm sập JavaFX
        javafx.application.Platform.runLater(() -> {

            switch (msg.getAction()) {
                case "UPDATE_BID":
                    System.out.println("Màn hình Phiên đấu giá đã nhận được tín hiệu!");

                    // 1. Bóc tách dữ liệu (Giả sử Huy gửi chuỗi: "Mã_SP,Giá_Mới,Tên_Người_Đặt")
                    String payloadStr = msg.getPayload().toString();
                    String[] data = payloadStr.split(",");

                    if(data.length == 3) {
                        String productId = data[0];
                        String newPrice = data[1];
                        String bidderName = data[2];

                        System.out.println("Sản phẩm ID: " + productId + " | Giá mới nhảy lên: " + newPrice + " bởi " + bidderName);

                    }
                    break;

                // (Các thông báo như LOGIN_SUCCESS... nó sẽ rơi vào default và bị bỏ qua, không làm loạn màn hình này)
                default:
                    break;
            }
        });
    }
}