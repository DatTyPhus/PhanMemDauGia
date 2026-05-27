package com.auction.shared.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Entity {

    protected int id = 0;

    /// Cấu hình khuôn định dạng thời gian chuẩn xác khớp 100% với Database
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public int getId() {
        return id;
    }

    public void setId(int id1) {
        this.id = id1;
    }

    /// Đóng gói LocalDateTime thành chuỗi String để gửi qua mạng / lưu Database
    public String changeTimetoString(LocalDateTime time) {
        if (time == null) return "";
        return time.format(FORMATTER);
    }

    /// Dịch ngược chuỗi String từ Database thành LocalDateTime để chạy đồng hồ
    public LocalDateTime changeStringToTime(String time) {
        if (time == null || time.isEmpty()) return null;
        return LocalDateTime.parse(time, FORMATTER);
    }
}