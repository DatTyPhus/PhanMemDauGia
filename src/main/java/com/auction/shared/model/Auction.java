package com.auction.shared.model;

public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private double currentPrice;
    private Bidder highestBidder;
    private boolean isOpen;

    public Auction(Item item, Seller seller) {
        super();
        this.item = item;
        this.seller = seller;
        this.currentPrice = item.getStartingPrice();
        this.isOpen = true;
    }

    public synchronized void placeBid(Bidder bidder, double amount) {
        if (!isOpen) {
            System.out.println("Auction closed!");
            return;
        }

        if (amount <= currentPrice) {
            System.out.println("Bid must be higher than current price!");
            return;
        }

        currentPrice = amount;
        highestBidder = bidder;

        System.out.println(bidder.getUsername() + " bid: " + amount);
    }

    public void close() {
        isOpen = false;
        System.out.println("Winner: " +
            (highestBidder != null ? highestBidder.getUsername() : "None"));
    }

    public void cancel() {
        isOpen = false;
        System.out.println("Auction canceled");
    }
}