package com.auction.server.network;

import com.auction.server.controller.AccountService;
import com.auction.server.controller.AuctionSchedular;
import com.auction.server.controller.AuctionService;
import com.auction.shared.model.Auction;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));            // autoFlush = true để đẩy dữ liệu đi ngay lập tức
            out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
            String jsonReceived;

            while ((jsonReceived = in.readLine()) != null) {
                Message msg = Message.fromJson(jsonReceived);

                switch (msg.getAction()) {
                    case "LOGIN":
                        AccountService accountService = new AccountService();

                        // Ở LoginController, ta gửi lên chuỗi "user,pass", nên phải bóc bằng lệnh split
                        String payloadStr = msg.getPayload().toString();
                        String[] loginData = payloadStr.split(",");
                        String loginUser = loginData[0];
                        String loginPass = loginData[1];

                        Message loginResult = accountService.login(loginUser, loginPass);
                        out.println(loginResult.toJson());
                        break;

                    case "REGISTER":
                        try {
                            java.util.Map<String, Object> map = (java.util.Map<String, Object>) msg.getPayload();
                            String regUser = (String) map.get("username");
                            String regPass = (String) map.get("password");
                            String regName = (String) map.get("fullName");
                            String regRole = (String) map.get("role");

                            AccountService accountService1 = new AccountService();
                            Message regResult = accountService1.register(regUser, regPass, regName, regRole);
                            out.println(regResult.toJson()); // Gửi kết quả về cho Đạt
                            break;

                        } catch (Exception e) {
                            e.printStackTrace(); // In lỗi đỏ ra console Server để dev đọc
                            // Gửi thông báo lỗi về cho Client để UI không bị đơ
                            out.println(new Message("REGISTER_FAIL", "Lỗi Server: " + e.getMessage()).toJson());
                        }
                        break;
                    case "ADD_ITEM":
                        try {
                            java.util.Map<String, Object> map = (java.util.Map<String, Object>) msg.getPayload();
                            String itemName = (String) map.get("itemName");
                            double startingPriceDouble = (double) map.get("startingPrice");
                            int durationMinutes = (int) map.get("durationMinutes");

                            AuctionService auctionService = new AuctionService();
                            Message addItemResult = auctionService.createAuction(itemName, BigDecimal.valueOf(startingPriceDouble), durationMinutes);
                            out.println(addItemResult.toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("ADD_ITEM_FAIL", "Lỗi Server: " + e.getMessage()).toJson());
                        }
                        break;
                    case "ADD_ITEM_SUCCESS":
                        AuctionSchedular auctionSchedular = new AuctionSchedular();
                        Auction auction = (Auction) msg.getPayload();
                        auctionSchedular.updateTimeLine(auction);
                        auctionSchedular.start(auction);
                        
                        break; 
                    case "ADD_ITEM_FAIL":
                         
                        break;
                    case "GET_MY_ITEMS":
                        // Xử lý khi Seller muốn xem kho đồ của mình. payload : null.
                        break;
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
