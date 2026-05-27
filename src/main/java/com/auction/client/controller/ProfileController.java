package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/// class ProfileController này dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình HỒ SƠ CỦA TÔI.

public class ProfileController implements NetworkClient.MessageListener {

    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;    // Số dư nhỏ ở góc phải trên cùng
    @FXML private Label lblCenterBalance; // Số dư to ở giữa màn hình dưới Avatar
    @FXML private Label lblCenterName;    // Tên hiển thị dưới Avatar

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Hồ sơ được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

            // Kiểm tra an toàn: Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
            if (currentUser != null) {

                /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin
                if(lblUserName != null) lblUserName.setText(currentUser.getFullName());
                if(lblUserRole != null) lblUserRole.setText(currentUser.getRole());
                if(lblCenterName != null) lblCenterName.setText(currentUser.getFullName());

                /// Gán số số dư vào khung người dùng.
                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());
                if(lblTopBalance != null) lblTopBalance.setText("Số dư: " + formattedBalance);
                if(lblCenterBalance != null) lblCenterBalance.setText(formattedBalance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /// Method này thực hiện khi người dùng click vào nút NẠP TIỀN NGAY.
    @FXML
    public void onDepositClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/deposit.fxml"));
            Parent root = loader.load();

            // Thay đổi màn hồ sơ thành màn nạp tiền.
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow(); // Bắt buộc lấy Stage trước khi thay root để tránh sập màn

            source.getScene().setRoot(root);
            currentStage.setTitle("NẠP TIỀN VÀO TÀI KHOẢN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file deposit.fxml! Bạn nhớ tạo file này ở bước tiếp theo nhé.");
        }
    }


    /// Method này thực hiện khi thao tác click vào Trang chủ
    @FXML
    public void onBackToHomeClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  //Thay đổi màn hình phiendaugia thành màn home.
            source.getScene().setRoot(root);

            //Đặt lại tiêu đề cho window.
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không thể tải trang chủ!");
        }
    }

    /// Method này thực hiện khi thao tác click vào nút chuyển sang màn notification.fxml.
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/notification.fxml"));

            Parent root = loader.load();                ///Thay đổi màn home thành màn notification.
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file notification.fxml");
        }
    }

    /// Method này thực hiện khi thao tác click vào Phiên đấu giá
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/phiendaugia.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  //Thay đổi màn home thành màn phiendaugia
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file phiendaugia.fxml! Hãy kiểm tra lại đường dẫn.");
        }
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
                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

                // Sau đó mới thay giao diện mới vào
                source.getScene().setRoot(root);
                currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi: Không tìm thấy file product_management.fxml!");
            }
        } else {            /// Báo lỗi khi người dùng không phải là role seller click chuyêển màn sang QUẢN LÝ TÀI SẢN.
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

    // ================= PHẦN XỬ LÝ REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        // Đưa lệnh đổi giao diện vào Platform.runLater.
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


                default:
                    break;
            }
        });
    }
}