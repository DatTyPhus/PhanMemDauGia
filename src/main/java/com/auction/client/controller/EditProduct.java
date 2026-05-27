package com.auction.client.controller;

import java.io.File;
import java.util.Base64;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Item;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import com.google.gson.Gson;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import static javafx.scene.control.Alert.AlertType.ERROR;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/// class HomeController này dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình ,và xử lý các yêu cầu từ server.

public class EditProduct implements NetworkClient.MessageListener {


    @FXML private javafx.scene.control.TextField txtProductName;
    @FXML private javafx.scene.control.ComboBox<String> cbCategory;
    @FXML private javafx.scene.control.TextArea txtDescription;
    @FXML private javafx.scene.control.TextField txtStartPrice;
    @FXML private javafx.scene.control.ComboBox<String> cbDuration;
    @FXML private javafx.scene.control.TextField txtSpecialInfo;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Thông báo được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);

            // 1 . Phân loại sản phẩm.
            if (cbCategory != null) {
                cbCategory.getItems().addAll("ART", "ELECTRONIC", "VEHICLE");
                cbCategory.setValue("ART"); // Lựa chọn mặc định ban đầu
            }

            // 2. Chọn thời lượng đấu giá.
            if (cbDuration != null) {
                cbDuration.getItems().addAll("5p", "15p", "30p", "1h", "2h");
                cbDuration.setValue("5p"); // Lựa chọn mặc định ban đầu
            }
            //  BẢO VỆ MỨC 1 (CHẶN GÕ CHỮ TRÊN Ô NHẬP GIÁ TIỀN)
            if (txtStartPrice != null) {
                txtStartPrice.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                    // 1. Chỉ cho phép nhập số từ 0-9 (Chặn hoàn toàn chữ cái, ký tự đặc biệt, dấu cách)
                    if (!change.getText().matches("[0-9]*")) return null;
                    // 2. Chống tràn số (Anti-Overflow): Giới hạn độ dài ô nhập tối đa 11 chữ số (Dưới 100 Tỷ VNĐ)
                    if (change.getControlNewText().length() > 11) return null;
                    return change;
                }));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onSaveClick() {
        try {
            // 1. Kiểm tra thông tin người bán đang đăng nhập
            User currentUser = UserSession.getInstance().getLoginUser();
            if (currentUser == null) {
                showAlert(ERROR, "Lỗi", "Không tìm thấy thông tin phiên đăng nhập. Vui lòng thử lại!");
                return;
            }

            // 2. Thu thập dữ liệu từ các ô nhập liệu trên giao diện
            String name = (txtProductName != null) ? txtProductName.getText().trim() : "";
            String selectedCategory = (cbCategory != null) ? cbCategory.getValue() : null; // Lấy "ART", "ELECTRONIC", hoặc "VEHICLE"
            String description = (txtDescription != null) ? txtDescription.getText().trim() : "";
            String startPriceStr = (txtStartPrice != null) ? txtStartPrice.getText().trim() : "";
            String duration = (cbDuration != null) ? cbDuration.getValue() : null; // Lấy "5p", "15p", "30p", "1h", "2h"
            String specialInfo = (txtSpecialInfo != null) ? txtSpecialInfo.getText().trim() : "";

            // 3. Kiểm tra dữ liệu trống (Validation)
            if (name.isEmpty() || selectedCategory == null || startPriceStr.isEmpty() || duration == null) {
                showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tất cả các trường dữ liệu!");
                return;
            }

            // 4. Kiểm tra định dạng số tiền khởi điểm
            java.math.BigDecimal startPrice;
            try {
                startPrice = new java.math.BigDecimal(startPriceStr);
                if (startPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Dữ liệu sai", "Giá khởi điểm phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(ERROR, "Sai định dạng", "Giá khởi điểm bắt buộc phải là một dãy số!");
                return;
            }

            // 5. Quy đổi chuỗi thời lượng hiển thị (5p, 1h...) sang số phút nguyên (int) để lưu DB
            int durationMinutes = 5; // Mặc định nếu có lỗi xảy ra
            switch (duration) {
                case "5p":   durationMinutes = 5; break;
                case "15p":  durationMinutes = 15; break;
                case "30p":  durationMinutes = 30; break;
                case "1h":   durationMinutes = 60; break;
                case "2h":   durationMinutes = 120; break;
            }

            // 6. KHỞI TẠO ĐỐI TƯỢNG: Truyền đúng loại "ART", "ELECTRONIC", "VEHICLE" vào hàm createFromType
            Item newItem = Item.createFromType(selectedCategory);

            // 7. ĐỔ DỮ LIỆU: Gọi chuẩn xác các hàm set tương thích hoàn toàn với lớp Item.java
            newItem.setSellerId(currentUser.getId());
            newItem.setName(name);
            newItem.setItemType(selectedCategory); // Khớp với trường itemType trong Item.java
            newItem.setDescription(description);
            newItem.setStartingPrice(startPrice);
            newItem.setSpecialInfo(specialInfo);

            // Mã hóa thành chuỗi Base64
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    // Đọc file ảnh dưới dạng mảng byte
                    File file = new File(selectedImagePath);
                    byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());

                    // Mã hóa mảng byte thành chuỗi Base64 khổng lồ
                    String base64String = Base64.getEncoder().encodeToString(fileContent);

                    // Gắn chuỗi Base64 vào Object để gửi lên Server
                    newItem.setImageUrl(base64String);
                } catch (Exception e) {
                    System.err.println("Lỗi khi mã hóa ảnh: " + e.getMessage());
                    newItem.setImageUrl(""); // Lỗi thì để trống ảnh
                }
            } else {
                newItem.setImageUrl(""); // Không chọn ảnh thì để trống
            }
            newItem.setDurationMinutes(durationMinutes); // Gán số phút quy đổi
            newItem.setStatus("PENDING"); // Gắn cờ chờ Admin duyệt theo đúng logic của nhóm

            // 8. CHUYỂN ĐỔI SANG JSON: Sử dụng thư viện Gson
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(newItem);

            // 9. BẮN GÓI TIN: Gửi mã hành động "ADD_ITEM" đồng bộ theo thiết kế xử lý của Server
            Message msg = new Message("ADD_ITEM", jsonPayload);
            NetworkClient.getInstance().send(msg);

            // In log kiểm tra tiến trình dưới Console
            System.out.println("\n[CLIENT] Đã tạo đối tượng và bắn lệnh ADD_ITEM lên Server thành công!");
            System.out.println("Nội dung chuỗi JSON gửi đi:\n" + jsonPayload);


        } catch (Exception e) {
            System.err.println("[CLIENT ERROR] Lỗi khi thực hiện lưu sản phẩm: " + e.getMessage());
            e.printStackTrace();
            showAlert(ERROR, "Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    /// Hàm phụ trợ hiển thị nhanh hộp thoại thông báo Pop-up trên giao diện JavaFX
    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /// Hàm dọn sạch nội dung form sau khi hoàn tất gửi dữ liệu lên mạng
    private void clearForm() {
        if (txtProductName != null) txtProductName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtStartPrice != null) txtStartPrice.clear();
        if (cbCategory != null) cbCategory.setValue("ART");
        if (cbDuration != null) cbDuration.setValue("5p");
        if (txtSpecialInfo != null) txtSpecialInfo.clear();
    }

    // 1. Khai báo thêm khung ảnh và một biến chuỗi để lưu đường dẫn ảnh
    @FXML private ImageView imgProduct;
    private String selectedImagePath = ""; // Biến toàn cục lưu đường dẫn ảnh để lát nữa gửi đi


    // 2. Viết hàm xử lý khi bấm nút "Chọn ảnh"
    @FXML
    public void onChooseImageClick(javafx.event.ActionEvent event) {
        try {
            // Khởi tạo cửa sổ chọn file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn hình ảnh sản phẩm");

            // Chỉ cho phép chọn các file định dạng hình ảnh
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            // Mở cửa sổ và lấy file người dùng chọn
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            java.io.File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Lấy đường dẫn tuyệt đối của file
                selectedImagePath = selectedFile.getAbsolutePath();

                // Hiển thị ảnh xem trước lên giao diện
                Image image = new Image(selectedFile.toURI().toString());
                imgProduct.setImage(image);


                System.out.println("Đã chọn ảnh tại đường dẫn: " + selectedImagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(ERROR, "Lỗi", "Không thể tải ảnh: " + e.getMessage());
        }
    }

    /// XỬ LÝ SỰ KIỆN KHI NGƯỜI DÙNG BẤM NÚT HỦY BỎ .Quay trở lại màn hình Quản lý tài sản (product_management.fxml)

    @FXML
    public void onCancelClick(javafx.event.ActionEvent event) {
        try {
            // 1. Rút ống nghe của màn hình hiện tại ra khỏi NetworkClient
            NetworkClient.getInstance().removeListener(this);

            // 2. Định vị và tải tệp giao diện Quản lý tài sản của bạn
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/product_management.fxml"));
            Parent root = loader.load();

            // 3. Xác định đối tượng Node kích hoạt sự kiện bấm nút
            Node source = (Node) event.getSource();

            // 4. Lấy cửa sổ (Stage) TRƯỚC KHI thiết lập Root mới
            Stage currentStage = (Stage) source.getScene().getWindow();

            // 5. Tiến hành thay đổi giao diện trung tâm thành màn hình danh sách tài sản
            source.getScene().setRoot(root);

            // 6. Đảm bảo tiêu đề đồng bộ trên thanh tác vụ của hệ thống
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

            System.out.println("[CLIENT] Người dùng đã hủy bỏ tác vụ thêm tài sản. Đã quay về màn Quản lý.");

        } catch (java.io.IOException e) {
            System.err.println("[FXML ERROR] Không tìm thấy file giao diện: product_management.fxml");
            e.printStackTrace();
            showAlert(ERROR, "Lỗi hệ thống", "Không thể quay lại màn hình quản lý: " + e.getMessage());
        }
    }


    /// Method này thực hiện khi thao tác click vào nút chuyển sang màn notification.fxml.
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/notification.fxml"));

            Parent root = loader.load();                ///Thay đổi màn home thành màn notification.
            Node source = (Node) event.getSource();
            Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            Stage currentStage = (Stage) currentScene.getWindow();
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

            Node source = (Node) event.getSource();  //Thay đổi màn home thành màn phiendaugia
            Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            Stage currentStage = (Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file phiendaugia.fxml!");
        }
    }

    /// Method này thực hiện khi người dùng click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/history.fxml")); //Tải file phiendaugia.fxml
            Parent root = loader.load();

            Node source = (Node) event.getSource();  //Thay đổi màn home thành màn phiendaugia
            Scene currentScene = source.getScene();
            currentScene.setRoot(root);

            Stage currentStage = (Stage) currentScene.getWindow(); // Đặt tiêu đề cho window
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file history.fxml! Hãy kiểm tra lại đường dẫn.");
        }
    }

    /// Method này thực hiện khi thao tác click vào Trang chủ
    @FXML
    public void onBackToHomeClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            Node source = (Node) event.getSource();  //Thay đổi màn hình phiendaugia thành màn home.
            source.getScene().setRoot(root);

            //Đặt lại tiêu đề cho window.
            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");

        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không thể tải trang chủ!");
        }
    }

    @FXML
    public void onProductManagementClick(javafx.event.ActionEvent event) {
        //  Lấy thông tin người dùng hiện tại từ Session
        com.auction.shared.model.User currentUser = com.auction.client.session.UserSession.getInstance().getLoginUser();

        // KIỂM TRA ROLE ĐỂ VÀO MÀN HÌNH
        if (currentUser != null && "Seller".equalsIgnoreCase(currentUser.getRole())) {         // Nếu đủ điều kiện thfi chuyển màn hiình sang màn quản lý tài sản.
            try {
                NetworkClient.getInstance().removeListener(this);    // Xoá màn hình khỏi danh sách nghe tín hiệu từ server

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/product_management.fxml"));
                Parent root = loader.load();

                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                source.getScene().setRoot(root);

                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();
                currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi: Không tìm thấy file product_management.fxml!");
            }
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);    //Nếu không đủ điều kiện , hiện lên thông báo để chuyển hướng.
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

    // ================= PHẦN XỬ LÝ REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        // Phải đưa lệnh đổi giao diện vào Platform.runLater vì tin nhắn đến từ luồng mạng (Thread khác), nếu đổi trực tiếp sẽ làm sập JavaFX
        javafx.application.Platform.runLater(() -> {

            switch (msg.getAction()) {
                case "ADD_ITEM_SUCCESS":
                    // Hiện Popup báo thành công và xóa trắng Form
                    showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Thành công", msg.getPayload().toString());
                    clearForm();
                    break;

                case "ADD_ITEM_FAIL":
                    // Hiện Popup báo lỗi nếu Server trục trặc
                    showAlert(ERROR, "Lỗi", msg.getPayload().toString());
                    break;

                // Các tín hiệu khác tạm thời bỏ qua
                default:
                    break;
            }
        });
    }

    /// Hàm  giải mã chuỗi Base64 khổng lồ từ DB thành hình ảnh JavaFX .Hàm này sẽ được gọi khi bạn tải thông tin sản phẩm (có chứa chuỗi URL Base64) để Sửa.
    public void displayImageFromBase64(String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // 1. Dịch ngược chuỗi Base64 thành mảng byte
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

                // 2. Tạo một luồng đọc dữ liệu từ mảng byte đó
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes);

                // 3. Tạo thành đối tượng Image của JavaFX
                javafx.scene.image.Image image = new javafx.scene.image.Image(bis);

                // 4. Hiển thị lên khung ImageView trên màn hình EditProduct
                if (imgProduct != null) {
                    imgProduct.setImage(image);
                }

            } catch (Exception e) {
                System.err.println("Lỗi giải mã hình ảnh từ Database: " + e.getMessage());
            }
        }
    }
}
