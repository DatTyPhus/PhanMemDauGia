package com.auction.shared.model;


public abstract class Entity {

    protected int id=0;
    public int getId() {
        return id;
    }

    public void setId(int id1) {
        this.id = id1;
    }
}