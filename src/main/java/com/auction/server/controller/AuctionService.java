/*thực hiện các chức năng liên quan đến đấu giá, như tạo đấu giá, 
đặt giá, kết thúc đấu giá, v.v.
Đảm bảo khi 2 người cùng đặt giá, nó sẽ xếp hàng cho từng người một, 
không để xảy ra tình trạng 1 món đồ bán cho 2 người. 
*/
package com.auction.server.controller;

import java.math.BigDecimal;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.auction.server.dao.*;
import com.auction.server.network.ServerCore;
import com.auction.shared.model.*;
import com.auction.shared.network.Message;
import com.google.gson.JsonObject;

/// Class AuctionService dùng để thực hiện các tác vụ trong phòng đấu giá , như đặt bid,.............

public class AuctionService {

    //Các thông số khi cấu hình auto_bid
    private static BigDecimal autoBidAmount = BigDecimal.ZERO;
    private static String autoBidderName = null;
    private static BigDecimal autoBiddStep = BigDecimal.ZERO;

    // Kho chứa "ổ khóa" độc lập cho từng phòng đấu giá
    private static final java.util.concurrent.ConcurrentHashMap<Integer, java.util.concurrent.locks.ReentrantLock> auctionLocks = new java.util.concurrent.ConcurrentHashMap<>();

    private static Auction currentAuction;
    protected static Map<Integer, Auction> waitingAuctions;

    /// Hàm tạo phiên đấu giá mới và tự động xếp hàng nối đuôi chính xác tuyệt đối
    public static Auction createAuction(Item item) {
        Auction auction = new Auction();
        auction.setItemId(item.getId());
        auction.setItemName(item.getName());
        auction.setCurrentPrice(item.getStartingPrice());
        auction.setDurationMinutes(item.getDurationMinutes());

        LocalDateTime latestEndTime = AuctionDAO.getLatestEndTime();
        LocalDateTime now = LocalDateTime.now();

        if (latestEndTime == null || now.isAfter(latestEndTime)) {
            auction.setStartTime(auction.changeTimetoString(now));
            auction.setEndTime(auction.changeTimetoString(now.plusMinutes(item.getDurationMinutes())));
        } else {
            auction.setStartTime(auction.changeTimetoString(latestEndTime));
            auction.setEndTime(auction.changeTimetoString(latestEndTime.plusMinutes(item.getDurationMinutes())));
        }

        auction.setStatus("PENDING");
        AuctionDAO.create(auction);
        return auction;
    }

    /// Hàm xử lý khi người dùng ấn nút Cài đặt Auto-Bid
    public static Message processAutoBid(BidTransaction newAutoBid) {
        Auction auction = AuctionDAO.selectById(newAutoBid.getAuctionId());
        if (auction == null || !"OPEN".equalsIgnoreCase(auction.getStatus())) return new Message("AUTO_BID_FAIL", "Phiên đấu giá chưa mở!");

        User currentUser = getGenericUser(newAutoBid.getBiddername());

        /// Tính số dư thực tế nếu người dùng đang giữ Top 1

        BigDecimal effectiveBalance = currentUser.getBalance();
        if (newAutoBid.getBiddername().equals(auction.getHighestBidderName())) {
            effectiveBalance = effectiveBalance.add(auction.getCurrentPrice());
        }

        if (effectiveBalance.compareTo(newAutoBid.getBidAmount()) < 0) {
            return new Message("AUTO_BID_FAIL", "Số dư không đủ để gánh mức giá Tối đa bạn vừa nhập.");
        }

        if (newAutoBid.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
            return new Message("AUTO_BID_FAIL", "Mức giá tối đa phải cao hơn giá hiện tại của tài sản.");
        }

        auction.lock();
        try {
            BidTransaction currentBot = AutobidDAO.getAutoBidsByAuctionId(auction.getId());

            /// Các điều kiện theo từng tầng khi cấu hình auto-bid
            if (currentBot != null && !currentBot.getBiddername().equals(newAutoBid.getBiddername())) {
                if (newAutoBid.getBidAmount().compareTo(currentBot.getBidAmount()) <= 0) {
                    BigDecimal targetPrice = newAutoBid.getBidAmount().add(currentBot.getStep());
                    if (targetPrice.compareTo(currentBot.getBidAmount()) > 0) targetPrice = currentBot.getBidAmount();

                    executeDirectBid(new BidTransaction(auction.getId(), currentBot.getBiddername(), targetPrice, BigDecimal.ZERO), auction);
                    return new Message("AUTO_BID_FAIL", "Từ chối! Đã có người thiết lập giới hạn Auto cao hơn bạn. Hệ thống đã tự động nâng giá sàn!");
                } else {
                    BigDecimal targetPrice = currentBot.getBidAmount().add(newAutoBid.getStep());
                    if (targetPrice.compareTo(newAutoBid.getBidAmount()) > 0) targetPrice = newAutoBid.getBidAmount();

                    AutobidDAO.updateAutobid(auction.getId(), newAutoBid.getBidAmount(), newAutoBid.getStep(), newAutoBid.getBiddername());
                    executeDirectBid(new BidTransaction(auction.getId(), newAutoBid.getBiddername(), targetPrice, BigDecimal.ZERO), auction);
                    return new Message("AUTO_BID_SUCCESS", "Tuyệt vời! Bạn đã chiếm quyền Auto-Bid thành công!");
                }
            } else {
                BigDecimal targetPrice = auction.getCurrentPrice().add(newAutoBid.getStep());
                if (targetPrice.compareTo(newAutoBid.getBidAmount()) > 0) targetPrice = newAutoBid.getBidAmount();

                AutobidDAO.updateAutobid(auction.getId(), newAutoBid.getBidAmount(), newAutoBid.getStep(), newAutoBid.getBiddername());
                executeDirectBid(new BidTransaction(auction.getId(), newAutoBid.getBiddername(), targetPrice, BigDecimal.ZERO), auction);
                return new Message("AUTO_BID_SUCCESS", "Đã thiết lập hệ thống tự động Đấu giá!");
            }
        } finally {
            auction.unlock();
        }
    }

    /// Hàm xử lý đặt giá tay hoặc đặt giá sẵn .
    public static Message processBid(BidTransaction bidTransaction) {

        // 1. LẤY ĐÚNG Ổ KHÓA CỦA PHÒNG NÀY (Nếu phòng chưa có khóa thì tạo mới 1 cái duy nhất)
        ReentrantLock roomLock = auctionLocks.computeIfAbsent(bidTransaction.getAuctionId(), k -> new ReentrantLock());

        // 2. Xin quyền trao chìa khóa nội tại.
        roomLock.lock();

        try {
            // 3. SAU KHI VÀO ĐƯỢC TRONG PHÒNG, BẮT ĐẦU QUÉT DATABASE LẤY DỮ LIỆU MỚI NHẤT
            Auction auction = AuctionDAO.selectById(bidTransaction.getAuctionId());
            LocalDateTime now = LocalDateTime.now();

            if (auction == null) return new Message("BID_FAIL", "Đấu giá không tồn tại.");
            if (!"OPEN".equalsIgnoreCase(auction.getStatus())) return new Message("BID_FAIL", "Chỉ có thể đặt giá khi đang diễn ra.");

            Item item = ItemDAO.selectById(auction.getItemId());
            User user = SellerDAO.getSellersByUserid(item.getSellerId());

            if (item != null && bidTransaction.getBiddername().equals(user.getUsername())) {
                return new Message("BID_FAIL", "Bạn không thể tự đặt giá cho sản phẩm do chính mình đăng bán!");
            }

            LocalDateTime endTimeObj = auction.changeStringToTime(auction.getEndTime());
            if (endTimeObj != null && now.isAfter(endTimeObj)) return new Message("BID_FAIL", "Thời gian đã kết thúc.");
            if (bidTransaction.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) return new Message("BID_FAIL", "Giá đặt phải cao hơn giá hiện tại.");

            User currentUser = getGenericUser(bidTransaction.getBiddername());
            if (currentUser == null) return new Message("BID_FAIL", "Lỗi dữ liệu người dùng.");

            /// Tính số dư thực tế nếu tự Outbid chính mình
            BigDecimal effectiveBalance = currentUser.getBalance();
            if (bidTransaction.getBiddername().equals(auction.getHighestBidderName())) {
                effectiveBalance = effectiveBalance.add(auction.getCurrentPrice());
            }

            if (effectiveBalance.compareTo(bidTransaction.getBidAmount()) < 0) {
                return new Message("BID_FAIL", "Lỗi dữ liệu hoặc số dư không đủ.");
            }

            String oldBidderName = auction.getHighestBidderName();
            BigDecimal oldPrice = auction.getCurrentPrice();

            /// Hoàn tiền TRƯỚC, Trừ tiền SAU để tránh âm số dư ảo
            if (oldBidderName != null && !oldBidderName.trim().isEmpty()) {
                User oldUser = getGenericUser(oldBidderName);
                if (oldUser != null) updateGenericBalance(oldUser, oldPrice);
            }
            updateGenericBalance(currentUser, bidTransaction.getBidAmount().negate());

            auction.setCurrentPrice(bidTransaction.getBidAmount());
            auction.setHighestBidderName(bidTransaction.getBiddername());
            AuctionDAO.update(auction);

            String bidTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            BidTransactionDAO.insert(bidTransaction, bidTime);

            // Các điều kiện để gia hạn đấu giá
            if (endTimeObj != null) {
                int secondsBetween = (int) ChronoUnit.SECONDS.between(now, endTimeObj);
                if (secondsBetween <= 30 && secondsBetween > 0) {
                    AuctionSchedular.delay(auction, 1);
                    AuctionSchedular.delayFromAuction(auction.getId(), 1);
                }
            }

            com.google.gson.JsonObject responseObj = new com.google.gson.JsonObject();
            responseObj.addProperty("auctionId", auction.getId());
            responseObj.addProperty("newPrice", auction.getCurrentPrice());
            responseObj.addProperty("highestBidder", auction.getHighestBidderName());
            responseObj.addProperty("bidTime", bidTime);
            responseObj.addProperty("newEndTime", auction.getEndTime());


            /// BƯỚC 1: LƯU VÀ PHÁT THANH LƯỢT ĐÁNH CỦA NGƯỜI THẬT TRƯỚC
            ServerCore.broadcastMessage(new Message("BID_SUCCESS", responseObj.toString()));
            /// BƯỚC 2: BOT KIỂM TRA VÀ PHẢN HỒI
            BidTransaction currentBot = AutobidDAO.getAutoBidsByAuctionId(auction.getId());
            if (currentBot != null && !currentBot.getBiddername().equals(bidTransaction.getBiddername())) {
                if (currentBot.getBidAmount().compareTo(auction.getCurrentPrice()) > 0) {

                    // dừng 2 giây để Client kịp vẽ dữ liệu của người thật
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    BigDecimal botReactionPrice = auction.getCurrentPrice().add(currentBot.getStep());
                    if (botReactionPrice.compareTo(currentBot.getBidAmount()) > 0) botReactionPrice = currentBot.getBidAmount();
                    executeDirectBid(new BidTransaction(auction.getId(), currentBot.getBiddername(), botReactionPrice, BigDecimal.ZERO), auction);
                } else {
                    AutobidDAO.updateAutobid(auction.getId(), BigDecimal.ZERO, BigDecimal.ZERO, null);
                    System.out.println("[AUTO-BID] Người chơi " + bidTransaction.getBiddername() + " đã phá vỡ giới hạn Auto-bid!");
                }
            }

            return new Message("BID_FAKE_SUCCESS", "");

        } finally {
            // MỞ CỬA CHO LUỒNG TIẾP THEO BƯỚC VÀO
            roomLock.unlock();
        }
    }

    /// Hàm giả lập thao tác của Bot y như người thật .Thực thi một lệnh đặt giá tự động ngầm dành riêng cho hệ thống Bot.
    private static void executeDirectBid(BidTransaction botBid, Auction auction) {
        String oldBidderName = auction.getHighestBidderName();
        BigDecimal oldPrice = auction.getCurrentPrice();
        User botUser = getGenericUser(botBid.getBiddername());

        /// Hoàn tiền TRƯỚC, Trừ tiền SAU
        if (oldBidderName != null && !oldBidderName.trim().isEmpty()) {
            User oldUser = getGenericUser(oldBidderName);
            if (oldUser != null) updateGenericBalance(oldUser, oldPrice);
        }
        updateGenericBalance(botUser, botBid.getBidAmount().negate());

        auction.setCurrentPrice(botBid.getBidAmount());
        auction.setHighestBidderName(botBid.getBiddername());
        AuctionDAO.update(auction);

        /// Cố tình cộng thêm 1 giây để Bot luôn là người đến sau cùng và nằm trên đỉnh Database
        String bidTime = LocalDateTime.now().plusSeconds(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));        BidTransactionDAO.insert(botBid, bidTime);

        JsonObject responseObj = new JsonObject();
        responseObj.addProperty("auctionId", auction.getId());
        responseObj.addProperty("newPrice", auction.getCurrentPrice());
        responseObj.addProperty("highestBidder", auction.getHighestBidderName());
        responseObj.addProperty("bidTime", bidTime);
        responseObj.addProperty("newEndTime", auction.getEndTime());

        ServerCore.broadcastMessage(new Message("BID_SUCCESS", responseObj.toString()));
    }


    /// Hàm Tìm kiếm và trả về thông tin tài khoản người dùng một cách tổng quát dựa trên tên đăng nhập.
    private static User getGenericUser(String username) {
        User user = BidderDAO.selectByUsername(username);
        if (user == null) user = SellerDAO.selectByUsername(username);
        return user;
    }

    /// Hàm Cộng hoặc trừ số dư tài khoản của người dùng dựa trên vai trò (Role) của họ.
    private static void updateGenericBalance(User user, BigDecimal amount) {
        if (user == null) return;
        if ("SELLER".equalsIgnoreCase(user.getRole())) {
            SellerDAO.updateBalance(user.getId(), amount);
        } else {
            BidderDAO.updateBalance(user.getId(), amount);
        }
    }

    public static void setCurrentAuction(Auction auction) {currentAuction = auction;}
    public static Auction getCurrentAuction() {return currentAuction;}
    public static BigDecimal getAutoBidAmount() { return autoBidAmount; }
    public static void setAutoBidAmount(BigDecimal amount) { autoBidAmount = amount; }
    public static String getAutoBidderName() { return autoBidderName; }
    public static void setAutoBidderName(String name) { autoBidderName = name; }
    public static BigDecimal getAutoBidStep() { return autoBiddStep; }
    public static void setAutoBidStep(BigDecimal step) { autoBiddStep = step; }
}
