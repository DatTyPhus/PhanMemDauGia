package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

/// Class LoginController này dùng để thực hiện các yêu cầu của người dùng qua các thao tác trên màn hình ,và xử lý các phản hồi từ server.

public class LoginController {

    @FXML private TextField ten_dang_nhap;
    @FXML private PasswordField mat_khau;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {          /// Khởi tạo,chạy ngay khi chuyển qua màn Login
        try {
            // Lắng nghe phản hồi đăng nhập từ Server
            NetworkClient.getInstance().setListener(msg -> {
                Platform.runLater(() -> handleServerResponse(msg));
            });
        } catch (IOException e) {
            lblMessage.setText("Lỗi kết nối mạng!");
        }
    }

    /// Hàm này thực hiện khi người dùng click vào nút "Đăng nhập" ->Kiểm tra về việc nhập thông tin,và gửi yêu cầu muốn đăng nhập vào hệ thống xuống server xử lý.
    @FXML
    public void onLoginClick() {
        String user = ten_dang_nhap.getText();
        String pass = mat_khau.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đủ thông tin!");
            return;
        }

        Message loginMsg = new Message("LOGIN", user + "," + pass);  // Đóng gói Message chứa thông tin về username,password đẩy xuống server để xử lý.
        try {
            NetworkClient.getInstance().send(loginMsg);
        } catch (Exception e) {
            lblMessage.setText("Không thể gửi yêu cầu!");
        }
    }

    /// Hàm này thực hiện khi người dùng click vào nút Đăng ký.
    @FXML
    public void onRegisterLinkClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/registe.fxml"));  // Tải file registe.fxml để chuyển màn.
            Parent root = loader.load();

            //Thay thế màn hình đăng nhập sang màn hình đăng ký.
            Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
            ten_dang_nhap.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Kiểm tra lại đường dẫn file registe.fxml");
        }
    }


    private void handleServerResponse(Message msg) {
        if (msg.getAction().equals("LOGIN_SUCCESS")) {
            try {

                Object payload = msg.getPayload();    // Lấy thông tin từ phản hồi từ server.

                // 1. Chuyển thành chuỗi JSON
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String jsonString = gson.toJson(payload);

                System.out.println("THÔNG TIN SERVER GỬI VỀ LÀ: " + jsonString);

                // Đọc xem thông tin từ JSON
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

                // Dựa vào role để tạo ra class con chuẩn.
                com.auction.shared.model.User loggedInUser = null;
                if ("Bidder".equalsIgnoreCase(role)) {
                    loggedInUser = gson.fromJson(jsonString, com.auction.shared.model.Bidder.class);
                } else if ("Seller".equalsIgnoreCase(role)) {
                    loggedInUser = gson.fromJson(jsonString, com.auction.shared.model.Seller.class);
                } else if ("Admin".equalsIgnoreCase(role)) {
                    loggedInUser = gson.fromJson(jsonString, com.auction.shared.model.Admin.class);
                }

                // Nếu tạo thành công thì lưu đối tượng vào kho (UserSession).
                if ("Bidder".equalsIgnoreCase(role) || "Seller".equalsIgnoreCase(role)) {
                    com.auction.client.session.UserSession.getInstance().setLoginUser(loggedInUser);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
                    Parent root = loader.load();
                    Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
                    ten_dang_nhap.getScene().setRoot(root);
                    currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
                } else if ("Admin".equalsIgnoreCase(role)) {
                    com.auction.client.session.UserSession.getInstance().setLoginUser(loggedInUser);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_home.fxml"));
                    Parent root = loader.load();
                    Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
                    ten_dang_nhap.getScene().setRoot(root);
                    currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
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