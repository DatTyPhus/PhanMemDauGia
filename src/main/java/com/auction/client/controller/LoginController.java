package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField ten_dang_nhap;
    @FXML private PasswordField mat_khau;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        try {
            // Lắng nghe phản hồi đăng nhập từ Server
            NetworkClient.getInstance().setListener(msg -> {
                Platform.runLater(() -> handleServerResponse(msg));
            });
        } catch (IOException e) {
            lblMessage.setText("Lỗi kết nối mạng!");
        }
    }

    // Logic Đăng nhập (Gửi dữ liệu lên Server)
    @FXML
    public void onLoginClick() {
        String user = ten_dang_nhap.getText();
        String pass = mat_khau.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đủ thông tin!");
            return;
        }

        // Tạm thời đóng gói payload đơn giản để test luồng
        Message loginMsg = new Message("LOGIN", user + "," + pass);
        try {
            NetworkClient.getInstance().send(loginMsg);
        } catch (Exception e) {
            lblMessage.setText("Không thể gửi yêu cầu!");
        }
    }

    // ĐÂY LÀ ĐOẠN QUAN TRỌNG NHẤT: Link sang RegisterController
    @FXML
    public void onRegisterLinkClick() {
        try {
            // Chỉ đường cho JavaFX tìm đến file registe.fxml của Bảo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/registe.fxml"));
            Parent root = loader.load();

            // Lấy cửa sổ hiện tại và thay thế bằng giao diện Đăng ký
            Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
            ten_dang_nhap.getScene().setRoot(root);
            currentStage.setTitle("Đăng ký tài khoản");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Kiểm tra lại đường dẫn file registe.fxml");
        }
    }

    private void handleServerResponse(Message msg) {
        if (msg.getAction().equals("LOGIN_SUCCESS")) {
            try {
                // 1. LẤY MÓN QUÀ TỪ SERVER (Lúc này đang bị biến dạng)
                Object payload = msg.getPayload();

                // 1. Chuyển thành chuỗi JSON
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String jsonString = gson.toJson(payload);

                // 🌟 TUYỆT CHIÊU: In thẳng gói hàng ra màn hình để soi xem Server gửi biến gì
                System.out.println("GÓI HÀNG SERVER GỬI VỀ LÀ: " + jsonString);

                // 2. "Đọc trộm" JSON để xem chức vụ là gì
                com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonString).getAsJsonObject();

                String role = "";
                // Kiểm tra an toàn: Xem có chữ "role" in thường không
                if (jsonObject.has("role")) {
                    role = jsonObject.get("role").getAsString();
                }
                // Kiểm tra an toàn: Xem có chữ "Role" in hoa không
                else if (jsonObject.has("Role")) {
                    role = jsonObject.get("Role").getAsString();
                }
                // Nếu vẫn không có, in ra báo động đỏ
                else {
                    System.err.println("CẢNH BÁO: Không tìm thấy chữ 'role' hay 'Role' trong gói hàng!");
                }

                // 3. Dựa vào chức vụ để nặn ra đúng Class con
                com.auction.shared.model.User loggedInUser = null;
                if ("Bidder".equalsIgnoreCase(role)) {
                    loggedInUser = gson.fromJson(jsonString, com.auction.shared.model.Bidder.class);
                } else if ("Seller".equalsIgnoreCase(role)) {
                    loggedInUser = gson.fromJson(jsonString, com.auction.shared.model.Seller.class);
                }

                // 4. Lưu vào Session và chuyển trang (chỉ làm nếu nặn thành công)
                if (loggedInUser != null) {
                    com.auction.client.session.UserSession.getInstance().setLoginUser(loggedInUser);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
                    Parent root = loader.load();
                    Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
                    ten_dang_nhap.getScene().setRoot(root);
                    currentStage.setTitle("Trang chủ Đấu Giá");
                } else {
                    System.err.println("Lỗi: Không nặn được User vì không xác định được Role là gì.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                lblMessage.setText("Lỗi không tìm thấy file home.fxml!");
            }
        } else if (msg.getAction().equals("LOGIN_FAIL")) {
            lblMessage.setText(msg.getPayload().toString());
        }
    }
}