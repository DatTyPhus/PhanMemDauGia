package com.auction.shared.model;


public abstract class Entity {

    protected static int id=0;
    public Entity() {
        id= id +1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id1) {
        id = id1;
    }
}