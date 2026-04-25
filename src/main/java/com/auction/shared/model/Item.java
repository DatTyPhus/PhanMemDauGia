package com.auction.shared.model;

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startingPrice;

    public Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getName() {
        return name;
    }
}
