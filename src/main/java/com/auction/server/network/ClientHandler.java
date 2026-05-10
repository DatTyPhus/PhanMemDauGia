package com.auction.server.network;

import com.auction.server.controller.AccountService;
import com.auction.shared.model.User;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out; // BỔ SUNG: Ống gửi dữ liệu xuống Client

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Khởi tạo cả ống nghe (in) và ống nói (out)
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // autoFlush = true để đẩy dữ liệu đi ngay lập tức
            out = new PrintWriter(socket.getOutputStream(), true);

            String jsonReceived;

            while ((jsonReceived = in.readLine()) != null) {
                Message msg = Message.fromJson(jsonReceived);

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
                        System.out.println("Có người đặt giá: " + msg.getPayload());
                        break;

                    default:
                        System.out.println("Không hiểu lệnh này: " + msg.getAction());
                }
            }
        } catch (IOException e) {
            System.out.println("Client " + socket.getInetAddress() + " đã ngắt kết nối.");
        } finally {
            // Khi Client thoát app (đứt ống), phải báo ServerCore xoá khỏi danh sách
            ServerCore.removeClient(this);
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Hàm để ServerCore hoặc Controller gọi khi muốn gửi tin nhắn lại cho riêng Client này
    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toJson());
        }
    }


    
}
