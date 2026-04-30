package com.auction.shared.model;


public abstract class Entity {

    protected static Integer id=0;
    public synchronized Entity() {
        id= id +1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id1) {
        id = id1;
    }
}