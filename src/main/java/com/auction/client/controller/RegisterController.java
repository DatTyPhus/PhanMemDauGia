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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;

/// Class RegisterController này dùng để xử lý những yêu cầu của người dùng khi thao tác trên màn hình đăng ký, và xử lý các phản hồi từ server để hiển thị lên màn hình.

public class RegisterController implements NetworkClient.MessageListener {

    /// Liên kết với các ô nhập thông tin hay click trên giao diện
    @FXML private TextField fullName;
    @FXML private TextField userName;
    @FXML private PasswordField password;
    @FXML private PasswordField re_password;
    @FXML private Circle role_bidder;
    @FXML private Circle role_seller;

    @FXML private Label lblMessage;            // Thêm 1 Label ẩn trên giao diện để hiện chữ báo lỗi

    /// Biến lưu trữ Role hiện tại mà người dùng đang chọn (Mặc định là BIDDER)
    private String selectedRole = "BIDDER";       // Khi người dùng đăng ký không click vào bất cứ ô chọn role nào thì mặc định sẽ là BIDDER.

    @FXML
    public void initialize() {               /// Khởi tạo đối tượng để nghe phản hồi từ mạng.


        // Tự động tô màu xanh cho vòng tròn Bidder lúc mới mở màn hình
        role_bidder.setFill(Color.DODGERBLUE);
        role_seller.setFill(Color.WHITE);

        // Khi NetworkClient có tín hiệu phản hồi từ server sẽ được đi vào hàm handleServerResponse.
        try {
            /// BẮT BUỘC: Đăng ký màn hình này vào danh sách nghe tín hiệu mạng từ server để nhận lệnh REGISTER_FAIL / REGISTER_SUCCESS
            NetworkClient.getInstance().addListener(this);

            /// Đặt màu ban đầu cho 2 nút chọn Role tròn để người dùng dễ nhìn diện
            role_bidder.setFill(Color.web("#2563eb"));
            role_seller.setFill(Color.TRANSPARENT);
            role_seller.setStroke(Color.web("#cbd5e1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Xoá thông báo lỗi khi người dùng bắt đầu gõ lại vào bất kỳ ô nào
        fullName.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
        userName.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
        password.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
        re_password.textProperty().addListener((obs, oldVal, newVal) -> lblMessage.setText(""));
    }

    // XỬ LÝ CHỌN ROLE (Khi bấm vào hình tròn)
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


    /// XỬ LÝ NÚT TẠO TÀI KHOẢN
    @FXML
    public void onRegisterClick() {
        // Lấy thông tin từ các ô nhập liệu
        String name = fullName.getText();
        String user = userName.getText();
        String pass = password.getText();
        String rePass = re_password.getText();

        // Kiểm tra việc nhập thông tin
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

        // Tên đăng nhập không được chứa khoảng trắng
        if (user.contains(" ")) {
            lblMessage.setText("Tên đăng nhập không được chứa khoảng trắng.");
            lblMessage.setStyle("-fx-text-fill: red;");
            userName.requestFocus();
            return;
        }

        // Tên đăng nhập tối thiểu 4 ký tự
        if (user.length() < 4) {
            lblMessage.setText("Tên đăng nhập phải có ít nhất 4 ký tự.");
            lblMessage.setStyle("-fx-text-fill: red;");
            userName.requestFocus();
            return;
        }

        // Mật khẩu tối thiểu 6 ký tự
        if (pass.length() < 6) {
            lblMessage.setText("Mật khẩu phải có ít nhất 6 ký tự.");
            lblMessage.setStyle("-fx-text-fill: red;");
            password.requestFocus();
            return;
        }

        // Họ tên không được chứa số
        if (name.matches(".*\\d.*")) {
            lblMessage.setText("Họ tên không được chứa chữ số.");
            lblMessage.setStyle("-fx-text-fill: red;");
            fullName.requestFocus();
            return;
        }

        // Đa hình: Khai báo lớp abstract nhưng khởi tạo lớp con
        User newUser;
        if (selectedRole.equals("BIDDER")) {
            newUser = new Bidder(user, pass, name, "BIDDER");
        } else {
            newUser = new Seller(user, pass, name, "SELLER");
        }

        // Đóng gói vào Message với nhãn "REGISTER"
        Message regMsg = new Message("REGISTER", newUser);

        // Gửi yêu cầu đăng ký xuống server để xử lý đi qua NetworkClient
        try {
            NetworkClient.getInstance().send(regMsg);
        } catch (IOException e) {
            lblMessage.setText("Lỗi kết nối mạng!");
        }
    }

    /// XỬ LÝ PHẢN HỒI TỪ SERVER VÀ CHUYỂN MÀN HÌNH.
    /// Lắng nghe phản hồi trả dữ liệu từ Server phát về máy Client
    @Override
    public void onMessageReceived(Message msg) {
        switch (msg.getAction()) {
            case "REGISTER_SUCCESS":
                Platform.runLater(() -> {
                    /// Cập nhật chữ thông báo thành công và chuyển màu sang xanh lá trực tiếp bằng code
                    lblMessage.setText("Đăng ký thành công! Đang quay lại trang đăng nhập...");
                    lblMessage.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13; -fx-font-weight: bold;");

                    /// Gỡ lắng nghe an toàn trước khi chuyển màn hình để tránh rác luồng chạy ngầm
                    try {
                        NetworkClient.getInstance().removeListener(this);
                        onBackToLoginClick();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                break;

            case "REGISTER_FAIL":
                /// Khi Server báo lỗi trùng tên, khối lệnh này sẽ đổ chữ trực tiếp vào nhãn FXML đã được nới rộng diện tích
                Platform.runLater(() -> {
                    lblMessage.setText(msg.getPayload().toString()); /// Đổ văn bản lỗi vào nhãn hiển thị
                    userName.clear();          // Xoá username cũ để người dùng nhập lại tên mới
                    userName.requestFocus();   // Tự động đưa con trỏ chuột nhấp nháy vào ô username
                });
                break;
        }
    }

    /// CHUYỂN VỀ MÀN HÌNH ĐĂNG NHẬP (khi đã đăng ký thành công)
    @FXML
    public void onBackToLoginClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/sample.fxml"));
            Parent root = loader.load();

            // SỬ DỤNG SET_ROOT ĐỂ KHÔNG BỊ GIẬT MÀN HÌNH
            userName.getScene().setRoot(root);

            Stage currentStage = (Stage) userName.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
