package com.auction.server.controller;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.auction.shared.model.Auction;
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
    AuctionDAO.updateDateTime(auction, startTimeStr, endTimeStr);
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

        startTasks.put(auction.getId(), startTask);
        endTasks.put(auction.getId(), endTask);

        System.out.println("[Scheduler] Đã lên lịch auction " + auction.getId());
        System.out.println("  Mở lúc : " + auction.getStartTime());
        System.out.println("  Đóng lúc: " + auction.getEndTime());
    }

  public static void start (Auction auction) {
    auction.setStatus("OPEN");
    AuctionDAO.update(auction);
    AuctionService.setCurrentAuction(auction);
    timer(auction);
  }

  public static void end (Auction auction) {
    auction.setStatus("CLOSED");
    AuctionDAO.update(auction);
    Auction nextAuction = AuctionDAO.selectStartTime(auction.getEndTime());
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
  
  public static LocalDateTime getTimeline() {
    return timeline;
  }
  public static void setTimeline(LocalDateTime timeline1) {
    timeline = timeline1;
  }
}
