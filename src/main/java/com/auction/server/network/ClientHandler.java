package com.auction.server.network;

import com.auction.server.controller.AccountService;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Class dùng để đọc file JSON trên client gửi xuống , vừa kiểm tra nhãn dán rồi ném đến controller.

public class ClientHandler implements Runnable {
    private Socket socket;

    // Đưa Socket của Client vào qua Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        // Thiết lập luồng Đọc dữ liệu (InputStream)

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Luồng Ghi dữ liệu (OutputStream)
            String jsonReceived;

            while ((jsonReceived = in.readLine()) != null) {

                Message msg = Message.fromJson(jsonReceived);            //Mã hoá JSON về Message.

                // Điều phối công việc dựa trên nhãn dán "action"
                switch (msg.getAction()) {
                    case "LOGIN":
                        AccountService accountService = new AccountService();
                        User user = (User) msg.getPayload();
                        Message message = accountService.login(user.getUsername(), user.getPassword());
                        out.println(message.toJson());
                    case "REGISTER":
                        AccountService accountService1 = new AccountService();
                        User user1 = (User) msg.getPayload();
                        Message message1 = accountService1.register(user1.getUsername(), user1.getPassword(), user1.getFullName(), user1.getRole());
                        out.println(message1.toJson());
                    case "ADD_ITEM":
                        // Xử lý thêm sản phẩm....
                    case "CREATE_AUCTION":
                        // Xử lý tạo cuộc đấu giá. payload vd : {"itemId": 15, "endTime": "2026-05-01 10:00:00"}.
                    case "GET_ACTIVE_AUCTIONS":
                        // Yêu cầu server trả về danh sách các phiên đấu giá đang mở. payload : null.
                    case "GET_MY_ITEMS":
                        // Xử lý khi Seller muốn xem kho đồ của mình. payload : null.
                    case "PLACE_BID":
                        // Xử lý khi bidder bấm nút đặt giá. payload vd : {"auctionId": 1, "bidAmount": 500000}

                    default:
                        System.out.println("Không hiểu lệnh này!");
                    }
                }
        }
        catch(IOException e){
            System.out.println("Client " + socket.getInetAddress() + " đã ngắt kết nối đột ngột.");
        }
    }


    
}
