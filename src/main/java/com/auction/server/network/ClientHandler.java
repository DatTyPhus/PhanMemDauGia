package com.auction.server.network;

import com.auction.server.controller.*;
import com.auction.server.dao.*;
import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import com.auction.shared.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static com.google.gson.JsonParser.parseString;

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

                    /// Xử lý khi người dùng đăng nhập.
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

                    /// Xử lý khi người dùng đăng ký.
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
                            e.printStackTrace();
                            out.println(new Message("REGISTER_FAIL", "Lỗi Server: " + e.getMessage()).toJson());
                        }
                        break;

                    /// Xử lý khi seller thêm sản phẩm đấu giá .
                    case "ADD_ITEM":
                        try {

                            // 1.Nhận thông tin từ client gửi xuống.
                            String jsonStr = msg.getPayload().toString();
                            System.out.println("\n[SERVER] Nhận được sản phẩm mới: " + jsonStr);

                            // 2. Dùng JsonParser đọc trước JSON để lấy "itemType"
                            JsonObject jsonObj = parseString(jsonStr).getAsJsonObject();
                            String itemType = jsonObj.get("itemType").getAsString();

                            // 3. Dịch ngược JSON thành đối tượng Java (Factory)
                            Gson gson = new Gson();
                            Item itemObj = null;

                            if ("ART".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, Art.class);
                            } else if ("ELECTRONIC".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, Electronics.class);
                            } else if ("VEHICLE".equals(itemType)) {
                                itemObj = gson.fromJson(jsonStr, Vehicle.class);
                            }

                            if (itemObj != null) {
                                // 4. Đưa xuống tầng DAO để lưu vào CSDL
                                ItemDAO.instance().create(itemObj);

                                // 5. Phản hồi thành công về cho Seller
                                this.sendMessage(new Message("ADD_ITEM_SUCCESS", "Sản phẩm đã được gửi! Đang chờ Admin xét duyệt."));
                            }
                        } catch (Exception e) {
                            System.err.println("[SERVER ERROR] Lỗi khi xử lý ADD_ITEM: " + e.getMessage());
                            e.printStackTrace();
                            this.sendMessage(new Message("ADD_ITEM_FAIL", "Lỗi Server: " + e.getMessage()));
                        }
                        break;


//                    case "ADD_ITEM_SUCCESS":// đây sẽ là chỗ tạo ra các auction mới, sau đó gọi hàm timer để bắt đầu đếm ngược thời gian đấu giá
//                        Item item = (Item) msg.getPayload();
//                        ; // Tạo 1 auto bid mặc định cho mỗi sản phẩm mới (giá = giá khởi điểm)
//                        Auction auction = AuctionService.createAuction(item);
//                        // Tạo 1 hàng toàn giá trị mặc định có Auction id giống với auction id của auction để nếu cần thì dùng k cần thì tôi
//                        AutobidDAO.createAutobid(auction.getId());
//                        //lên sẵn lịch cho cuộc đấu giá đấy(kể cả khi chx thực hiện vẫn bắt đâì đếm ngc tg bắt đầu và kết thúc)
//                        AuctionSchedular.timer(auction);
//                        Message addItemSuccessResponse = new Message("ADD_ITEM_THANHCONG", auction);
//                        out.println(addItemSuccessResponse.toJson());
//                        break;
//
//                    case "ADD_ITEM_FAIL":
//                        String errorMsg = (String) msg.getPayload();
//                        Message errorResponse = new Message("ADD_ITEM_THATBAI", errorMsg);
//                        out.println(errorResponse.toJson());
//                        break;


                    /// Hàm này dùng để lấy các sản phẩm ở trạng thái PENDING lên cho admin chờ duyệt.
                    case "GET_PENDING_ITEMS":
                        System.out.println("[SERVER] Đã bắt được lệnh xin dữ liệu PENDING từ Admin!");

                        try {
                            List<Item> pendingList = com.auction.server.dao.ItemDAO.findPendingItems();

                            System.out.println("[SERVER] Tìm thấy " + pendingList.size() + " sản phẩm chờ duyệt. Đang đóng gói gửi về Client...");

                            out.println(new Message("RECEIVE_PENDING_ITEMS_SUCCESS", new com.google.gson.Gson().toJson(pendingList)).toJson());
                        } catch (Exception e) {
                            System.err.println("[SERVER ERROR] Lỗi khi xử lý PENDING: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm này xử lý khi người dùng vào trang QUẢN LÝ TÀI SẢN và muốn xem sản phẩm đã được đăng bán.
                    case "MY_PRODUCTS": {
                        String jsonStr = msg.getPayload().toString();

                        //  Dùng JsonParser đọc trước JSON để lấy "itemType"
                        JsonObject jsonObj = parseString(jsonStr).getAsJsonObject();
                        int id = jsonObj.get("id").getAsInt();

                        List<Item> sellerItems = ItemDAO.findItemsBySellerId(id); // Thay 1 bằng ID người bán thực tế
                        out.println(new Message("MY_PRODUCTS_SUCCESS", sellerItems).toJson());
                        break;
                    }

                    /// Hàm xử lý khi người dùng cấu hình auto-bid.
                    case "AUTO_BID":{
                        // Nhận thông tin từ client xuống.
                        String jsonStr = msg.getPayload().toString();
                        com.google.gson.JsonObject jsonObj = parseString(jsonStr).getAsJsonObject();

                        // Gán thông tin đã mã hóa sang thành các thuộc tính.
                        int auctionId = jsonObj.get("auctionId").getAsInt();
                        String bidderName = jsonObj.get("bidderName").getAsString();
                        BigDecimal autoBidAmount = jsonObj.get("autoBidAmount").getAsBigDecimal();
                        BigDecimal autoBidStep = jsonObj.get("autoBidStep").getAsBigDecimal();
                        // Tạo 1 kiểu đặt bid cho auto bid(nếu mà bidtransaction có 3 tham số là đặt bid bth, nếu 4 tham số là autobid)
                        BidTransaction autoBidTransaction = new BidTransaction(auctionId, bidderName, autoBidAmount, autoBidStep);
                        // coi tiếp trong file Auction service
                        Message message = AuctionService.processAutoBid(autoBidTransaction);
                        out.println(message.toJson());
                        break;
                    }

                    /// Hàm xử lý khi người dùng nạp tiền vào tài khoản
                    case "DEPOSIT":
                        try {
                            // Dữ liệu Client gửi lên có dạng: "ID,Role,Amount"
                            String depositStr = msg.getPayload().toString();
                            String[] depositData = depositStr.split(",");

                            int userId = Integer.parseInt(depositData[0]);
                            String role = depositData[1];
                            BigDecimal amountToAdd = new BigDecimal(depositData[2]);

                            boolean isSuccess = false;

                            // Kiểm tra Role để gọi đúng kho (DAO) cập nhật tiền
                            if (role.equalsIgnoreCase("BIDDER")) {
                                isSuccess = com.auction.server.dao.BidderDAO.updateBalance(userId, amountToAdd);
                            } else if (role.equalsIgnoreCase("SELLER")) {
                                // ĐÃ MỞ KHÓA: Gọi xuống kho Seller để cộng tiền
                                isSuccess = com.auction.server.dao.SellerDAO.updateBalance(userId, amountToAdd);
                            }

                            if (isSuccess) {
                                // Lấy số dư MỚI NHẤT từ database tương ứng gửi ngược về cho Client
                                BigDecimal newBalance = BigDecimal.ZERO;

                                if (role.equalsIgnoreCase("BIDDER")) {
                                    // lấy số dư mói của Bidder.
                                    newBalance = BidderDAO.getUserByUserid(userId).getBalance();
                                } else if (role.equalsIgnoreCase("SELLER")) {
                                    // Lấy số dư mới của Seller
                                    newBalance = SellerDAO.getSellersByUserid(userId).getBalance();
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

                    /// Hàm này thực hiện tác vụ khi người dùng thực hiện xóa tài sản đấu giá
                    case "DELETE_ITEM":
                        try {
                            // Lấy ID sản phẩm mà Client (Seller) gửi xuống
                            int itemIdToDelete = Integer.parseInt(msg.getPayload().toString());

                            // Gọi xuống kho ItemDAO để thực thi lệnh xóa bằng ID
                            boolean isDeleted = ItemDAO.deleteItem(itemIdToDelete);

                            // Phản hồi lại cho Client biết kết quả
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

                    /// Hàm này dùng để xử lý cập nhật khi admin duyệt sản phẩm lên thành đấu giá (chuyển từ PENDING sang ACTIVE)
                    case "APPROVE_ITEM":
                        try {
                            int itemIdToApprove = Integer.parseInt(msg.getPayload().toString());
                            boolean isApproved = ItemDAO.approveItem(itemIdToApprove);

                            if (isApproved) {

                                //Lấy thông tin chi tiết của sản phẩm vừa được duyệt lên
                                Item approvedItem = ItemDAO.selectById(itemIdToApprove);

                                if (approvedItem != null) {

                                    /// Chuyển hóa Item thành một cuộc đấu giá (Auction) và tính toán thời gian
                                    Auction newAuction = AuctionService.createAuction(approvedItem);

                                    // Tạo sẵn một record Autobid mặc định bảo vệ phiên đấu giá này.
                                    try {
                                        AutobidDAO.createAutobid(newAuction.getId());
                                    } catch (Exception ex) {
                                        System.err.println("[WARNING] Lỗi tạo Autobid mặc định: " + ex.getMessage());
                                    }

                                    //  Ném cuộc đấu giá này vào cỗ máy đếm ngược của hệ thống
                                    AuctionSchedular.timer(newAuction);

                                    out.println(new Message("APPROVE_ITEM_SUCCESS", "Đã duyệt sản phẩm và xếp lịch lên sàn thành công!").toJson());
                                } else {
                                    out.println(new Message("APPROVE_ITEM_FAIL", "Duyệt thành công nhưng lỗi khởi tạo phiên đấu giá.").toJson());
                                }
                            } else {
                                out.println(new Message("APPROVE_ITEM_FAIL", "Không tìm thấy sản phẩm để duyệt.").toJson());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("APPROVE_ITEM_FAIL", "Lỗi Server khi chuyển hóa sản phẩm thành phiên đấu giá.").toJson());
                        }
                        break;

                    /// Hàm này trả về số lượng người đang online. (Số lượng người đang kết nối tại cổng servercore)
                    case "GET_ONLINE_COUNT":
                        /// Trả về số lượng người dùng đang kết nối hiện tại cho Client vừa bật màn hình Home
                        out.println(new Message("UPDATE_ONLINE_COUNT", String.valueOf(ServerCore.getOnlineCount())).toJson());
                        break;

                   /// Hàm này trả xử lý yêu cầu số lượng tài sản (item) được seller đưa lên trong cả hệ thống.
                    case "GET_TOTAL_PRODUCTS":
                        try {
                            // Gọi xuống kho ItemDAO để lấy con số tổng tài sản mới nhất trong database
                            int totalCount = ItemDAO.getTotalItemsCount();

                            // Đóng gói con số và gửi phản hồi ngược lại cho Admin qua nhãn UPDATE_TOTAL_PRODUCTS
                            out.println(new Message("UPDATE_TOTAL_PRODUCTS", String.valueOf(totalCount)).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm này xử lý khi admin muốn xem danh sách người dùng đang có mặt ở hệ thống.
                    case "GET_ALL_USERS":
                        try {

                            // Lấy danh sách từ cả 2 kho database (Biider và Seller) và gộp lại thành 1 list chung
                            List<User> allUsers = new ArrayList<>();
                            allUsers.addAll(BidderDAO.getAllBidders());
                            allUsers.addAll(SellerDAO.getAllSellers());

                            // Đóng gói gửi mảng JSON về cho Client
                            out.println(new Message("RECEIVE_ALL_USERS_SUCCESS", new com.google.gson.Gson().toJson(allUsers)).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("RECEIVE_ALL_USERS_FAIL", "Lỗi Server không thể tải danh sách thành viên.").toJson());
                        }
                        break;

                    /// Hàm này xử lý khi admin yêu cầu xem toàn bộ các phiên đấu giá trong hệ thống.
                    case "GET_ALL_AUCTIONS":
                        try {
                            ///Lấy toàn bộ danh sách phiên đấu giá từ kho dữ liệu lên
                            List<Auction> auctionList = AuctionDAO.getAllAuctions();
                            out.println(new Message("RECEIVE_ALL_AUCTIONS_SUCCESS", new Gson().toJson(auctionList)).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm này xử lý khi client yêu cầu xem thông tin chi tiết của tài sản trong phòng đấu giá.
                    case "GET_ITEM_DETAILS":
                        try {
                            //Bóc tách ID sản phẩm do Client gửi lên
                            int itemIdForDetail = Integer.parseInt(msg.getPayload().toString());
                            System.out.println("[SERVER] Đang nạp chi tiết sản phẩm ID: " + itemIdForDetail + " cho Client...");

                            // Gọi hàm selectById từ kho ItemDAO (Hàm này đã lấy đủ ảnh, phân loại, mô tả)
                            Item itemDetails = ItemDAO.selectById(itemIdForDetail);

                            if (itemDetails != null) {
                                // Đóng gói thực thể thành JSON và trả thẳng về máy Client
                                out.println(new Message("RECEIVE_ITEM_DETAILS", new com.google.gson.Gson().toJson(itemDetails)).toJson());
                            } else {
                                System.err.println("[SERVER WARNING] Không tìm thấy thông tin sản phẩm ID: " + itemIdForDetail);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm này xử lý khi admin muốn xóa người dùng vĩnh viễn ra khỏi hệ thống.
                    case "DELETE_USER":
                        try {
                            // Bóc payload chứa chuỗi ghép: "ID,ROLE" từ client gửi xuống
                            String[] data = msg.getPayload().toString().split(",");
                            int userId = Integer.parseInt(data[0]);
                            String role = data[1];

                            boolean deletedResult = false;

                            // Kiểm tra vai trò để gọi lệnh xóa xuống đúng bảng tương ứng
                            if ("BIDDER".equalsIgnoreCase(role)) {
                                deletedResult = BidderDAO.deleteBidder(userId);
                            } else if ("SELLER".equalsIgnoreCase(role)) {
                                deletedResult = SellerDAO.deleteSeller(userId);
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

                    /// Hàm này xử lý khi có người đặt giá trong phòng đấu giá.
                    case "BID":
                        try {
                            // Lấy thông tin từ payload gửi từ client xuống.
                            String[] parts = msg.getPayload().toString().split(",");
                            int auctionId = Integer.parseInt(parts[0]);
                            BigDecimal bidAmount = new BigDecimal(parts[1]);
                            String bidderName = parts[2];

                            BidTransaction newBid = new BidTransaction(auctionId, bidderName, bidAmount, BigDecimal.ZERO);
                            Message bidResult = AuctionService.processBid(newBid);

                            if ("BID_SUCCESS".equals(bidResult.getAction())) {

                                // Ép thêm ID phòng vào JSON để Client biết  mà cập nhật
                                JsonObject obj = parseString(bidResult.getPayload().toString()).getAsJsonObject();
                                obj.addProperty("auctionId", auctionId);
                                bidResult.setPayload(obj.toString());

                                /// GỌI LỆNH GỬI CHO CHO TẤT CẢ CLIENT ĐANG KẾT NỐI
                                ServerCore.broadcastMessage(bidResult);
                            } else {
                                /// Nếu lỗi (thiếu tiền, v.v) thì chỉ báo riêng cho người vừa bấm
                                out.println(bidResult.toJson());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            out.println(new Message("BID_FAIL", "Lỗi xử lý đặt giá trên Server!").toJson());
                        }
                        break;

                    /// Hàm này để xử lý khi người dùng muốn xem số dư tài khoản mới nhất
                    case "GET_MY_BALANCE":
                        try {
                            // Lấy thông tin từ payload gửi từ client xuống.
                            String[] parts = msg.getPayload().toString().split(",");
                            String user = parts[0];
                            String userRole = parts[1];

                            User genericUser = null;
                            // Xét role để lấy từ bảng ở database.
                            if ("SELLER".equalsIgnoreCase(userRole)) genericUser =com.auction.server.dao.SellerDAO.selectByUsername(user);
                            else genericUser = com.auction.server.dao.BidderDAO.selectByUsername(user);

                            if (genericUser != null) {
                                out.println(new Message("UPDATE_BALANCE_UI", genericUser.getBalance().toString()).toJson());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm xử lý khi client muốn xem lịch sửu đấu giá của tài sản trong phòng đấu giá (Bảng bên dưới góc phải phòng đấu giá).
                    case "GET_BID_HISTORY":
                        try {
                            //Lấy thông tin về ID cuộc đấu giá để tìm lịch sử đấu giá và lưu các lịch sử đấu giá vào một danh sách.
                            int auctionIdForHistory = Integer.parseInt(msg.getPayload().toString());
                            List<String[]> historyList = BidTransactionDAO.getHistoryByAuctionId(auctionIdForHistory);
                            out.println(new Message("RECEIVE_BID_HISTORY", new Gson().toJson(historyList)).toJson());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    /// Hàm xử lý khi Client xin danh sách phiên đấu giá đã THẮNG và trả về một danh sách các cuộc đấu giá mà mình đã thắng.
                    case "GET_WIN_HISTORY":
                        try {
                            String rawPayload = msg.getPayload().toString();
                            // Cắt bỏ mọi dấu ngoặc kép và khoảng trắng thừa
                            String username = rawPayload.replace("\"", "").trim();

                            System.out.println("[SERVER] Đang truy xuất Lịch sử thắng cuộc cho user: [" + username + "]");

                            List<Auction> wonList = BidTransactionDAO.getWonAuctionsByUsername(username);
                            System.out.println("[SERVER] Tìm thấy " + wonList.size() + " trận thắng!");

                            // Tạo đối tượng JsonArray để chuỗi hóa danh sách và gửi client.
                            JsonArray jsonArray = new JsonArray();
                            for (Auction a : wonList) {
                                JsonObject obj = new JsonObject();
                                obj.addProperty("auctionId", a.getId());
                                obj.addProperty("itemName", a.getItemName());
                                obj.addProperty("endTime", a.getEndTime());
                                obj.addProperty("currentPrice",a.getCurrentPrice());
                                jsonArray.add(obj);
                            }
                            out.println(new Message("RECEIVE_WIN_HISTORY", jsonArray.toString()).toJson());
                        } catch (Exception e) {
                            e.printStackTrace();
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
