package com.auction.client.controller.adminController;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Item;
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
import java.util.ArrayList;
import java.util.List;

/// class ProductsManagement dùng để thực hiện các yêu cầu kiểm duyệt sản phẩm của Admin, hiển thị danh sách sản phẩm PENDING và nhúng bộ đôi nút bấm Thao tác.

public class ProductsManagement implements NetworkClient.MessageListener {

    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Định hình kiểu dữ liệu thực thể Item rõ ràng cho bảng hiển thị kiểm duyệt để tránh lỗi Object
    @FXML private TableView<Item> reviewTable;
    @FXML private TableColumn<Item, Integer> idColumn;
    @FXML private TableColumn<Item, String> productColumn;
    @FXML private TableColumn<Item, Integer> sellerColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> specialColumn;
    @FXML private TableColumn<Item, java.math.BigDecimal> priceColumn;
    @FXML private TableColumn<Item, String> statusColumn;
    @FXML private TableColumn<Item, Void> actionColumn; // Định dạng cột Thao tác chứa nút bấm đồ họa công khai

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Quản lý sản phẩm của Admin được load lên.
    @FXML
    public void initialize() {
        try {
            /// Đăng ký màn hình này vào danh sách nghe tín hiệu mạng để nhận mảng dữ liệu đổ về
            NetworkClient.getInstance().addListener(this);

            User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

            if (currentUser != null && lblUserName != null) {         /// Lấy dữ liệu người dùng hiện tại để in lên thanh thông tin ở góc phải
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());
            }

            /// Ánh xạ chính xác thuộc tính thực thể vào các cột hiển thị dữ liệu văn bản
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            specialColumn.setCellValueFactory(new PropertyValueFactory<>("specialInfo"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            /// Gọi hàm cấu hình tạo bộ đôi nút Duyệt/Xóa nhét vào cột Thao tác
            setupActionColumn();

            /// TỰ ĐỘNG: Phát lệnh yêu cầu hệ thống tải toàn bộ sản phẩm PENDING từ Server về
            NetworkClient.getInstance().send(new Message("GET_PENDING_ITEMS", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Hàm dựng bộ đôi nút bấm DUYỆT (Xanh) và XÓA (Đỏ) lồng vào khối HBox ngang trong cột thao tác
    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<Item, Void>, TableCell<Item, Void>>() {
            @Override
            public TableCell<Item, Void> call(TableColumn<Item, Void> param) {
                return new TableCell<Item, Void>() {
                    private final Button btnApprove = new Button("✔ DUYỆT");
                    private final Button btnDelete = new Button("🗑 XÓA");
                    private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, btnApprove, btnDelete);

                    {
                        /// Thiết kế trang trí màu sắc, bo góc bám sát giao diện quản trị hiện tại
                        btnApprove.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12;");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12;");
                        pane.setAlignment(javafx.geometry.Pos.CENTER);

                        /// Cài đặt hành động xử lý khi click chuột vào nút [ DUYỆT]
                        btnApprove.setOnAction(event -> {
                            /// Ép kiểu tường minh qua bảng reviewTable để chặn đứng lỗi không nhận diện được Class Item
                            Item targetItem = (Item) reviewTable.getItems().get(getIndex());
                            if (targetItem != null) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Xác nhận phê duyệt");
                                alert.setHeaderText("Phê duyệt sản phẩm: " + targetItem.getName() + " ?");
                                alert.setContentText("Hệ thống sẽ chuyển trạng thái sang ACTIVE và đưa lên sàn đấu giá công khai.");

                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        try {
                                            NetworkClient.getInstance().send(new Message("APPROVE_ITEM", String.valueOf(targetItem.getId())));
                                        } catch (Exception e) { e.printStackTrace(); }
                                    }
                                });
                            }
                        });

                        /// Cài đặt hành động xử lý khi click chuột vào nút [XÓA]
                        btnDelete.setOnAction(event -> {
                            /// Ép kiểu tường minh qua bảng reviewTable để chặn đứng lỗi không nhận diện được Class Item
                            Item targetItem = (Item) reviewTable.getItems().get(getIndex());
                            if (targetItem != null) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Xác nhận từ chối");
                                alert.setHeaderText("Từ chối và Xóa sản phẩm: " + targetItem.getName() + " ?");
                                alert.setContentText("Hành động này không thể hoàn tác, sản phẩm sẽ bị gỡ bỏ khỏi Database.");

                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        try {
                                            NetworkClient.getInstance().send(new Message("DELETE_ITEM", String.valueOf(targetItem.getId())));
                                        } catch (Exception e) { e.printStackTrace(); }
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() >= reviewTable.getItems().size() || reviewTable.getItems().get(getIndex()) == null) {
                            setGraphic(null); /// Nếu dòng trống rỗng hoàn toàn thì ẩn hộp nút đi
                        } else {
                            setGraphic(pane); /// Nhét hộp chứa 2 nút vào hiển thị công khai ở cột thao tác
                        }
                    }
                };
            }
        });
    }

    /// Lắng nghe các gói dữ liệu mạng phản hồi từ trạm Server phát về máy Client
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "RECEIVE_PENDING_ITEMS_SUCCESS":
                    try {
                        String jsonArrayStr = msg.getPayload().toString();
                        com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonArrayStr).getAsJsonArray();

                        ObservableList<Item> observableList = FXCollections.observableArrayList();
                        com.google.gson.Gson gson = new com.google.gson.Gson();

                        for (com.google.gson.JsonElement element : jsonArray) {
                            com.google.gson.JsonObject obj = element.getAsJsonObject();
                            if (obj.has("itemType") && !obj.get("itemType").isJsonNull()) {
                                String type = obj.get("itemType").getAsString();
                                Item item = null;

                                if ("ART".equalsIgnoreCase(type)) {
                                    item = gson.fromJson(obj, com.auction.shared.model.Art.class);
                                } else if ("ELECTRONIC".equalsIgnoreCase(type)) {
                                    item = gson.fromJson(obj, com.auction.shared.model.Electronics.class);
                                } else if ("VEHICLE".equalsIgnoreCase(type)) {
                                    item = gson.fromJson(obj, com.auction.shared.model.Vehicle.class);
                                }

                                if (item != null) {
                                    observableList.add(item);
                                }
                            }
                        }

                        if (reviewTable != null) {
                            reviewTable.setItems(observableList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "APPROVE_ITEM_SUCCESS":
                case "DELETE_ITEM_SUCCESS":
                    /// Bật hộp thông báo thao tác thành công dữ liệu
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thao tác thành công");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText(msg.getPayload().toString());
                    successAlert.showAndWait();

                    try {
                        /// Tự động gọi phát lệnh quét lại danh sách để đồng bộ giật bảng biến mất dòng vừa làm xong
                        NetworkClient.getInstance().send(new Message("GET_PENDING_ITEMS", ""));
                    } catch (Exception e) { e.printStackTrace(); }
                    break;

                case "APPROVE_ITEM_FAIL":
                case "DELETE_ITEM_FAIL":
                    Alert failAlert = new Alert(Alert.AlertType.ERROR);
                    failAlert.setTitle("Thao tác thất bại");
                    failAlert.setHeaderText(null);
                    failAlert.setContentText(msg.getPayload().toString());
                    failAlert.showAndWait();
                    break;
            }
        });
    }

    /// Method này thc hiện khi admin click vào TRANG CHỦ
    @FXML
    public void onBackHomeClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_home.fxml")); // Tải màn hình
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

    /// Method này thực hiện khi thao tác click vào Phiên đấu giá
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_qlphiendaugia.fxml")); // Tải màn hình
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  // Thay đổi màn hình
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file admin_qlphiendaugia.fxml!");
        }
    }

    /// Method này thực hiện khi click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_history.fxml")); // Tải màn hình
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

    /// Method này thực hiện khi admin click vào nút QUẢN LÝ SẢN PHẨM.
    @FXML
    public void onUsersManagementClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_quanlinguoidung.fxml")); // Tải màn hình
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
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_setting.fxml")); //Tải file màn hình
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
    
}