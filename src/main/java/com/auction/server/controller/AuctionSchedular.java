package com.auction.server.controller;

import java.time.LocalDateTime;
import com.auction.shared.model.Auction;
import com.auction.server.dao.*;

public class AuctionSchedular {
  
  private static LocalDateTime timeline= null;
  private final AuctionDAO auctionDAO = new AuctionDAO();
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

  public void start (Auction auction) {
    auction.setStatus("OPEN");
    auctionDAO.update(auction);
  }

  public void end (Auction auction) {
    auction.setStatus("CLOSED");
    auctionDAO.update(auction);
  }
}
