package com.auction.client.controller.adminController;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
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

/// class UsersManagement dùng để quản lý tài khoản người dùng, thống kê số lượng và xử lý yêu cầu xóa thành viên từ Admin.

public class UsersManagement implements NetworkClient.MessageListener {

    // Các biến dùng để link các nút từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalAccounts; /// Nhãn thống kê số lượng tài khoản

    /// Các cột dữ liệu liên kết bảng (Đã loại bỏ hoàn toàn createdColumn theo yêu cầu)
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionColumn; /// Cột chứa nút bấm xóa tài khoản

    /// Hàm khởi tạo chạy tự động khi giao diện quản lý người dùng được load lên
    @FXML
    public void initialize() {
        try {
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();

            if (currentUser != null && lblUserName != null) {
                lblUserName.setText(currentUser.getFullName());
                lblUserRole.setText(currentUser.getRole());
            }

            /// Ánh xạ dữ liệu từ thực thể User vào các cột tương ứng trên TableView
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("username")); // Hiển thị username (tên đăng nhập)
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

            /// Đặt mặc định trạng thái tài khoản
            statusColumn.setCellFactory(column -> new TableCell<User, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableView().getItems().get(getIndex()) == null) {
                        setText(null);
                    } else {
                        setText("● Hoạt động");
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    }
                }
            });

            /// Gọi hàm sinh nút bấm Xóa ở cột Thao tác
            setupActionColumn();

            /// Tự động phát lệnh yêu cầu Server trả về danh sách tất cả người dùng
            NetworkClient.getInstance().send(new Message("GET_ALL_USERS", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Hàm dựng nút bấm Xóa tài khoản đồ họa tiệp màu đỏ vào cột thao tác
    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button btnDelete = new Button("🗑 XÓA");

                    {
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> {
                            User targetUser = getTableView().getItems().get(getIndex());

                            /// Hiển thị hộp thoại xác nhận tránh admin lỡ tay ấn nhầm
                            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmation.setTitle("Cảnh báo khẩn cấp");
                            confirmation.setHeaderText("Xác nhận xóa tài khoản: " + targetUser.getFullName() + "?");
                            confirmation.setContentText("Dữ liệu tài khoản này sẽ bị xóa vĩnh viễn khỏi dữ liệu hệ thống!");

                            confirmation.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    try {
                                        /// Gửi chuỗi định danh ghép "ID,ROLE" lên server để xóa
                                        String payload = targetUser.getId() + "," + targetUser.getRole();
                                        NetworkClient.getInstance().send(new Message("DELETE_USER", payload));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDelete);
                            setAlignment(javafx.geometry.Pos.CENTER);
                        }
                    }
                };
            }
        });
    }

    /// Lắng nghe phản hồi trả dữ liệu Realtime đồng bộ từ trạm Server phát về
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "RECEIVE_ALL_USERS_SUCCESS":
                    try {
                        /// Lấy chuỗi JSON mảng người dùng đổ về từ Server
                        String jsonArrayStr = msg.getPayload().toString();
                        com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonArrayStr).getAsJsonArray();

                        ObservableList<User> observableList = FXCollections.observableArrayList();
                        com.google.gson.Gson gson = new com.google.gson.Gson();

                        /// Duyệt qua từng phần tử trong mảng JSON để phân loại lớp con
                        for (com.google.gson.JsonElement element : jsonArray) {
                            com.google.gson.JsonObject obj = element.getAsJsonObject();

                            /// Kiểm tra an toàn xem đối tượng JSON có chứa thuộc tính vai trò (role) không
                            if (obj.has("role") && !obj.get("role").isJsonNull()) {
                                String role = obj.get("role").getAsString();

                                User user = null;
                                /// Dựa vào giá trị vai trò để ép GSON giải mã ra đúng Class con tương ứng
                                if ("BIDDER".equalsIgnoreCase(role)) {
                                    user = gson.fromJson(obj, com.auction.shared.model.Bidder.class);
                                } else if ("SELLER".equalsIgnoreCase(role)) {
                                    user = gson.fromJson(obj, com.auction.shared.model.Seller.class);
                                }

                                if (user != null) {
                                    observableList.add(user); // Thêm phần tử đã định hình vào danh sách hiển thị
                                }
                            }
                        }

                        /// Đổ mảng dữ liệu đã bóc tách chuẩn xác vào bảng hiển thị trên màn hình
                        if (userTable != null) {
                            userTable.setItems(observableList);
                        }

                        /// Cập nhật hiển thị số lượng tài khoản động lên nhãn Thống Kê góc phải
                        if (lblTotalAccounts != null) {
                            lblTotalAccounts.setText(observableList.size() + " Tài khoản");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("[CLIENT ERROR] Lỗi phân tách dữ liệu đa hình danh sách thành viên!");
                    }
                    break;

                case "RECEIVE_ALL_USERS_FAIL":
                case "DELETE_USER_FAIL":
                    showNotification(Alert.AlertType.ERROR, "Lỗi hệ thống", msg.getPayload().toString());
                    break;
            }
        });
    }

    private void showNotification(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /// Method này thc hiện khi admin click vào TRANG CHỦ
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

    /// Method này thực hiện khi thao tác click vào Phiên đấu giá
    @FXML
    public void onAuctionSessionClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/admin/admin_qlphiendaugia.fxml")); //Tải file phiendaugia.fxml
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

    /// Method này thực hiện khi admin click vào nút QUẢN LÝ SẢN PHẨM.
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
