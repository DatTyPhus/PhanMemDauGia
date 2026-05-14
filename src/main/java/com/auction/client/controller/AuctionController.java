package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AuctionController {

    @FXML
    public void initialize() {
        // Hàm này sẽ tự động chạy khi giao diện home.fxml được mở lên
        System.out.println("Giao diện Home đã được load thành công!");
    }

    // Các hàm trống này để chống lỗi khi bạn bấm vào các nút Filter trên giao diện
    @FXML
    public void filterAll(ActionEvent event) { }

    @FXML
    public void filterRunning(ActionEvent event) { }

    @FXML
    public void filterUpcoming(ActionEvent event) { }

    @FXML
    public void filterFinished(ActionEvent event) { }
}