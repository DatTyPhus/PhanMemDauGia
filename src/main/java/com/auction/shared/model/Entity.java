package com.auction.shared.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Entity {

    protected int id=0;
    public int getId() {
        return id;
    }
    public String changeTimetoString( LocalDateTime time) {
        DateTimeFormatter khuon = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String newTime = time.format(khuon);
        return newTime;
    }

    public LocalDateTime changeStringToTime(String time) {
        DateTimeFormatter khuon = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime newTime = LocalDateTime.parse(time, khuon);
        return newTime;
    }


    public void setId(int id1) {
        this.id = id1;
    }
}