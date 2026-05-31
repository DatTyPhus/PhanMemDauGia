package com.auction.server.controller;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.auction.shared.model.*;
import com.auction.server.dao.*;
import com.auction.shared.network.Message;

/// class AuctionSchedular: Xử lý hẹn giờ đóng/mở phiên đấu giá -
public class AuctionSchedular {

    // Tạo hàng chờ cho từng phiên đấu giá
    private static LocalDateTime timeline = null;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final Map<Integer, ScheduledFuture<?>> startTasks = new ConcurrentHashMap<>();
    private static final Map<Integer, ScheduledFuture<?>> endTasks   = new ConcurrentHashMap<>();


    /// Hàm này tính toán khoảng cách giữa start_time và end_time.
    public static void timer(Auction auction) {
        LocalDateTime startTime = auction.changeStringToTime(auction.getStartTime());
        LocalDateTime endTime = auction.changeStringToTime(auction.getEndTime());

        if (startTime == null || endTime == null) {
            System.err.println("[Scheduler Lỗi] Không thể hẹn giờ phiên ID " + auction.getId() + " do dữ liệu thời gian bị NULL!");
            return;
        }

        long secondsUntilStart = Duration.between(LocalDateTime.now(), startTime).getSeconds();
        long secondsUntilEnd   = Duration.between(LocalDateTime.now(), endTime).getSeconds();


        if (secondsUntilStart > 0) {
            /// Nếu còn thời gian chờ ở tương lai -> Hẹn giờ mở và gán trạng thái WAITING
            ScheduledFuture<?> startTask = scheduler.schedule(
                    () -> start(auction), secondsUntilStart, TimeUnit.SECONDS
            );
            startTasks.put(auction.getId(), startTask);

            auction.setStatus("WAITING");
            AuctionDAO.update(auction);
            ItemService.changeItemStatus(auction);
            System.out.println("[Scheduler] Đã lên lịch CHỜ (WAITING) cho auction " + auction.getId());
        } else {
            /// Nếu thời điểm bắt đầu đã đến hoặc ở trong quá khứ -> Kích hoạt OPEN ngay lập tức
            start(auction);
        }

        if (secondsUntilEnd > 0) {
            ScheduledFuture<?> endTask = scheduler.schedule(
                    () -> end(auction), secondsUntilEnd, TimeUnit.SECONDS
            );
            endTasks.put(auction.getId(), endTask);
        } else {
            end(auction);
        }
    }

    /// Hàm MỞ phiên đấu giá
    public static void start(Auction auction) {

        ///  KIỂM TRA CHÉO THỜI GIAN TRƯỚC KHI MỞ .Tránh tình trạng Đa luồng gọi nhầm hàm start cho một phiên đã quá hạn
        Auction latestAuction = AuctionDAO.selectById(auction.getId());
        if (latestAuction == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dbEndTime = latestAuction.changeStringToTime(latestAuction.getEndTime());

        /// Nếu thời gian thực tế đã vượt qua giờ đóng cửa -> Ép nó ĐÓNG ngay lập tức thay vì MỞ
        if (dbEndTime != null && (now.isAfter(dbEndTime) || now.isEqual(dbEndTime))) {
            System.err.println("[Scheduler Cảnh Báo] Phiên ID " + latestAuction.getId() + " đã quá hạn nhưng bị gọi start(). Chuyển hướng sang end()!");
            end(latestAuction);
            return;
        }

        latestAuction.setStatus("OPEN");
        AuctionDAO.update(latestAuction);
        ItemService.changeItemStatus(latestAuction);
        AuctionService.setCurrentAuction(latestAuction);
        System.out.println("[Scheduler] Đã MỞ (OPEN) phiên đấu giá ID: " + latestAuction.getId());
    }

    /// Hàm ĐÓNG phiên đấu giá
    public static void end(Auction auction) {

        /// KIỂM TRA CHÉO TRẠNG THÁI TRƯỚC KHI ĐÓNG .Tránh tình trạng 1 phiên bị đóng 2 lần gây lỗi vòng lặp gọi nextAuction
        Auction latestAuction = AuctionDAO.selectById(auction.getId());
        if (latestAuction == null) return;

        /// Nếu nó đã đóng rồi thì không làm gì cả
        if ("CLOSED".equals(latestAuction.getStatus())) {
            return;
        }

        latestAuction.setStatus("CLOSED");
        AuctionDAO.update(latestAuction);
        ItemService.changeItemStatus(latestAuction);

        Auction nextAuction = AuctionDAO.selectStartTime(latestAuction.getEndTime());
        AuctionService.setAutoBidAmount(BigDecimal.ZERO);
        AuctionService.setAutoBidStep(BigDecimal.ZERO);
        AuctionService.setAutoBidderName(null);
        System.out.println("[Scheduler] Đã ĐÓNG (CLOSED) phiên đấu giá ID: " + latestAuction.getId());

        /// Phát thanh thông báo phiên đã kết thúc cho toàn bộ Client biết đường cập nhật Lịch sử
        com.auction.server.network.ServerCore.broadcastMessage(new Message("AUCTION_ENDED", String.valueOf(latestAuction.getId())));

        if (nextAuction != null) {
            start(nextAuction);
        }
    }

    /// Làm mới lại lịch hẹn giờ của một phiên.
    public static void reschedule(Auction auction) {
        cancelTasks(auction.getId());
        timer(auction);
        System.out.println("[Scheduler] Đã reschedule auction " + auction.getId());
    }

    /// Xử lý việc gia hạn thời gian cho phiên đấu giá hiện tại.
    public static void delay(Auction auction, int delayMinutes) {
        ///  CHỈ CỘNG VÀO endTime. Tuyệt đối KHÔNG cộng vào startTime để tránh phiên đấu giá bị quay về trạng thái WAITING
        auction.setEndTime(auction.changeTimetoString(auction.changeStringToTime(auction.getEndTime()).plusMinutes(delayMinutes)));
        AuctionDAO.update(auction);
        reschedule(auction);
        System.out.println("[Scheduler] Đã gia hạn auction " + auction.getId() + " thêm " + delayMinutes + " phút");
    }

    /// Xử lý việc gia hạn thời gian cho phiên đấu giá hiện tại. ( hợp bạn gọi bằng ID)
    public static void delay(int auctionId, int delayMinutes) {
        Auction auction = AuctionDAO.selectById(auctionId);
        if(auction != null){
            auction.setEndTime(auction.changeTimetoString(auction.changeStringToTime(auction.getEndTime()).plusMinutes(delayMinutes)));
            AuctionDAO.update(auction);
            reschedule(auction);
            System.out.println("[Scheduler] Đã gia hạn auction " + auctionId + " thêm " + delayMinutes + " phút");
        }
    }

    /// Hàm Đẩy lùi lịch trình của các phiên đấu giá xếp hàng phía sau.
    public static void delayFromAuction(int auctionId, int delayMinutes) {
        List<Auction> affectedAuctions = AuctionDAO.findFromAuction(auctionId);
        for (Auction auction : affectedAuctions) {
            auction.setStartTime(auction.changeTimetoString(auction.changeStringToTime(auction.getStartTime()).plusMinutes(delayMinutes)));
            auction.setEndTime(auction.changeTimetoString(auction.changeStringToTime(auction.getEndTime()).plusMinutes(delayMinutes)));
            AuctionDAO.update(auction);
            reschedule(auction);
        }
    }

    /// Hàm Thu hồi lệnh hẹn giờ.
    private static void cancelTasks(int auctionId) {
        ScheduledFuture<?> startTask = startTasks.get(auctionId);
        ScheduledFuture<?> endTask   = endTasks.get(auctionId);
        if (startTask != null) startTask.cancel(false);
        if (endTask   != null) endTask.cancel(false);
        startTasks.remove(auctionId);
        endTasks.remove(auctionId);
    }

    /// Hàm Khôi phục trạng thái và trí nhớ của cỗ máy thời gian khi Server bị sự cố hoặc khởi động lại.
    public static void onServerStart() {
        List<Auction> waitingAuctions = AuctionDAO.selectByStatus("WAITING");
        List<Auction> openAuctions = AuctionDAO.selectByStatus("OPEN");

        List<Auction> allRunningAuctions = new ArrayList<>();
        allRunningAuctions.addAll(waitingAuctions);
        allRunningAuctions.addAll(openAuctions);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxEndTime = now;

        for (Auction auction : allRunningAuctions) {
            LocalDateTime startTime = auction.changeStringToTime(auction.getStartTime());
            LocalDateTime endTime   = auction.changeStringToTime(auction.getEndTime());

            if (startTime != null && endTime != null) {
                if (endTime.isAfter(maxEndTime)) {
                    maxEndTime = endTime;
                }

                if (startTime.isAfter(now)) {
                    timer(auction);
                } else if (endTime.isAfter(now)) {
                    BidTransaction lastBid = AutobidDAO.getAutoBidsByAuctionId(auction.getId());
                    if (lastBid != null) {
                        AuctionService.setAutoBidAmount(lastBid.getBidAmount() != null ? lastBid.getBidAmount() : BigDecimal.ZERO);
                        AuctionService.setAutoBidderName(lastBid.getBiddername());
                        AuctionService.setAutoBidStep(lastBid.getStep() != null ? lastBid.getStep() : BigDecimal.ZERO);
                    }
                    start(auction);
                    long secondsUntilEnd = Duration.between(now, endTime).getSeconds();
                    ScheduledFuture<?> endTask = scheduler.schedule(
                            () -> end(auction), secondsUntilEnd, TimeUnit.SECONDS
                    );
                    endTasks.put(auction.getId(), endTask);
                } else {
                    end(auction);
                }
            }
        }
        setTimeline(maxEndTime);
    }

    public static LocalDateTime getTimeline() { return timeline; }
    public static void setTimeline(LocalDateTime timeline1) { timeline = timeline1; }
}