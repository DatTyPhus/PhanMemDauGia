package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Auction;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.TilePane;
import java.util.ArrayList;
import java.util.List;

/// class AuctionController này dùng để thực hiện các yêu cầu của người dùng khi thao tác trên màn hình PHIÊN ĐẤU GIÁ.
public class AuctionController implements NetworkClient.MessageListener {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTopBalance;

    /// Link với cái TilePane (bảng chứa Card) và Pagination (thanh phân trang) bên fxml
    @FXML private TilePane cardContainer;
    @FXML private Pagination pagination;

    /// Danh sách chứa toàn bộ dữ liệu đấu giá lấy từ Server
    private List<Auction> allAuctions = new ArrayList<>();

    /// Thiết lập hằng số 4 Card cho mỗi trang
    private final int ITEMS_PER_PAGE = 3;

    /// Hàm khởi tạo này sẽ tự động chạy ngay khi trang Phiên đấu giá được load lên.
    @FXML
    public void initialize() {
        try{
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();

            if (currentUser != null && lblUserName != null) {
                lblUserName.setText(currentUser.getFullName());
                if (lblUserRole != null) lblUserRole.setText(currentUser.getRole());

                String formattedBalance = String.format("%,.0f VNĐ", currentUser.getBalance());
                if (lblTopBalance != null) lblTopBalance.setText("Số dư: " + formattedBalance);
            }

            // Lắng nghe sự kiện người dùng bấm chuyển trang trên thanh Pagination
            if (pagination != null) {
                pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                    updateCardsForPage(newIndex.intValue());
                });
            }

            // Khi vào trang: Gửi lệnh lấy toàn bộ danh sách phiên đấu giá khi User vừa bước vào sảnh
            NetworkClient.getInstance().send(new Message("GET_ALL_AUCTIONS", ""));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ///  Hàm xử lý việc cắt nhỏ danh sách và hiển thị 3 Card mỗi trang
    private void updateCardsForPage(int pageIndex) {
        if (cardContainer == null) return;

        /// Dọn sạch các thẻ cũ trên màn hình RAM để nạp dữ liệu mới.
        cardContainer.getChildren().clear();

        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            Auction auction = allAuctions.get(i);
            try {
                // 1. Tải bản vẽ FXML của tấm thẻ
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/auction/client/view/card.fxml"));
                javafx.scene.layout.VBox cardBox = fxmlLoader.load();

                // 2. Lấy cái CardController đang điều khiển tấm thẻ này ra
                CardController cardController = fxmlLoader.getController();

                // 3. Lấy dữ liệu các cuộc đấu giá được duyệt ở database cho vào từng card.
                cardController.setData(auction);

                // 4. Nhét tấm thẻ vào bảng lưới trên giao diện chính
                cardContainer.getChildren().add(cardBox);
            } catch (Exception e) {
                System.err.println("[CLIENT ERROR] Lỗi render Card đấu giá ID: " + auction.getId());
                e.printStackTrace();
            }
        }
    }

    /// Các hàm trống này để chống lỗi khi bấm vào các nút Filter trên giao diện
    @FXML public void filterAll(ActionEvent event) { }
    @FXML public void filterRunning(ActionEvent event) { }
    @FXML public void filterUpcoming(ActionEvent event) { }
    @FXML public void filterFinished(ActionEvent event) { }

    /// Method này thực hiện khi thao tác click vào Trang chủ
    @FXML
    public void onBackToHomeClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            source.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /// Method này thực hiện khi thao tác click vào nút chuyển sang màn notification.fxml.
    @FXML
    public void onNotificationClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/notification.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            source.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
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
                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

                source.getScene().setRoot(root);
                currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else {     // Chặn bidder khi chuyển trang sang QUẢN LÝ TÀI SẢN
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Từ chối truy cập");
            alert.setHeaderText(null);
            alert.setContentText("Xin lỗi, tính năng TÀI SẢN ĐẤU GIÁ chỉ dành riêng cho Người Bán (Seller)!");
            alert.showAndWait();
        }
    }

    /// Method này thực hiện khi người dùng click vào nút LỊCH SỬ ĐẤU GIÁ.
    @FXML
    public void onHistoryClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/history.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            source.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /// Method này thực hiện khi nguười dùng click vào nút HỒ SƠ CỦA TÔI.
    @FXML
    public void onProfileClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/profile.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            source.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /// Method này thực hiện khi click vào nút CÀI ĐẶT
    @FXML
    public void onSettingClick(javafx.event.ActionEvent event) {
        try {
            NetworkClient.getInstance().removeListener(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/setting.fxml"));
            Parent root = loader.load();

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            source.getScene().setRoot(root);
            currentStage.setTitle("ĐẤU GIÁ TRỰC TUYẾN");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /// ================= PHẦN XỬ LÝ REALTIME =================
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "RECEIVE_ALL_AUCTIONS_SUCCESS":        // Nhận tín hiệu gửi toàn bộ cuộc đấu giá cho Bidder
                    try {
                        String jsonArrayStr = msg.getPayload().toString();
                        com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonArrayStr).getAsJsonArray();
                        com.google.gson.Gson gson = new com.google.gson.Gson();

                        /// Lưu toàn bộ dữ liệu mới nhất vào list AllAuctions
                        allAuctions.clear();
                        for (com.google.gson.JsonElement element : jsonArray) {
                            Auction auction = gson.fromJson(element, Auction.class);
                            if (auction != null) {
                                allAuctions.add(auction);
                            }
                        }

                        // Tính toán ra tổng số lượng trang (Mỗi trang 3 item)
                        int pageCount = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);
                        if (pagination != null) {
                            /// Thiết lập lại số trang cho thanh Pagination
                            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
                            /// Gọi hàm vẽ lại Card cho trang hiện tại
                            updateCardsForPage(pagination.getCurrentPageIndex());
                        } else {
                            updateCardsForPage(0);
                        }

                    } catch (Exception e) {
                        System.err.println("[CLIENT ERROR] Lỗi render danh sách Card đấu giá!");
                        e.printStackTrace();
                    }
                    break;
            }
        });
    }
}