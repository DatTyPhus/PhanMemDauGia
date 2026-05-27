package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/// class AuctionHistoryController điều khiển màn hình LỊCH SỬ ĐẤU GIÁ (Chỉ hiện các trận THẮNG)
public class AuctionHistoryController implements NetworkClient.MessageListener {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;

    /// Khai báo bảng và danh sách dữ liệu
    @FXML private TableView<HistoryRow> historyTable;
    private ObservableList<HistoryRow> historyDataList = FXCollections.observableArrayList();

    /// Lớp nội bộ để biểu diễn 1 dòng trong bảng
    public static class HistoryRow {
        String auctionId;
        String itemName;
        String endTime;
        String highestPrice;
        String result;
        Button actionButton;

        public HistoryRow(String auctionId, String itemName, String endTime,String highestPrice, String result, Button actionButton) {
            this.auctionId = auctionId;
            this.itemName = itemName;
            this.endTime = endTime;
            this.highestPrice = highestPrice;
            this.result = result;
            this.actionButton = actionButton;
        }
    }

    /// Hàm chạy khi vào màn.
    @FXML
    public void initialize() {
        try {
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();

            if (currentUser != null && lblUserName != null) {
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());
                lblTopBalance.setText(String.format("Số dư: %,.0f VNĐ", currentUser.getBalance()));

                /// Thiết lập link dữ liệu cho 6 cột trong Bảng
                if (historyTable != null && historyTable.getColumns().size() >= 6) {
                    TableColumn<HistoryRow, String> colId = (TableColumn<HistoryRow, String>) historyTable.getColumns().get(0);
                    TableColumn<HistoryRow, String> colName = (TableColumn<HistoryRow, String>) historyTable.getColumns().get(1);
                    TableColumn<HistoryRow, String> colEnd = (TableColumn<HistoryRow, String>) historyTable.getColumns().get(2);
                    TableColumn<HistoryRow, String> colPrice = (TableColumn<HistoryRow, String>) historyTable.getColumns().get(3);
                    TableColumn<HistoryRow, String> colResult = (TableColumn<HistoryRow, String>) historyTable.getColumns().get(4);
                    TableColumn<HistoryRow, Button> colAction = (TableColumn<HistoryRow, Button>) historyTable.getColumns().get(5);

                    colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().auctionId));
                    colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().itemName));
                    colEnd.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().endTime));
                    colPrice.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().highestPrice));
                    colResult.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().result));
                    colAction.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().actionButton));

                    historyTable.setItems(historyDataList);
                }

                /// Gửi lệnh xuống Server lấy danh sách các phiên đấu giá đã THẮNG của user này
                NetworkClient.getInstance().send(new Message("GET_WIN_HISTORY", currentUser.getUsername()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Nhận tín hiệu từ server.
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "RECEIVE_WIN_HISTORY":     // Nhận dữ liệu những cuộc đấu giá mà mình thắng cuộc.
                    try {
                        String payloadRaw = msg.getPayload().toString();
                        System.out.println("[CLIENT] Nhận dữ liệu lịch sử từ Server: " + payloadRaw);

                        JsonArray historyArray = JsonParser.parseString(payloadRaw).getAsJsonArray();
                        historyDataList.clear();

                        for (JsonElement element : historyArray) {      // Lấy từng lịch sử để chèn theo từng dòng
                            JsonObject row = element.getAsJsonObject();

                            String aId = row.has("auctionId") && !row.get("auctionId").isJsonNull() ? row.get("auctionId").getAsString() : "N/A";
                            String iName = row.has("itemName") && !row.get("itemName").isJsonNull() ? row.get("itemName").getAsString() : "Sản phẩm ẩn";
                            String eTime = row.has("endTime") && !row.get("endTime").isJsonNull() ? row.get("endTime").getAsString() : "00:00:00";
                            String mPrice = "0 VNĐ";
                            if (row.has("currentPrice") && !row.get("currentPrice").isJsonNull()) {
                                java.math.BigDecimal price = row.get("currentPrice").getAsBigDecimal();
                                mPrice = String.format("%,.0f VNĐ", price);
                            }

                            String res = "🏆 Thắng";

                            Button deleteBtn = new Button("🗑 Xóa");
                            deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
                            deleteBtn.setOnAction(e -> {
                                historyDataList.removeIf(item -> item.auctionId.equals(aId));
                            });

                            historyDataList.add(new HistoryRow(aId, iName, eTime, mPrice, res, deleteBtn));
                        }

                        historyTable.refresh();   // Bảng load lại ngay lập tức

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /// Tự động cập nhật lại bảng khi có một cuộc đấu giá kết thúc
                case "AUCTION_ENDED":
                    User currentUser = UserSession.getInstance().getLoginUser();
                    if (currentUser != null) {
                        try {
                            NetworkClient.getInstance().send(new Message("GET_WIN_HISTORY", currentUser.getUsername()));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        });
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
}
