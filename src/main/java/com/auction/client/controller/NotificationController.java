package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class NotificationController {

    @FXML
    public void onBackToHomeClick(ActionEvent event) {
        try {
            // Đi tìm lại file home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/home.fxml"));
            Parent root = loader.load();

            // Lấy cửa sổ hiện tại và thay ruột
            Node source = (Node) event.getSource();
            source.getScene().setRoot(root);

            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.setTitle("Trang chủ Đấu Giá");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}