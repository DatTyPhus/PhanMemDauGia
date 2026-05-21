package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/// class HomeController này dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình ,và xử lý các yêu cầu từ server.

public class EditProduct implements NetworkClient.MessageListener {


    @FXML private javafx.scene.control.TextField txtProductName;
    @FXML private javafx.scene.control.ComboBox<String> cbCategory;
    @FXML private javafx.scene.control.TextArea txtDescription;
    @FXML private javafx.scene.control.TextField txtStartPrice;
    @FXML private javafx.scene.control.ComboBox<String> cbDuration;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);

            // 1. Nạp dữ liệu cho ComboBox Danh mục đúng 3 dòng bạn cần
            if (cbCategory != null) {
                cbCategory.getItems().addAll("ART", "ELECTRONIC", "VEHICLE");
                cbCategory.setValue("ART"); // Lựa chọn mặc định ban đầu
            }

            // 2. Nạp dữ liệu cho ComboBox Thời lượng đúng các mốc bạn cần
            if (cbDuration != null) {
                cbDuration.getItems().addAll("5p", "15p", "30p", "1h", "2h");
                cbDuration.setValue("5p"); // Lựa chọn mặc định ban đầu
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onSaveClick() {
        try {
            // 1. Kiểm tra thông tin người bán đang đăng nhập
            User currentUser = UserSession.getInstance().getLoginUser();
            if (currentUser == null) {
                showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin phiên đăng nhập. Vui lòng thử lại!");
                return;
            }

            // 2. Thu thập dữ liệu từ các ô nhập liệu trên giao diện
            String name = (txtProductName != null) ? txtProductName.getText().trim() : "";
            String selectedCategory = (cbCategory != null) ? cbCategory.getValue() : null; // Lấy "ART", "ELECTRONIC", hoặc "VEHICLE"
            String description = (txtDescription != null) ? txtDescription.getText().trim() : "";
            String startPriceStr = (txtStartPrice != null) ? txtStartPrice.getText().trim() : "";
            String duration = (cbDuration != null) ? cbDuration.getValue() : null; // Lấy "5p", "15p", "30p", "1h", "2h"

            // 3. Kiểm tra dữ liệu trống (Validation)
            if (name.isEmpty() || selectedCategory == null || startPriceStr.isEmpty() || duration == null) {
                showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tất cả các trường dữ liệu!");
                return;
            }

            // 4. Kiểm tra định dạng số tiền khởi điểm
            java.math.BigDecimal startPrice;
            try {
                startPrice = new java.math.BigDecimal(startPriceStr);
                if (startPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Dữ liệu sai", "Giá khởi điểm phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Sai định dạng", "Giá khởi điểm bắt buộc phải là một dãy số!");
                return;
            }

            // 5. Quy đổi chuỗi thời lượng hiển thị (5p, 1h...) sang số phút nguyên (int) để lưu DB
            int durationMinutes = 5; // Mặc định nếu có lỗi xảy ra
            switch (duration) {
                case "5p":   durationMinutes = 5; break;
                case "15p":  durationMinutes = 15; break;
                case "30p":  durationMinutes = 30; break;
                case "1h":   durationMinutes = 60; break;
                case "2h":   durationMinutes = 120; break;
            }

            // 6. KHỞI TẠO ĐỐI TƯỢNG: Truyền đúng loại "ART", "ELECTRONIC", "VEHICLE" vào hàm createFromType
            com.auction.shared.model.Item newItem = com.auction.shared.model.Item.createFromType(selectedCategory);

            // 7. ĐỔ DỮ LIỆU: Gọi chuẩn xác các hàm set tương thích hoàn toàn với lớp Item.java
            newItem.setSellerId(currentUser.getId());
            newItem.setName(name);
            newItem.setItemType(selectedCategory); // Khớp với trường itemType trong Item.java
            newItem.setDescription(description);
            newItem.setStartingPrice(startPrice);
            newItem.setImageUrl(""); // Tạm thời để trống đường dẫn ảnh
            newItem.setDurationMinutes(durationMinutes); // Gán số phút quy đổi
            newItem.setStatus("PENDING"); // Gắn cờ chờ Admin duyệt theo đúng logic của nhóm

            // 8. CHUYỂN ĐỔI SANG JSON: Sử dụng thư viện Gson
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String jsonPayload = gson.toJson(newItem);

            // 9. BẮN GÓI TIN: Gửi mã hành động "ADD_ITEM" đồng bộ theo thiết kế xử lý của Server
            Message msg = new Message("ADD_ITEM", jsonPayload);
            NetworkClient.getInstance().send(msg);

            // In log kiểm tra tiến trình dưới Console
            System.out.println("\n[CLIENT] Đã nặn đối tượng và bắn lệnh ADD_ITEM lên Server thành công!");
            System.out.println("Nội dung chuỗi JSON gửi đi:\n" + jsonPayload);

            // Hiển thị thông báo thành công cho người dùng trực quan
            showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Thành công", "Đã gửi sản phẩm lên hệ thống! Vui lòng chờ Admin xét duyệt.");

            // 10. Làm sạch biểu mẫu để chuẩn bị cho lượt nhập tiếp theo
            clearForm();

        } catch (Exception e) {
            System.err.println("[CLIENT ERROR] Lỗi khi thực hiện lưu sản phẩm: " + e.getMessage());
            e.printStackTrace();
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    /**
     * Hàm phụ trợ hiển thị nhanh hộp thoại thông báo Pop-up trên giao diện JavaFX
     */
    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hàm dọn sạch nội dung form sau khi hoàn tất gửi dữ liệu lên mạng
     */
    private void clearForm() {
        if (txtProductName != null) txtProductName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtStartPrice != null) txtStartPrice.clear();
        if (cbCategory != null) cbCategory.setValue("ART");
        if (cbDuration != null) cbDuration.setValue("5p");
    }


    /// Method này thực hiện khi thao tác click vào nút chuyển sang màn notification.fxml.
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        try {

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
            System.err.println("Lỗi: Không tìm thấy file phiendaugia.fxml!");
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

    @FXML
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        //  Lấy thông tin người dùng hiện tại từ Session
        com.auction.shared.model.User currentUser = com.auction.client.session.UserSession.getInstance().getLoginUser();

        // KIỂM TRA ROLE ĐỂ VÀO MÀN HÌNH
        if (currentUser != null && "Seller".equalsIgnoreCase(currentUser.getRole())) {         // Nếu đủ điều kiện thfi chuyển màn hiình sang màn quản lý tài sản.
            try {
                NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/product_management.fxml"));
                Parent root = loader.load();

                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                source.getScene().setRoot(root);

                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
                currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi: Không tìm thấy file product_management.fxml!");
            }
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);    //Nếu không đủ điều kiện , hiện lên thông báo để chuyển hướng.
            alert.setTitle("Từ chối truy cập");
            alert.setHeaderText(null);
            alert.setContentText("Xin lỗi, tính năng TÀI SẢN ĐẤU GIÁ chỉ dành riêng cho Người Bán (Seller)!");
            alert.showAndWait();
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

    // ================= PHẦN XỬ LÝ REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        // BẮT BUỘC: Phải đưa lệnh đổi giao diện vào Platform.runLater
        // vì tin nhắn đến từ luồng mạng (Thread khác), nếu đổi trực tiếp sẽ làm sập JavaFX
        javafx.application.Platform.runLater(() -> {

            // Bộ lọc: Chỉ quan tâm đến tin nhắn báo "Cập nhật giá"
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

                        // 2. TẠI ĐÂY LÀ LOGIC ĐỔI GIAO DIỆN CỦA BẠN:
                        // (Ví dụ: Bạn dùng vòng lặp tìm cái Card sản phẩm có ID khớp với productId,
                        // sau đó gọi lệnh set text để cập nhật lại label giá tiền trên cái Card đó)
                    }
                    break;
                case "ADD_ITEM_SUCCESS":
                    // Hiện Popup báo thành công và xóa trắng Form
                    showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Thành công", msg.getPayload().toString());
                    clearForm();
                    break;

                case "ADD_ITEM_FAIL":
                    // Hiện Popup báo lỗi nếu Server trục trặc
                    showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Lỗi", msg.getPayload().toString());
                    break;

                // Các tín hiệu khác tạm thời bỏ qua
                default:
                    break;
            }
        });
    }
}
