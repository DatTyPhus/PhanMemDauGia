package com.auction.client.controller;

import java.io.IOException;
import com.auction.client.network.NetworkClient;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/// Class LoginController này dùng để thực hiện các yêu cầu của người dùng qua các thao tác trên màn hình ,và xử lý các phản hồi từ server.

public class LoginController implements NetworkClient.MessageListener {

    @FXML private TextField ten_dang_nhap;
    @FXML private PasswordField mat_khau;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {          /// Khởi tạo,chạy ngay khi chuyển qua màn Login
        try {
            //Lắng nghe phản hồi đăng nhập từ Server bằng chính class này (this)
            NetworkClient.getInstance().addListener(this);
        } catch (Exception e) {
            lblMessage.setText("Lỗi kết nối mạng!");
        }

        // Xoá thông báo lỗi khi người dùng bắt đầu gõ lại
        ten_dang_nhap.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
        mat_khau.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
    }

    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> handleServerResponse(msg));
    }

    /// Hàm này thực hiện khi người dùng click vào nút "Đăng nhập"
    @FXML
    public void onLoginClick() {
        String user = ten_dang_nhap.getText();
        String pass = mat_khau.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đủ thông tin!");
            return;
        }

        // Tên đăng nhập không được chứa khoảng trắng
        if (user.contains(" ")) {
            lblMessage.setText("Tên đăng nhập không được chứa khoảng trắng.");
            ten_dang_nhap.requestFocus();
            return;
        }

        // Tên đăng nhập tối thiểu 4 ký tự
        if (user.length() < 4) {
            lblMessage.setText("Tên đăng nhập phải có ít nhất 4 ký tự.");
            ten_dang_nhap.requestFocus();
            return;
        }

        // Mật khẩu tối thiểu 6 ký tự
        if (pass.length() < 6) {
            lblMessage.setText("Mật khẩu phải có ít nhất 6 ký tự.");
            mat_khau.requestFocus();
            return;
        }

        Message loginMsg = new Message("LOGIN", user + "," + pass);
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
            // Ngắt kết nối lắng nghe trước khi sang màn hình Đăng ký
            NetworkClient.getInstance().removeListener(this);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/registe.fxml"));
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
                Gson gson = new Gson();
                String jsonString = gson.toJson(payload);

                System.out.println("THÔNG TIN SERVER GỬI VỀ LÀ: " + jsonString);

                // Đọc xem thông tin từ JSON
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

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
                User loggedInUser = null;
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

                    // Ngắt kết nối lắng nghe trước khi sang màn hình Home
                    NetworkClient.getInstance().removeListener(this);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
                    Parent root = loader.load();
                    Stage currentStage = (Stage) ten_dang_nhap.getScene().getWindow();
                    ten_dang_nhap.getScene().setRoot(root);
                    currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
                } else if ("Admin".equalsIgnoreCase(role)) {
                    com.auction.client.session.UserSession.getInstance().setLoginUser(loggedInUser);

                    // Ngắt kết nối lắng nghe trước khi sang màn hình Admin Home
                    NetworkClient.getInstance().removeListener(this);

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
            mat_khau.clear();          // 1. Phải xóa mật khẩu trước (sự kiện xóa chữ sẽ chạy ở đây)

            lblMessage.setText(msg.getPayload().toString()); // 2. Sau đó mới in lỗi (đè lên chữ rỗng)

            mat_khau.requestFocus();   // 3. Đưa con trỏ chuột vào ô mật khẩu
        }
    }
}