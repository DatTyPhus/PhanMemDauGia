package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.session.UserSession;
import com.auction.shared.model.Auction;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/// class BiddingController: Đã hoàn thiện toàn bộ luồng Đặt giá (Bidding) và Lịch sử Real-time
public class BiddingController implements NetworkClient.MessageListener {

    ///  Đặt tên biến cho nội dung hiển thị ở màn PHÒNG ĐẤU GIÁ
    @FXML private Label lblUserName, lblRole, lblTopBalance, lblProductTitle, lblCurrentPrice, lblHighestBidder, timeLabel;
    @FXML private Label lblDescription, lblItemType, lblSpecialInfo;
    @FXML private TextField txtBidAmount, txtAutoMaxPrice, txtAutoStep; // Bổ sung 2 biến Auto
    @FXML private ImageView productImage;
    @FXML private Button bidButton;

    /// Bảng lịch sử dùng mảng String[] để chứa 3 cột: Thời gian, Tên, Giá
    @FXML private TableView<String[]> historyTable;
    private ObservableList<String[]> historyDataList = FXCollections.observableArrayList();

    /// Khai báo Biểu đồ và Dải dữ liệu (Series)
    @FXML private javafx.scene.chart.LineChart<String, Number> priceChart;
    private javafx.scene.chart.XYChart.Series<String, Number> priceSeries = new javafx.scene.chart.XYChart.Series<>();

    /// Khai báo đối tương CUỘC ĐẤU GIÁ và ĐỒNG HỒ ĐẾM GIỜ.
    private Auction targetAuction;
    private Timeline countdownTimeline;

    /// Cập nhật lại đối tượng ĐẤU GIÁ chứa các thông về phòng đấu giá.
    public void setAuctionData(Auction auction) {
        this.targetAuction = auction;
        refreshUI();
        startCountdown();

        try {
            /// Lấy chi tiết hình ảnh sản phẩm
            NetworkClient.getInstance().send(new Message("GET_ITEM_DETAILS", String.valueOf(auction.getItemId())));

            /// Lấy lịch sử đặt giá cũ.
            NetworkClient.getInstance().send(new Message("GET_BID_HISTORY", String.valueOf(auction.getId())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        try {
            NetworkClient.getInstance().addListener(this);
            User currentUser = UserSession.getInstance().getLoginUser();
            if (currentUser != null) {
                if (lblUserName != null) lblUserName.setText(currentUser.getFullName());   //Hiển thị thông tin và số dư
                if (lblRole != null) lblRole.setText(currentUser.getRole().toUpperCase());
                if (lblTopBalance != null) lblTopBalance.setText(String.format("Số dư: %,.0f VNĐ", currentUser.getBalance()));
            }

            /// Khởi tạo móc nối dữ liệu cho 3 cột trong Bảng Lịch sử
            if (historyTable != null && historyTable.getColumns().size() >= 3) {
                TableColumn<String[], String> colTime = (TableColumn<String[], String>) historyTable.getColumns().get(0);
                TableColumn<String[], String> colName = (TableColumn<String[], String>) historyTable.getColumns().get(1);
                TableColumn<String[], String> colPrice = (TableColumn<String[], String>) historyTable.getColumns().get(2);

                colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
                colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
                colPrice.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%,.0f đ", new BigDecimal(cellData.getValue()[2]))));

                historyTable.setItems(historyDataList);
            }

            /// Khởi tạo Biểu đồ theo thời gian thực
            if (priceChart != null) {
                priceChart.getData().add(priceSeries);
                priceChart.setAnimated(false); /// Tắt animation để biểu đồ không bị giật lag khi cập nhật liên tục
            }
        } catch (Exception e) {
            System.err.println("[CLIENT WARNING] Lỗi khởi tạo phòng đấu giá: " + e.getMessage());
        }
    }

    /// Đếm ngược thời gian của phiên đấu giá theo thời gian thực - Cập nhật lại thời gian khi gia hạn đấu giá - Khóa phòng khi đã hết thời gian
    /// Đếm ngược thời gian của phiên đấu giá theo thời gian thực (ĐÃ CHUYỂN QUYỀN CHO SERVER)
    private void startCountdown() {
        // Đã xóa logic Timeline nội bộ.
        // Từ nay Client chỉ ngồi im chờ Server gửi số giây về qua Socket!
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    /// Hàm dùng để cập nhật các thông số khi đặt giá
    private void refreshUI() {
        if (targetAuction != null) {
            lblProductTitle.setText(targetAuction.getItemName().toUpperCase());
            lblCurrentPrice.setText(String.format("%,.0f VNĐ", targetAuction.getCurrentPrice()));
            lblHighestBidder.setText("👑 Người dẫn đầu: " + (targetAuction.getHighestBidderName() != null ? targetAuction.getHighestBidderName() : "Chưa có"));
        }
    }

    // Link với các nút đặt giá nhanh.
    @FXML public void quickBid1M() { performQuickBid(new BigDecimal("1000000")); }
    @FXML public void quickBid5M() { performQuickBid(new BigDecimal("5000000")); }
    @FXML public void quickBid10M() { performQuickBid(new BigDecimal("10000000")); }

    ///  Hàm dùng để đặt giá nhanh
    private void performQuickBid(BigDecimal additionalAmount) {
        BigDecimal newPrice = targetAuction.getCurrentPrice().add(additionalAmount);
        txtBidAmount.setText(newPrice.toPlainString());
        onBidClick();
    }

    /// Hàm dùng để gửi lệnh đặt giá xuống server xử lý.
    @FXML
    public void onBidClick() {

        // Khi cuộc đấu giá kết thúc không được đặt giá tiếp.
        if (bidButton.isDisabled()) {
            showAlert("Phiên đấu giá đã kết thúc, bạn không thể đặt giá được nữa!");
            return;
        }

        // Gửi đi lệnh đặt bid.
        try {
            BigDecimal amount = new BigDecimal(txtBidAmount.getText());
            User currentUser = UserSession.getInstance().getLoginUser();

            /// Gửi chuỗi ghép: "AuctionID,Amount,Username"
            String payload = targetAuction.getId() + "," + amount.toPlainString() + "," + currentUser.getUsername();
            NetworkClient.getInstance().send(new Message("BID", payload));

            txtBidAmount.clear(); // Xóa trắng ô nhập sau khi gửi
        } catch (Exception e) {
            showAlert("Số tiền không hợp lệ! Vui lòng chỉ nhập các con số.");
        }
    }

    /// Hàm thực hiện khi click về lại PHIÊN ĐẤU GIÁ
    @FXML
    public void onBackToHallClick(javafx.event.ActionEvent event) {
        if (countdownTimeline != null) countdownTimeline.stop();
        try {
            NetworkClient.getInstance().removeListener(this);
            navigate(event, "/com/auction/client/view/phiendaugia.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// Hàm này gửi lệnh cấu hình cho Auto-bid xuống server.
    @FXML
    public void onAutoBidClick() {

        // Phiên đấu giá kết thúc không thể cấu hình Auto-bid được
        if (bidButton.isDisabled()) {
            showAlert("Phiên đấu giá đã kết thúc, không thể cài đặt Auto-Bid!");
            return;
        }

        // Các bước cấu hình cho tín hiệu Auto-bid được gửi đi
        try {
            BigDecimal maxAmount = new BigDecimal(txtAutoMaxPrice.getText().trim());
            BigDecimal stepAmount = new BigDecimal(txtAutoStep.getText().trim());
            User currentUser = UserSession.getInstance().getLoginUser();

            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", targetAuction.getId());
            payload.addProperty("bidderName", currentUser.getUsername());
            payload.addProperty("autoBidAmount", maxAmount);
            payload.addProperty("autoBidStep", stepAmount);

            NetworkClient.getInstance().send(new Message("AUTO_BID", payload.toString()));

            // Cấu hình xong xóa thông tin trong ô
            txtAutoMaxPrice.clear();
            txtAutoStep.clear();
        } catch (Exception e) {
            showAlert("Vui lòng nhập định dạng số hợp lệ cho Mức giá tối đa và Bước nhảy!");
        }
    }

    /// Nhận tín hiệu từ server
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                /// Xử lý load ảnh khi server gửi lên
                case "RECEIVE_ITEM_DETAILS":
                    try {
                        JsonObject itemJson = JsonParser.parseString(msg.getPayload().toString()).getAsJsonObject();
                        if (lblItemType != null && itemJson.has("itemType") && !itemJson.get("itemType").isJsonNull()) lblItemType.setText(itemJson.get("itemType").getAsString());
                        if (lblSpecialInfo != null && itemJson.has("special_info") && !itemJson.get("special_info").isJsonNull()) lblSpecialInfo.setText(itemJson.get("special_info").getAsString());
                        if (lblDescription != null && itemJson.has("description") && !itemJson.get("description").isJsonNull()) lblDescription.setText(itemJson.get("description").getAsString());

                        if (productImage != null && itemJson.has("imageUrl") && !itemJson.get("imageUrl").isJsonNull()) {
                            String imgUrl = itemJson.get("imageUrl").getAsString().trim();
                            if (!imgUrl.isEmpty()) {
                                try {
                                    Image img = null;
                                    if (imgUrl.length() > 200 || imgUrl.startsWith("data:image")) {
                                        String base64Data = imgUrl.contains(",") ? imgUrl.substring(imgUrl.indexOf(",") + 1) : imgUrl;
                                        img = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64Data)));
                                    } else if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://") || imgUrl.startsWith("file:")) {
                                        img = new Image(imgUrl, true);
                                    } else {
                                        File localFile = new File(imgUrl);
                                        if (localFile.exists()) img = new Image(localFile.toURI().toString(), true);
                                        else {
                                            java.net.URL resource = getClass().getResource(imgUrl.startsWith("/") ? imgUrl : "/" + imgUrl);
                                            if (resource != null) img = new Image(resource.toExternalForm(), true);
                                        }
                                    }
                                    if (img != null) productImage.setImage(img);
                                } catch (Exception ignored) {}
                            }
                        }
                    } catch (Exception e) {}
                    break;

                /// Nhận lịch sử cũ khi vừa vào phòng đấu giá
                case "RECEIVE_BID_HISTORY":
                    try {
                        JsonArray historyArray = JsonParser.parseString(msg.getPayload().toString()).getAsJsonArray();
                        historyDataList.clear();

                        for (JsonElement element : historyArray) {
                            JsonArray row = element.getAsJsonArray();
                            historyDataList.add(new String[]{row.get(0).getAsString(), row.get(1).getAsString(), row.get(2).getAsString()});
                        }

                        if (!historyDataList.isEmpty()) {
                            String[] latestBid = historyDataList.get(0);
                            String latestName = latestBid[1];
                            BigDecimal latestPrice = new BigDecimal(latestBid[2]);

                            targetAuction.setCurrentPrice(latestPrice);
                            targetAuction.setHighestBidderName(latestName);

                            lblCurrentPrice.setText(String.format("%,.0f VNĐ", latestPrice));
                            lblHighestBidder.setText("👑 Người dẫn đầu: " + latestName);

                            javafx.scene.chart.XYChart.Series<String, Number> initialSeries = new javafx.scene.chart.XYChart.Series<>();

                            for (int i = historyDataList.size() - 1; i >= 0; i--) {
                                String[] r = historyDataList.get(i);
                                String tRaw = r[0];
                                BigDecimal p = new BigDecimal(r[2]);

                                String shortTime = tRaw.length() >= 19 ? tRaw.substring(11, 19) : tRaw;
                                double priceInM = p.divide(new BigDecimal("1000000"), 2, java.math.RoundingMode.HALF_UP).doubleValue();

                                initialSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(shortTime, priceInM));
                            }

                            /// Xóa sạch lõi cũ và gắn nguyên cái khung mới vào
                            priceChart.getData().clear();
                            priceChart.getData().add(initialSeries);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    break;

                /// Nhận dữ liệu đặt giá thành công theo thời gian thực và cập nhật lại thông tin trên màn hình.
                case "BID_SUCCESS":
                    try {
                        JsonObject res = JsonParser.parseString(msg.getPayload().toString()).getAsJsonObject();
                        int msgAuctionId = res.get("auctionId").getAsInt();

                        if (targetAuction != null && targetAuction.getId() == msgAuctionId) {
                            BigDecimal newPrice = res.get("newPrice").getAsBigDecimal();
                            String highestBidder = res.get("highestBidder").getAsString();
                            String bidTime = res.get("bidTime").getAsString();

                            targetAuction.setCurrentPrice(newPrice);
                            targetAuction.setHighestBidderName(highestBidder);

                            lblCurrentPrice.setText(String.format("%,.0f VNĐ", newPrice));
                            lblHighestBidder.setText("👑 Người dẫn đầu: " + highestBidder);

                            historyDataList.add(0, new String[]{bidTime.substring(11), highestBidder, newPrice.toPlainString()});

                            //Mỗi lần đấu giá sẽ là một luồng.
                            XYChart.Series<String, Number> freshSeries = new XYChart.Series<>();

                            for (int i = historyDataList.size() - 1; i >= 0; i--) {
                                String[] r = historyDataList.get(i);
                                String tRaw = r[0];
                                BigDecimal p = new BigDecimal(r[2]);

                                String shortTime = tRaw.length() >= 19 ? tRaw.substring(11, 19) : tRaw;
                                double priceInM = p.divide(new BigDecimal("1000000"), 2, java.math.RoundingMode.HALF_UP).doubleValue();

                                freshSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(shortTime, priceInM));
                            }

                            /// Ép Chart(biểu đồ) xóa đường đường cũ và nạp đường mới, tạo cảm giác mượt mà Real-time
                            priceChart.getData().clear();
                            priceChart.getData().add(freshSeries);

                            if (res.has("newEndTime")) {
                                targetAuction.setEndTime(res.get("newEndTime").getAsString());
                            }

                            /// =========================================================================
                            /// [NEW LOGIC]: HIỆN THÔNG BÁO NẾU NGƯỜI ĐẶT KHÁC VỚI TÊN CỦA MÌNH
                            /// =========================================================================
                            User currentUser = UserSession.getInstance().getLoginUser();
                            if (currentUser != null && !highestBidder.equals(currentUser.getUsername())) {
                                String formattedPrice = String.format("%,.0f VNĐ", newPrice);
                                showBidNotification("🔥 " + highestBidder + " vừa đặt " + formattedPrice + "!");
                            }
                        }

                        /// Đồng bộ số dư khi đấu giá.
                        User currentUser = UserSession.getInstance().getLoginUser();
                        if (currentUser != null) {
                            NetworkClient.getInstance().send(new Message("GET_MY_BALANCE", currentUser.getUsername() + "," + currentUser.getRole()));
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    break;


                /// ĐỒNG BỘ GIỜ TỪ SERVER ĐỂ ĐẾM NGƯỢC
                case "SERVER_TIME":
                    try {
                        if (targetAuction != null && targetAuction.getEndTime() != null) {
                            String serverTimeStr = msg.getPayload().toString();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                            // Lấy thời gian Server và thời gian kết thúc của phòng này để so sánh
                            LocalDateTime serverNow = LocalDateTime.parse(serverTimeStr, dtf);
                            LocalDateTime endTime = LocalDateTime.parse(targetAuction.getEndTime(), dtf);

                            // Tính ra số giây còn lại (Dùng thời gian server chuẩn, bất chấp máy Client chạy sai giờ)
                            long remainingSecs = java.time.temporal.ChronoUnit.SECONDS.between(serverNow, endTime);

                            Platform.runLater(() -> {
                                if (remainingSecs > 0) {
                                    long h = remainingSecs / 3600;
                                    long m = (remainingSecs % 3600) / 60;
                                    long s = remainingSecs % 60;

                                    timeLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
                                    timeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 50;");

                                } else {
                                    timeLabel.setText("00:00:00");
                                    // Và thêm vào cả lúc hết giờ!
                                    timeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 60;");
                                    bidButton.setDisable(true);
                                }
                            });
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    break;

                /// Bắt tín hiệu cập nhật số dư
                case "UPDATE_BALANCE_UI":
                    try {
                        BigDecimal freshBalance = new BigDecimal(msg.getPayload().toString());
                        User u = UserSession.getInstance().getLoginUser();
                        if (u != null) {
                            u.setBalance(freshBalance);
                            if (lblTopBalance != null) {
                                lblTopBalance.setText(String.format("Số dư: %,.0f VNĐ", freshBalance));
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    break;

                /// Bắt tín hiệu Server báo đóng phiên và thực hiện gửi thông báo đến tất cả người đấu giá.
                case "AUCTION_ENDED":
                    try {
                        int endedId = Integer.parseInt(msg.getPayload().toString());
                        if (targetAuction != null && targetAuction.getId() == endedId) {
                            timeLabel.setText("⏱️ Trạng thái: ĐÃ KẾT THÚC");
                            timeLabel.setStyle("-fx-text-fill: #ef4444;");
                            txtBidAmount.setDisable(true);
                            bidButton.setDisable(true);
                            if (countdownTimeline != null) countdownTimeline.stop();

                            /// Thông báo người chiến thắng cho TOÀN BỘ CĂN PHÒNG
                            String winner = targetAuction.getHighestBidderName();
                            User u = UserSession.getInstance().getLoginUser();

                            if (winner == null || winner.trim().isEmpty()) {
                                showAlert("Phiên đấu giá kết thúc! Không có ai chiến thắng do chưa có lượt đặt giá nào.");
                            } else if (u != null && u.getUsername().equals(winner)) {
                                showAlert("🎉 CHÚC MỪNG!\nBạn đã chiến thắng phiên đấu giá này với mức giá " +" \n "+String.format("%,.0f VNĐ", targetAuction.getCurrentPrice()) + "!");
                            } else {
                                showAlert("Phiên đấu giá kết thúc!\n🏆 Người chiến thắng: " + winner + "\n💰 Mức giá chốt: " + String.format("%,.0f VNĐ", targetAuction.getCurrentPrice()));
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    break;

                case "BID_FAIL":
                    showAlert(msg.getPayload().toString());
                    break;
                case "AUTO_BID_SUCCESS":
                    Alert alertAuto = new Alert(Alert.AlertType.INFORMATION, "🤖 " + msg.getPayload().toString());
                    alertAuto.setTitle("Thành công"); alertAuto.setHeaderText(null); alertAuto.show();
                    break;
                case "AUTO_BID_FAIL":
                    showAlert("🤖 " + msg.getPayload().toString());
                    break;
            }
        });
    }

    private void navigate(javafx.event.ActionEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }

    /// Hàm tạo hiệu ứng thông báo Toast nổi lên rồi tự tắt
    private void showBidNotification(String message) {
        Platform.runLater(() -> {
            javafx.stage.Popup popup = new javafx.stage.Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            // Thiết kế giao diện cho thẻ thông báo (Nền cam, chữ trắng, bo góc, có bóng đổ)
            Label label = new Label(message);
            label.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 20; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

            popup.getContent().add(label);

            // Lấy tọa độ cửa sổ hiện tại (Dựa vào nút bidButton đang có sẵn trên màn hình)
            javafx.stage.Stage stage = (javafx.stage.Stage) bidButton.getScene().getWindow();

            // Đặt tọa độ cho Popup xuất hiện ở GÓC TRÊN BÊN PHẢI màn hình
            popup.show(stage, stage.getX() + (stage.getWidth() / 2) - 150, stage.getY() + 40);

            // Tạo một luồng ngầm đếm ngược 3 giây rồi tắt Popup đi để không làm đơ màn hình
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Thời gian hiển thị: 3 giây
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(popup::hide);
            }).start();
        });
    }


}