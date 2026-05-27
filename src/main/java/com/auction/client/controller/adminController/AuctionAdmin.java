package com.auction.client.controller.adminController;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Auction; // BẮT BUỘC IMPORT FILE MODEL NÀY
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/// class AuctionAdmin dùng để hiển thị và quản lý các phiên đấu giá trên màn hình Admin.
public class AuctionAdmin implements NetworkClient.MessageListener {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    ///  BUG CRITICAL]: Kiểu dữ liệu trong ngoặc nhọn < > PHẢI LÀ 'Auction' (Model), tuyệt đối không dùng 'AuctionAdmin' (Controller)
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, Integer> idColumn;
    @FXML private TableColumn<Auction, String> nameColumn;
    @FXML private TableColumn<Auction, java.math.BigDecimal> priceColumn;
    @FXML private TableColumn<Auction, String> statusColumn;
    @FXML private TableColumn<Auction, String> timeColumn;
    @FXML private TableColumn<Auction, Void> actionColumn;

    @FXML
    public void initialize() {
        try {
            NetworkClient.getInstance().addListener(this);

            User currentUser = UserSession.getInstance().getLoginUser();
            if (currentUser != null && lblUserName != null) {
                lblUserName.setText(currentUser.getFullName());
                if (lblUserRole != null) lblUserRole.setText(currentUser.getRole().toUpperCase());
            }

            /// Ánh xạ dữ liệu vào các cột
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

            /// Bổ sung hàm thiết lập nút bấm cho Cột Thao tác để fix cảnh báo vàng (unused)
            setupActionColumn();

            /// Gửi lệnh lấy dữ liệu
            NetworkClient.getInstance().send(new Message("GET_ALL_AUCTIONS", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Hàm dựng nút Chi tiết] lồng vào cột Thao tác (actionColumn)
    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<Auction, Void>, TableCell<Auction, Void>>() {
            @Override
            public TableCell<Auction, Void> call(TableColumn<Auction, Void> param) {
                return new TableCell<Auction, Void>() {
                    private final Button btnDetail = new Button("Chi tiết");

                    {
                        btnDetail.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                        btnDetail.setOnAction(event -> {
                            Auction targetAuction = getTableView().getItems().get(getIndex());
                            System.out.println("Admin đang xem chi tiết phiên: " + targetAuction.getItemName());
                            // Tại đây sau này bạn có thể gài logic chuyển sang màn hình phòng đấu giá chi tiết
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() >= getTableView().getItems().size() || getTableView().getItems().get(getIndex()) == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDetail);
                        }
                    }
                };
            }
        });
    }

    /// Lắng nghe dữ liệu đổ về từ Server
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            if ("RECEIVE_ALL_AUCTIONS_SUCCESS".equals(msg.getAction())) {
                try {
                    String jsonArrayStr = msg.getPayload().toString();
                    com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonArrayStr).getAsJsonArray();

                    ObservableList<Auction> observableList = FXCollections.observableArrayList();
                    com.google.gson.Gson gson = new com.google.gson.Gson();

                    for (com.google.gson.JsonElement element : jsonArray) {
                        /// [FIX BUG CRITICAL]: Phải giải mã JSON thành đối tượng MODEL 'Auction.class'
                        Auction auction = gson.fromJson(element, Auction.class);
                        if (auction != null) {
                            observableList.add(auction);
                        }
                    }

                    if (auctionTable != null) {
                        auctionTable.setItems(observableList);
                    }
                } catch (Exception e) {
                    System.err.println("[CLIENT ERROR] Lỗi giải mã danh sách phiên đấu giá!");
                    e.printStackTrace();
                }
            }
        });
    }
    /// Method này thực hiện khi admin click vào TRANG CHỦ
    @FXML
    public void onBackHomeClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_home.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào LỊCH SỬ ĐẤU GIÁ
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_history.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file history.fxml! Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào QUẢN LÝ NGƯỜI DÙNG
    @FXML
    public void onUsersManagementClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlinguoidung.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào QUẢN LÝ SẢN PHẨM
    @FXML
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlisanpham.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi admin click vào nút CÀI ĐẶT.
    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        navigate(event, "/com/auction/client/view/admin/admin_setting.fxml");
    }

    /**
     * Hàm tiện ích dùng chung để chuyển trang an toàn, tránh lỗi NullPointerException
     * khi cố gắng lấy Stage từ một Scene đã bị huỷ.
     */
    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); //Tải file màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình

            // BẮT BUỘC: Lấy Stage TRƯỚC KHI thay đổi Root
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            // Thay đổi giao diện
            source.getScene().setRoot(root);

            // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Hãy kiểm tra lại đường dẫn: " + fxmlPath);
        }
    }
}