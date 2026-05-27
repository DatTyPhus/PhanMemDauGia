package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Item;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

/// class HomeController này dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình ,và xử lý các yêu cầu từ server.

public class ProductManageController implements NetworkClient.MessageListener {

    // Các biến dùng để link các thông số từ màn hình.
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;

    // Các biến dùng để link với các thông số cột ở bảng.
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colType;
    @FXML private TableColumn<Item, String> colDescription;
    @FXML private TableColumn<Item, String> colSpecialInfo;
    @FXML private TableColumn<Item, BigDecimal> colPrice;
    @FXML private TableColumn<Item, Integer> colDuration;
    @FXML private TableColumn<Item, String> colStatus;
    @FXML private TableColumn<Item, Void> colAction;
    @FXML private javafx.scene.control.Pagination pagination;
    @FXML private javafx.scene.control.TableView<Item> productTable;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();  /// Lấy thông tin người dùng hiện tại đang thao tác lưu vào kho để khi chuyển màn không bị mất thông tin.

            //  Nếu có user và đã gắn fx:id thì mới đắp dữ liệu
            if (currentUser != null) {                                         /// Lấy dữ liệu người dùng hiện tại(ở kho đã lưu khi chuyển màn) để in lên thanh thông tin ở góc phải
                // Biến nào đã được gắn fx:id bên FXML thì mới được phép setText
                if (lblUserName != null) {
                    lblUserName.setText(currentUser.getFullName());
                }
                if (lblUserRole != null) {
                    lblUserRole.setText(currentUser.getRole());
                }
                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());      /// Hiển thị số dư.
                lblTopBalance.setText("Số dư: " + formattedBalance);
            }

            // Định dạng các cột lưu 1 thuộc tính của sản phẩm.
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colType.setCellValueFactory(new PropertyValueFactory<>("itemType"));
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
            colSpecialInfo.setCellValueFactory(new PropertyValueFactory<>("specialInfo"));
            colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
            colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            // 1. Định dạng Giá tiền
            colPrice.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VNĐ", item));
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;"); // Chữ màu xanh lá
                    }
                }
            });

            // 2.Tạo Badge cho Trạng thái
            colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label badge = new Label(item);
                        badge.setPadding(new javafx.geometry.Insets(3, 10, 3, 10));
                        badge.setStyle("-fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11;");

                        if ("PENDING".equalsIgnoreCase(item)) {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;");
                        } else if ("APPROVED".equalsIgnoreCase(item)) {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                        } else {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                        }
                        setGraphic(badge);
                        setAlignment(javafx.geometry.Pos.CENTER);
                    }
                }
            });

            // 3. Thêm nút Thao tác để chỉnh sửa thông tin và xóa.
            colAction.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
                private final javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("📝");
                private final javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑");
                private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10, btnEdit, btnDelete);

                {
                    container.setAlignment(javafx.geometry.Pos.CENTER);
                    btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                    btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                    btnDelete.setOnAction(e -> {
                        Item item = getTableView().getItems().get(getIndex());

                        /// Bật hộp thoại hỏi xác nhận trước khi xóa
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Xác nhận xóa");
                        alert.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm: " + item.getName() + "?");
                        alert.setContentText("Hành động này không thể hoàn tác!");

                        // Nhận tín hiệu khi người dùng bấm nút trong hộp thoại
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {

                                /// Nếu bấm OK, đóng gói ID sản phẩm và gửi lệnh DELETE_ITEM xuống Server
                                Message msg = new Message("DELETE_ITEM", String.valueOf(item.getId()));
                                try {
                                    NetworkClient.getInstance().send(msg);
                                    System.out.println("[CLIENT] Đã gửi yêu cầu XÓA sản phẩm ID: " + item.getId());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : container);
                }
            });

            // Nâng cấp phân trang PAGINATION
            if (pagination != null) {
                // Reset style về dạng Bullet mặc định
                pagination.getStyleClass().add(javafx.scene.control.Pagination.STYLE_CLASS_BULLET);

                // Dùng code để ép CSS trực tiếp cho Pagination
                pagination.setStyle(
                        "-fx-page-information-visible: false; " +  // Ẩn dòng chữ "1/10" thừa thãi
                                "-fx-background-color: transparent;"       // Làm trong suốt nền
                );
            }
            onRefreshClick(null);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    /// Method này thực hiện khi người dùng click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/history.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();  //Thay đổi màn home thành màn phiendaugia
            javafx.scene.Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file history.fxml! Hãy kiểm tra lại đường dẫn.");
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

    // Hàm này dùng để hứng sự kiện khi bấm nút "+ Thêm sản phẩm"
    @FXML
    public void handleAdd(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/add_edit_product.fxml"));  // Tải file màn hình
            Parent root = loader.load();

            // 1. Lấy cái nút vừa được bấm
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();

            // 2. QUAN TRỌNG: Lấy cái Cửa sổ (Stage) TRƯỚC KHI thay đổi màn hình
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            // 3. Bây giờ mới được phép thay ruột (giao diện mới)
            source.getScene().setRoot(root);

            // 4. Đặt lại tiêu đề
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file setting.fxml!");
        }
    }

    @FXML
    public void onRefreshClick(ActionEvent event) {
        User currentUser = UserSession.getInstance().getLoginUser();
        if (currentUser != null) {
            // Đóng gói ID người bán thành JSON gửi lên Server
            String payload = "{\"id\":" + currentUser.getId() + "}";
            Message msg = new Message("MY_PRODUCTS", payload);
            try {
                NetworkClient.getInstance().send(msg);
                System.out.println("[CLIENT] Đã gửi yêu cầu LÀM MỚI danh sách sản phẩm.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= PHẦN XỬ LÝ REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        //  Phải đưa lệnh đổi giao diện vào Platform.runLater vì tin nhắn đến từ luồng mạng (Thread khác), nếu đổi trực tiếp sẽ làm sập JavaFX
        javafx.application.Platform.runLater(() -> {

            switch (msg.getAction()) {

                // Xử lý khi nhận sản phẩm thành công.
                case "MY_PRODUCTS_SUCCESS":
                    System.out.println("[CLIENT] Đã nhận được danh sách tài sản từ Server!");

                    // 1. Dùng Gson để chuyển đổi Object thành chuỗi JSON chuẩn
                    Gson gson = new Gson();
                    String jsonList = gson.toJson(msg.getPayload());

                    // 2. Bây giờ JsonParser sẽ đọc  vì chuỗi đã chuẩn JSON
                    com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonList).getAsJsonArray();
                    javafx.collections.ObservableList<Item> items = javafx.collections.FXCollections.observableArrayList();

                    for (com.google.gson.JsonElement element : jsonArray) {
                        com.google.gson.JsonObject obj = element.getAsJsonObject();

                        //  Đảm bảo đối tượng có thuộc tính itemType
                        if (obj.has("itemType") && !obj.get("itemType").isJsonNull()) {
                            String type = obj.get("itemType").getAsString(); // Lấy loại để khởi tạo đúng class con

                            Item item = null;
                            if ("ART".equals(type)) item = gson.fromJson(obj, com.auction.shared.model.Art.class);
                            else if ("ELECTRONIC".equals(type))
                                item = gson.fromJson(obj, com.auction.shared.model.Electronics.class);
                            else if ("VEHICLE".equals(type))
                                item = gson.fromJson(obj, com.auction.shared.model.Vehicle.class);

                            if (item != null) items.add(item);
                        }
                    }
                    // Đổ dữ liệu vào bảng
                    if (productTable != null) {
                        productTable.setItems(items);
                    }
                    break;

                // Khi xóa sản phẩm thành công
                case "DELETE_ITEM_SUCCESS":
                    /// In thông báo xóa thành công
                    javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thành công");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText(msg.getPayload().toString());
                    successAlert.showAndWait();

                    // Làm mới bẳng lại
                    onRefreshClick(null);
                    break;

                // Khi xóa ảnh thất bại.
                case "DELETE_ITEM_FAIL":
                    /// In thông báo lỗi nếu xóa thất bại
                    javafx.scene.control.Alert failAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    failAlert.setTitle("Thất bại");
                    failAlert.setHeaderText(null);
                    failAlert.setContentText(msg.getPayload().toString());
                    failAlert.showAndWait();
                    break;

                // (Các thông báo như LOGIN_SUCCESS... nó sẽ rơi vào default và bị bỏ qua, không làm loạn màn hình này)
                default:
                    break;
            }
        });
    }
}