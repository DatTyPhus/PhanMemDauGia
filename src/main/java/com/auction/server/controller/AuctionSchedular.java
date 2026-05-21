package com.auction.server.controller;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.auction.shared.model.Auction;
import com.auction.server.dao.*;

public class AuctionSchedular {
  
  private static LocalDateTime timeline= null;
  private final AuctionDAO auctionDAO = new AuctionDAO();
  private final AuctionService auctionService = new AuctionService();
  private final AuctionSchedular auctionSchedular = new AuctionSchedular();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
  public void updateTimeLine (Auction auction) {
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
    auctionDAO.updateDateTime(auction, startTimeStr, endTimeStr);
  }

  public void timer (Auction auction){

    int secondsUntilStart = (int) Duration.between(LocalDateTime.now(), auction.changeStringToTime(auction.getStartTime())).getSeconds();
    int secondsUntilEnd   = (int) Duration.between(LocalDateTime.now(), auction.changeStringToTime(auction.getEndTime())).getSeconds();

    scheduler.schedule(() -> end(auction), secondsUntilStart, TimeUnit.SECONDS);
    scheduler.schedule(() -> end(auction), secondsUntilEnd , TimeUnit.SECONDS);
  }

  public void start (Auction auction) {
    auction.setStatus("OPEN");
    auctionDAO.update(auction);
    auctionService.setCurrentAuction(auction);
    timer(auction);
  }

  public void end (Auction auction) {
    auction.setStatus("CLOSED");
    auctionDAO.update(auction);
    Auction nextAuction = auctionDAO.selectStartTime(auction.getEndTime());
    if (nextAuction != null) {
        start(nextAuction);
    }
  }
  public LocalDateTime getTimeline() {
    return timeline;
  }
  public void setTimeline(LocalDateTime timeline1) {
    timeline = timeline1;
  }
}
