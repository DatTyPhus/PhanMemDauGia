package com.auction.client.controller;

import com.auction.shared.model.Auction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// class CardController dùng để quản lý giao diện và nạp dữ liệu cho một tấm thẻ Card đấu giá phiên bản cải tiến thẩm mỹ cao.
public class CardController {

    /// Link các nhãn với các thông số trên màn hình
    @FXML private ImageView image;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label timeLabel;
    @FXML private Label priceLabel;
    @FXML private Label startPriceLabel;

    private Auction currentAuction; /// Thực thể phiên đấu giá hiện hành gắn liền với Card

    /// Hàm setData tiếp nhận thực thể dữ liệu từ AuctionController và đắp lên giao diện mới
    public void setData(Auction auction) {
        this.currentAuction = auction;

        if (titleLabel != null) titleLabel.setText(auction.getItemName());

        ///  Dữ liệu được gán chuẩn xác, Giá hiện tại (xanh lớn), Giá khởi điểm (xám nhỏ)
        if (priceLabel != null) priceLabel.setText(formatCurrency(auction.getCurrentPrice()));
        if (startPriceLabel != null) startPriceLabel.setText(formatCurrency(auction.getStartingPrice()));


        /// XỬ LÝ THỜI GIAN THỰC VÀ ÉP TRẠNG THÁI HIỂN THỊ THÔNG MINH

        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        LocalDateTime start = null;
        LocalDateTime end = null;
        LocalDateTime now = LocalDateTime.now(); /// Lấy thời gian hiện tại của máy tính
        String computedStatus = "CLOSED"; /// Mặc định đóng nếu có lỗi

        try {
            /// 1. Dịch chuỗi và hiển thị Ngày giờ Bắt đầu
            if (auction.getStartTime() != null && !auction.getStartTime().isEmpty()) {
                start = LocalDateTime.parse(auction.getStartTime(), dbFormatter);
                if (startTimeLabel != null) startTimeLabel.setText("🟢 Bắt đầu:  " + start.format(displayFormatter));
            }

            /// 2. Dịch chuỗi và hiển thị Ngày giờ Kết thúc
            if (auction.getEndTime() != null && !auction.getEndTime().isEmpty()) {
                end = LocalDateTime.parse(auction.getEndTime(), dbFormatter);
                if (timeLabel != null) timeLabel.setText("🔴 Kết thúc: " + end.format(displayFormatter));
            }

            /// 3. Thể hiện trạng thái dựa trên việc so sánh thời gian
            if (start != null && end != null) {
                if (now.isBefore(start)) {
                    computedStatus = "WAITING"; /// Thời gian hiện tại nhỏ hơn lúc Bắt đầu -> Sắp diễn ra
                } else if (now.isAfter(end)) {
                    computedStatus = "CLOSED";  /// Thời gian hiện tại lớn hơn lúc Kết thúc -> Đã xong
                } else {
                    computedStatus = "OPEN";    /// Thời gian nằm ở giữa -> Đang diễn ra
                }
            }
        } catch (Exception e) {
            System.err.println("[UI WARNING] Lỗi định dạng thời gian ở Card ID: " + auction.getItemId());
        }

        /// 4. Đổ màu Huy hiệu (Badge) dựa trên kết quả phán đoán thời gian vừa rồi
        if (statusLabel != null) {
            switch (computedStatus) {
                case "OPEN":
                    statusLabel.setText("ĐANG DIỄN RA");
                    statusLabel.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; -fx-padding: 4 10; -fx-background-radius: 20;");
                    break;
                case "WAITING":
                    statusLabel.setText("SẮP DIỄN RA");
                    statusLabel.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; -fx-padding: 4 10; -fx-background-radius: 20;");
                    break;
                case "CLOSED":
                default:
                    statusLabel.setText("ĐÃ KẾT THÚC");
                    statusLabel.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; -fx-padding: 4 10; -fx-background-radius: 20;");
                    break;
            }
        }

        /// 5. Bọc an toàn xử lý hình ảnh sản phẩm đấu giá
        if (image != null) {
            try {
                if (auction.getEndTime() != null && !auction.getEndTime().isEmpty()) {
                    // image.setImage(new Image(auction.getImageUrl()));
                }
            } catch (Exception e) {}
        }
    }

    /// Chuyển đổi con số BigDecimal thành chuỗi định dạng VNĐ dễ nhìn
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,.0f đ", amount);
    }

    /// Luồng xử lý sự kiện khi người dùng click chuột vào nút xem chi tiết trên tấm Card
    @FXML
    public void onDetailClick(javafx.event.ActionEvent event) {
        if (currentAuction != null) {
            try {
                // 1. Tải phòng đấu giá
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/bidding.fxml"));
                Parent root = loader.load();

                // 2. Lấy Controller của phòng đấu giá ra để nạp dữ liệu sản phẩm
                BiddingController biddingController = loader.getController();
                biddingController.setAuctionData(currentAuction);

                // 3. Chuyển màn hình
                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

                // 4. Thay giao diện mới
                source.getScene().setRoot(root);
                currentStage.setTitle("PHÒNG ĐẤU GIÁ: " + currentAuction.getItemName());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}