package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.shared.model.Bidder;
import com.auction.shared.model.Seller;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    // 1. Móc nối với các thành phần trên giao diện
    @FXML private TextField fullName;
    @FXML private TextField userName;
    @FXML private PasswordField password;
    @FXML private PasswordField re_password;
    @FXML private Circle role_bidder;
    @FXML private Circle role_seller;

    @FXML private Label lblMessage;            // Thêm 1 Label ẩn trên giao diện để hiện chữ báo lỗi

    // Biến lưu trữ Role hiện tại mà người dùng đang chọn (Mặc định là BIDDER)
    private String selectedRole = "BIDDER";

    // 2. KHỞI TẠO: CẮM PHÍCH LẮNG NGHE MẠNG
    @FXML
    public void initialize() {
        // Tự động tô màu xanh cho vòng tròn Bidder lúc mới mở màn hình
        role_bidder.setFill(Color.DODGERBLUE);
        role_seller.setFill(Color.WHITE);

        // Kêu NetworkClient: "Có tin nhắn gửi về thì ném vào hàm handleServerResponse cho tôi"
        try{
            NetworkClient.getInstance().setListener(new NetworkClient.MessageListener() {
                @Override
                public void onMessageReceived(Message msg) {
                    Platform.runLater(() -> handleServerResponse(msg));
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // 3. XỬ LÝ CHỌN ROLE (Khi bấm vào hình tròn)
    @FXML
    public void onBidderSelected() {
        selectedRole = "BIDDER";
        role_bidder.setFill(Color.DODGERBLUE); // Tô xanh
        role_seller.setFill(Color.WHITE);      // Tô trắng
    }

    @FXML
    public void onSellerSelected() {
        selectedRole = "SELLER";
        role_seller.setFill(Color.DODGERBLUE);
        role_bidder.setFill(Color.WHITE);
    }

    // 4. XỬ LÝ NÚT TẠO TÀI KHOẢN
    @FXML
    public void onRegisterClick() {
        // Lấy thông tin từ các ô nhập liệu
        String name = fullName.getText();
        String user = userName.getText();
        String pass = password.getText();
        String rePass = re_password.getText();

        // Kiểm tra tính hợp lệ cơ bản
        if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || rePass.isEmpty()) {
            lblMessage.setText("Vui lòng điền đầy đủ thông tin!");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        // Kiểm tra mật khẩu nhập lại đã đúng chưa.
        if (!pass.equals(rePass)) {
            lblMessage.setText("Mật khẩu nhập lại không khớp!");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        // 1. Logic Đa hình: Khai báo lớp abstract nhưng khởi tạo lớp con
        User newUser;
        if (selectedRole.equals("BIDDER")) {
            newUser = new Bidder(user, pass, name, "BIDDER");
        } else {
            newUser = new Seller(user, pass, name, "SELLER");
        }

        // 2. Đóng gói vào Message với nhãn "REGISTER"
        Message regMsg = new Message("REGISTER", newUser);

        // 3. Gửi đi qua NetworkClient
        try {
            NetworkClient.getInstance().send(regMsg);
        } catch (IOException e) {
            lblMessage.setText("Lỗi kết nối mạng!");
        }
    }

    // 5. XỬ LÝ PHẢN HỒI TỪ SERVER VÀ CHUYỂN MÀN HÌNH
    private void handleServerResponse(Message msg) {
        // Kiểm tra nhãn của kiện hàng trả về
        switch (msg.getAction()) {
            case "REGISTER_SUCCESS":
                // 1. Thông báo cho người dùng
                System.out.println("Đăng ký thành công!");

                // 2. Logic chuyển màn hình: Quay lại Đăng nhập
                // Vì việc chuyển màn hình tác động đến UI, ta phải dùng Platform.runLater
                Platform.runLater(() -> {
                    try {
                        // Tải file fxml của màn hình đăng nhập
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/sample.fxml"));
                        Parent root = loader.load();

                        // Lấy Stage (cửa sổ) hiện tại từ bất kỳ component nào (ví dụ: userName)
                        Stage currentStage = (Stage) userName.getScene().getWindow();

                        // Thay thế cảnh (Scene) hiện tại bằng cảnh Đăng nhập
                        currentStage.setScene(new Scene(root, 1400, 800));
                        currentStage.setTitle("Đăng nhập hệ thống");
                        currentStage.centerOnScreen();

                    } catch (IOException e) {
                        System.err.println("Không thể chuyển màn hình: " + e.getMessage());
                    }
                });
                break;

            case "REGISTER_FAIL":
                // Nếu thất bại (ví dụ trùng tên user), hiển thị lỗi lên màn hình
                Platform.runLater(() -> {
                    lblMessage.setText(msg.getPayload().toString());
                    lblMessage.setStyle("-fx-text-fill: red;");
                });
                break;
        }
    }

    // 6. CHUYỂN VỀ MÀN HÌNH ĐĂNG NHẬP
    // 6. CHUYỂN VỀ MÀN HÌNH ĐĂNG NHẬP
    @FXML
    public void onBackToLoginClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/sample.fxml"));
            Parent root = loader.load();

            // SỬ DỤNG SET_ROOT ĐỂ KHÔNG BỊ GIẬT MÀN HÌNH
            userName.getScene().setRoot(root);

            // Sửa lại tiêu đề cho đúng
            Stage currentStage = (Stage) userName.getScene().getWindow();
            currentStage.setTitle("Đăng nhập hệ thống");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
