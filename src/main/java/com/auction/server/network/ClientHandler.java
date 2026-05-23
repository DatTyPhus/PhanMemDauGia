package com.auction.server.network;

import com.auction.server.controller.*;
import com.auction.server.dao.*;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

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
                    case "PENDING_PRODUCTS":
                        List<Item> pendingItems = ItemDAO.findPendingItems();
                        Message pendingItemsResponse = new Message("PENDING_PRODUCTS", pendingItems);
                        out.println(pendingItemsResponse.toJson());
                        break;
                    case "MY_PRODUCTS": {
                        String jsonStr = msg.getPayload().toString();

                        // 2. Dùng JsonParser đọc trước JSON để lấy "itemType"
                        com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();

                        // LƯU Ý: Phải get đúng chữ "itemType" vì class Item.java khai báo biến này
                        int id = jsonObj.get("id").getAsInt();

                        List<Item> sellerItems = ItemDAO.findItemsBySellerId(id); // Thay 1 bằng ID người bán thực tế
                        out.println(new Message("MY_PRODUCTS_SUCCESS", sellerItems).toJson());
                        break;
                    }
                    case "DEPOSIT":
                        try {
                            // Dữ liệu Client gửi lên có dạng: "ID,Role,Amount"
                            String depositStr = msg.getPayload().toString();
                            String[] depositData = depositStr.split(",");

                            int userId = Integer.parseInt(depositData[0]);
                            String role = depositData[1];
                            java.math.BigDecimal amountToAdd = new java.math.BigDecimal(depositData[2]);

                            boolean isSuccess = false;

                            /// Kiểm tra Role để gọi đúng kho (DAO) cập nhật tiền
                            if (role.equalsIgnoreCase("BIDDER")) {
                                isSuccess = com.auction.server.dao.BidderDAO.updateBalance(userId, amountToAdd);
                            } else if (role.equalsIgnoreCase("SELLER")) {
                                /// ĐÃ MỞ KHÓA: Gọi xuống kho Seller để cộng tiền
                                isSuccess = com.auction.server.dao.SellerDAO.updateBalance(userId, amountToAdd);
                            }

                            if (isSuccess) {
                                /// Lấy số dư MỚI NHẤT từ database tương ứng gửi ngược về cho Client
                                java.math.BigDecimal newBalance = java.math.BigDecimal.ZERO;

                                if (role.equalsIgnoreCase("BIDDER")) {
                                    newBalance = com.auction.server.dao.BidderDAO.getUserByUserid(userId).getBalance();
                                } else if (role.equalsIgnoreCase("SELLER")) {
                                    /// ĐÃ MỞ KHÓA: Lấy số dư mới của Seller
                                    newBalance = com.auction.server.dao.SellerDAO.getSellersByUserid(userId).getBalance();
                                }

                                out.println(new Message("DEPOSIT_SUCCESS", newBalance.toString()).toJson());
                            } else {
                                out.println(new Message("DEPOSIT_FAIL", "Không tìm thấy tài khoản hoặc nạp thất bại.").toJson());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("DEPOSIT_FAIL", "Lỗi xử lý nạp tiền trên Server.").toJson());
                        }
                        break;
                    case "DELETE_ITEM":
                        try {
                            /// Lấy ID sản phẩm mà Client (Seller) gửi lên
                            int itemIdToDelete = Integer.parseInt(msg.getPayload().toString());

                            /// Gọi xuống kho ItemDAO để thực thi lệnh xóa
                            boolean isDeleted = com.auction.server.dao.ItemDAO.deleteItem(itemIdToDelete);

                            /// Phản hồi lại cho Client biết kết quả
                            if (isDeleted) {
                                out.println(new Message("DELETE_ITEM_SUCCESS", "Sản phẩm đã được xóa khỏi hệ thống!").toJson());
                            } else {
                                out.println(new Message("DELETE_ITEM_FAIL", "Không tìm thấy sản phẩm để xóa.").toJson());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("DELETE_ITEM_FAIL", "Lỗi Server khi xóa sản phẩm.").toJson());
                        }
                        break;
                    case "GET_ONLINE_COUNT":
                        /// Trả về số lượng người dùng đang kết nối hiện tại cho Client vừa bật màn hình Home
                        out.println(new Message("UPDATE_ONLINE_COUNT", String.valueOf(ServerCore.getOnlineCount())).toJson());
                        break;
                    case "GET_TOTAL_PRODUCTS":
                        try {
                            /// Gọi xuống kho ItemDAO để lấy con số tổng tài sản mới nhất trong database
                            int totalCount = com.auction.server.dao.ItemDAO.getTotalItemsCount();

                            /// Đóng gói con số và gửi phản hồi ngược lại cho Admin qua nhãn UPDATE_TOTAL_PRODUCTS
                            out.println(new Message("UPDATE_TOTAL_PRODUCTS", String.valueOf(totalCount)).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "GET_ALL_USERS":
                        try {
                            /// Lấy danh sách từ cả 2 kho database và gộp lại thành 1 list chung
                            java.util.List<com.auction.shared.model.User> allUsers = new java.util.ArrayList<>();
                            allUsers.addAll(com.auction.server.dao.BidderDAO.getAllBidders());
                            allUsers.addAll(com.auction.server.dao.SellerDAO.getAllSellers());

                            /// Đóng gói gửi mảng JSON về cho Client
                            out.println(new Message("RECEIVE_ALL_USERS_SUCCESS", new com.google.gson.Gson().toJson(allUsers)).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("RECEIVE_ALL_USERS_FAIL", "Lỗi Server không thể tải danh sách thành viên.").toJson());
                        }
                        break;

                    case "DELETE_USER":
                        try {
                            /// Bóc payload chứa chuỗi ghép: "ID,ROLE" từ client gửi xuống
                            String[] data = msg.getPayload().toString().split(",");
                            int userId = Integer.parseInt(data[0]);
                            String role = data[1];

                            boolean deletedResult = false;
                            /// Kiểm tra vai trò để gọi lệnh xóa xuống đúng bảng tương ứng
                            if ("BIDDER".equalsIgnoreCase(role)) {
                                deletedResult = com.auction.server.dao.BidderDAO.deleteBidder(userId);
                            } else if ("SELLER".equalsIgnoreCase(role)) {
                                deletedResult = com.auction.server.dao.SellerDAO.deleteSeller(userId);
                            }

                            if (deletedResult) {
                                out.println(new Message("DELETE_USER_SUCCESS", "Đã xóa tài khoản thành công khỏi hệ thống!").toJson());
                            } else {
                                out.println(new Message("DELETE_USER_FAIL", "Xóa tài khoản thất bại hoặc không tìm thấy.").toJson());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("DELETE_USER_FAIL", "Lỗi Server khi thực hiện xóa người dùng.").toJson());
                        }
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
