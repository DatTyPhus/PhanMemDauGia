package com.auction.server.network;

import com.auction.server.controller.*;
import com.auction.server.dao.*;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.auction.shared.model.*;

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
                            // 1. Nhận chuỗi JSON từ Client của Đạt gửi lên
                            String jsonStr = msg.getPayload().toString();
                            System.out.println("\n[SERVER] Nhận được sản phẩm mới: " + jsonStr);

                            // 2. Dùng JsonParser đọc trước JSON để lấy "itemType"
                            com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();

                            // LƯU Ý: Phải get đúng chữ "itemType" vì class Item.java khai báo biến này
                            String itemType = jsonObj.get("itemType").getAsString();

                            // 3. Dịch ngược JSON thành đối tượng Java (Factory)
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            com.auction.shared.model.Item itemObj = null;

                            // Chú ý: Value giờ là "ART", "ELECTRONIC", "VEHICLE" (Không có S)
                            if ("ART".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, com.auction.shared.model.Art.class);
                            } else if ("ELECTRONIC".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, com.auction.shared.model.Electronics.class);
                            } else if ("VEHICLE".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, com.auction.shared.model.Vehicle.class);
                            }

                            if (itemObj != null) {
                                // 4. Đưa xuống tầng DAO để lưu vào CSDL
                                com.auction.server.dao.ItemDAO.instance().create(itemObj);

                                // 5. Phản hồi thành công về cho Seller
                                this.sendMessage(new Message("ADD_ITEM_SUCCESS", "Sản phẩm đã được gửi! Đang chờ Admin xét duyệt."));
                            }
                        } catch (Exception e) {
                            System.err.println("[SERVER ERROR] Lỗi khi xử lý ADD_ITEM: " + e.getMessage());
                            e.printStackTrace();
                            this.sendMessage(new Message("ADD_ITEM_FAIL", "Lỗi Server: " + e.getMessage()));
                        }
                        break;

                    case "ADD_ITEM_SUCCESS":// đây sẽ là chỗ tạo ra các auction mới, sau đó gọi hàm timer để bắt đầu đếm ngược thời gian đấu giá
                        Item item = (Item) msg.getPayload();

                        Auction auction = AuctionService.createAuction(item);
                        AuctionSchedular.timer(auction);
                        Message addItemSuccessResponse = new Message("ADD_ITEM_THANHCONG", auction);
                        out.println(addItemSuccessResponse.toJson());
                        break; 
                    case "ADD_ITEM_FAIL":
                        String errorMsg = (String) msg.getPayload();
                        Message errorResponse = new Message("ADD_ITEM_THATBAI", errorMsg);
                        out.println(errorResponse.toJson());
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
