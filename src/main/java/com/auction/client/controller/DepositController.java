package com.auction.client.controller;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

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
                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());
                lblTopBalance.setText("Số dư: " + formattedBalance);
            }

            // BẢO MẬT MỨC 1: RÀNG BUỘC TRÊN GIAO DIỆN (CHẶN GÕ CHỮ) 
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String text = change.getText();
                if (text.matches("[0-9]*")) {
                    return change;
                }
                return null; 
            };
            TextFormatter<String> textFormatter = new TextFormatter<>(filter);
            txtAmount.setTextFormatter(textFormatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Sự kiện xảy ra khi người dùng bấm nút "NẠP TIỀN"
    @FXML
    public void onDepositClick(ActionEvent event) {
        String amountStr = txtAmount.getText().trim();

        // ================= BẢO MẬT MỨC 1: INPUT VALIDATION =================
        if (amountStr.isEmpty()) {
            showAlert("Lỗi dữ liệu", "Vui lòng nhập số tiền bạn muốn nạp!");
            txtAmount.requestFocus();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            showAlert("Lỗi dữ liệu", "Số tiền nhập vào không hợp lệ! Vui lòng chỉ nhập số từ 0-9.");
            txtAmount.requestFocus();
            return;
        }

        BigDecimal minDeposit = new BigDecimal("10000"); // Tối thiểu 10k
        if (amount.compareTo(minDeposit) < 0) {
            showAlert("Lỗi dữ liệu", "Số tiền nạp tối thiểu mỗi lần phải từ 10,000 VNĐ!");
            txtAmount.requestFocus();
            return;
        }

        BigDecimal maxDeposit = new BigDecimal("1000000000"); // Tối đa 1 tỷ
        if (amount.compareTo(maxDeposit) > 0) {
            showAlert("Lỗi dữ liệu", "Số tiền nạp một lần không được vượt quá 1,000,000,000 VNĐ!");
            txtAmount.requestFocus();
            return;
        }

        // ĐÃ ĐỔI TÊN HÀM THÀNH send(depositMsg) CHO ĐÚNG VỚI FILE NetworkClient.java CỦA BẠN
        try {
            Message depositMsg = new Message("DEPOSIT", amount.toString());
            NetworkClient.getInstance().send(depositMsg); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Sự kiện bấm nút QUAY LẠI
    @FXML
    public void onBackClick(ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();
            Node source = (Node) event.getSource();
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
                BigDecimal newBalance = new BigDecimal(msg.getPayload().toString());
                currentUser.setBalance(newBalance);

                lblTopBalance.setText("Số dư: " + String.format("%,.0f VNĐ", newBalance));
                txtAmount.clear();

                showAlert("Thành công", "Nạp tiền thành công! Số dư mới của bạn là: " + String.format("%,.0f VNĐ", newBalance));
            } else if (msg.getAction().equals("DEPOSIT_FAIL")) {
                showAlert("Thất bại", msg.getPayload().toString());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(title.equals("Lỗi") || title.equals("Thất bại") || title.equals("Lỗi dữ liệu") 
                ? Alert.AlertType.ERROR 
                : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}