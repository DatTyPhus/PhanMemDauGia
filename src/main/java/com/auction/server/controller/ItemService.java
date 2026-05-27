package com.auction.server.controller;


import com.auction.shared.model.*;
import com.auction.server.dao.*;

public class ItemService {
  public static void changeItemStatus (Auction auction) {
    Item item = ItemDAO.selectById(auction.getItemId());
    item.setStatus(auction.getStatus());
    ItemDAO.update(item);
  }
}
