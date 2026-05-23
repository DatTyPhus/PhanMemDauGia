package com.auction.server.controller;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.auction.shared.model.*;
import com.auction.server.dao.*;

public class AuctionSchedular {
  
  private static LocalDateTime timeline= null;
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
  private static final Map<Integer, ScheduledFuture<?>> startTasks = new ConcurrentHashMap<>();
  private static final Map<Integer, ScheduledFuture<?>> endTasks   = new ConcurrentHashMap<>();


  public static void updateTimeLine (Auction auction) {
    LocalDateTime endTime = null;
    LocalDateTime startTime =null;
    if (timeline == null || LocalDateTime.now().isAfter(timeline)) {
        timeline = LocalDateTime.now().plusMinutes(auction.getDurationMinutes());
    }
    else{
        startTime = timeline;
        endTime = timeline.plusMinutes(auction.getDurationMinutes());
    } 
    String startTimeStr = auction.changeTimetoString(startTime);
    String endTimeStr = auction.changeTimetoString(endTime);
    auction.setStartTime(startTimeStr);
    auction.setEndTime(endTimeStr);
    AuctionDAO.update(auction);
  }
  

  

   public static void timer(Auction auction) {
        long secondsUntilStart = Duration.between(LocalDateTime.now(),auction.changeStringToTime(auction.getStartTime())).getSeconds();
        long secondsUntilEnd   = Duration.between(LocalDateTime.now(), auction.changeStringToTime(auction.getEndTime())).getSeconds();

        ScheduledFuture<?> startTask = scheduler.schedule(
            () -> start(auction), secondsUntilStart, TimeUnit.SECONDS
        );
        ScheduledFuture<?> endTask = scheduler.schedule(
            () -> end(auction), secondsUntilEnd, TimeUnit.SECONDS
        );
        
        auction.setStatus("WAITING");
        AuctionDAO.update(auction);

        ItemService.changeItemStatus(auction); // Cập nhật trạng thái của Item tương ứng khi tạo đấu giá

        startTasks.put(auction.getId(), startTask);
        endTasks.put(auction.getId(), endTask);

        System.out.println("[Scheduler] Đã lên lịch auction " + auction.getId());
        System.out.println("  Mở lúc : " + auction.getStartTime());
        System.out.println("  Đóng lúc: " + auction.getEndTime());
    }

  public static void start (Auction auction) {
    auction.setStatus("OPEN");
    AuctionDAO.update(auction);
    ItemService.changeItemStatus(auction); // Cập nhật trạng thái của Item tương ứng khi bắt đầu đấu giá
    AuctionService.setCurrentAuction(auction);// Cập nhật phiên đấu giá hiện tại trong AuctionService *11111
    timer(auction);
  }

  public static void end (Auction auction) {
    auction.setStatus("CLOSED");
    AuctionDAO.update(auction);
    ItemService.changeItemStatus(auction); // Cập nhật trạng thái của Item tương ứng khi kết thúc đấu giá
    Auction nextAuction = AuctionDAO.selectStartTime(auction.getEndTime());
    AuctionService.setAutoBidAmount(BigDecimal.ZERO);
    AuctionService.setAutoBidStep(BigDecimal.ZERO);
    AuctionService.setAutoBidderName(null);
    if (nextAuction != null) {
        start(nextAuction);
    }
  }

  public static void reschedule(Auction auction) {
        cancelTasks(auction.getId());
        timer(auction);
        System.out.println("[Scheduler] Đã reschedule auction " + auction.getId());
  }

  public static void delayFromAuction(int auctionId, int delayMinutes) {
        List<Auction> affectedAuctions = AuctionDAO.findFromAuction(auctionId);
        for (Auction auction : affectedAuctions) {
            auction.setStartTime(auction.changeTimetoString(auction.changeStringToTime(auction.getStartTime()).plusMinutes(delayMinutes)));
            auction.setEndTime(auction.changeTimetoString(auction.changeStringToTime(auction.getEndTime()).plusMinutes(delayMinutes)));
            AuctionDAO.update(auction);
            reschedule(auction);
            System.out.println("[Scheduler] Đã dời auction " + auction.getId()
                + " → mở " + auction.getStartTime()
                + " đóng " + auction.getEndTime());
        }
    }
  
  public static void delay(int auctionId, int delayMinutes) {
        Auction auction = AuctionDAO.selectById(auctionId);
        auction.setStartTime(auction.changeTimetoString(auction.changeStringToTime(auction.getStartTime()).plusMinutes(delayMinutes)));
        auction.setEndTime(auction.changeTimetoString(auction.changeStringToTime(auction.getEndTime()).plusMinutes(delayMinutes)));
        AuctionDAO.update(auction);
        reschedule(auction);
        System.out.println("[Scheduler] Đã dời auction " + auctionId + " thêm " + delayMinutes + " phút");
    }

  private static void cancelTasks(int auctionId) {
        ScheduledFuture<?> startTask = startTasks.get(auctionId);
        ScheduledFuture<?> endTask   = endTasks.get(auctionId);
        if (startTask != null) startTask.cancel(false);
        if (endTask   != null) endTask.cancel(false);
        startTasks.remove(auctionId);
        endTasks.remove(auctionId);
    }
  
    public static void onServerStart() {
    List<Auction> auctions = AuctionDAO.selectByStatus("WAITING");
    LocalDateTime now = LocalDateTime.now();
    for (Auction auction : auctions) {
        LocalDateTime startTime = auction.changeStringToTime(auction.getStartTime());
        LocalDateTime endTime   = auction.changeStringToTime(auction.getEndTime());

        if (startTime.isAfter(now)) {
            // timer() đã tự lưu vào startTasks và endTasks bên trong
            timer(auction);

        } else if (endTime.isAfter(now)) {
            BidTransaction lastBid = AutobidDAO.getAutoBidsByAuctionId(auction.getId());
            AuctionService.setAutoBidAmount(lastBid.getBidAmount()); // Đặt lại giá tự động dựa trên lượt đặt cuối cùng (nếu có)
            AuctionService.setAutoBidderName(lastBid.getBiddername());
            AuctionService.setAutoBidStep(lastBid.getStep());
            start(auction);
            long secondsUntilEnd = Duration.between(now, endTime).getSeconds();

            // Lưu vào endTasks để có thể delay / cancel sau này
            ScheduledFuture<?> endTask = scheduler.schedule(
                () -> end(auction), secondsUntilEnd, TimeUnit.SECONDS
            );
            endTasks.put(auction.getId(), endTask); // ← bắt buộc

        } else {
            end(auction);
        }
    }
}

  public static LocalDateTime getTimeline() {return timeline;}
  public static void setTimeline(LocalDateTime timeline1) {timeline = timeline1;}
}
