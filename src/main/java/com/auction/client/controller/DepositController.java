package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

/// class DepositController này dùng để xử lý màn hình nạp tiền: In số dư, lấy số tiền người dùng nhập và gửi lệnh xuống Server.
public class DepositController implements NetworkClient.MessageListener {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;
    @FXML private TextField txtAmount; // Ô cho người dùng nhập số tiền

    private User currentUser;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Nạp tiền được load lên.
    @FXML
    public void initialize() {
        try {
            NetworkClient.getInstance().addListener(this);
            currentUser = UserSession.getInstance().getLoginUser();

            if (currentUser != null) {
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());

                /// Định dạng số dư cũ cho đẹp mắt
                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());      /// Hiển thị số dư.
                lblTopBalance.setText("Số dư: " + formattedBalance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /// Method này thực hiện khi click nút XÁC NHẬN ĐÃ CHUYỂN KHOẢN
    @FXML
    public void onConfirmDeposit(javafx.event.ActionEvent event) {
        String amountStr = txtAmount.getText().trim();

        if (amountStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tiền bạn đã chuyển khoản!");
            return;
        }

        try {
            /// Chuyển chuỗi chữ thành số tiền thực tế
            BigDecimal amount = new BigDecimal(amountStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Lỗi", "Số tiền nạp phải lớn hơn 0!");
                return;
            }

            /// Đóng gói dữ liệu gửi xuống Server (Gồm ID người dùng, vai trò và số tiền)
            /// Format chuỗi gửi đi sẽ là: "ID,Role,Amount"
            String payload = currentUser.getId() + "," + currentUser.getRole() + "," + amount.toString();
            Message msg = new Message("DEPOSIT", payload);

            NetworkClient.getInstance().send(msg);

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền không hợp lệ! Vui lòng chỉ nhập số.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /// Method quay lại trang Hồ sơ
    @FXML
    public void onBackClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/profile.fxml"));
            Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            source.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Lắng nghe phản hồi từ Server
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            if (msg.getAction().equals("DEPOSIT_SUCCESS")) {
                /// Nhận số dư mới từ Server và cập nhật lại kho (UserSession)
                BigDecimal newBalance = new BigDecimal(msg.getPayload().toString());
                currentUser.setBalance(newBalance);

                /// Cập nhật lại giao diện
                lblTopBalance.setText("Số dư: " + String.format("%,.0f VNĐ", newBalance));
                txtAmount.clear();

                showAlert("Thành công", "Nạp tiền thành công! Số dư mới của bạn là: " + String.format("%,.0f VNĐ", newBalance));
            } else if (msg.getAction().equals("DEPOSIT_FAIL")) {
                showAlert("Thất bại", msg.getPayload().toString());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(title.equals("Lỗi") || title.equals("Thất bại") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}