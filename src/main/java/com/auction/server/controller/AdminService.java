package com.auction.server.controller;

import com.auction.server.dao.*;
import com.auction.shared.model.Auction; // Giả sử model Auction nằm ở shared
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.auction.shared.network.Message;

public class AdminService {
    // Singleton Instance
    private static AdminService instance;
    
    // Sử dụng ConcurrentHashMap để xử lý đấu giá đồng thời an toàn
    private Map<Integer, Auction> runningAuctions;

    private AdminService() {
        runningAuctions = new ConcurrentHashMap<>();
    }

    public static synchronized AdminService getInstance() {
        if (instance == null) {
            instance = new AdminService();
        }
        return instance;
    }

    // Thêm một phiên đấu giá mới vào hệ thống
    public void addAuction(Auction auction) {
        runningAuctions.put(auction.getId(), auction);
        AuctionService.waitingAuctions.remove(auction.getId()); // Loại bỏ khỏi danh sách chờ nếu đã tồn tại
        
    }
    
    // Kết thúc một phiên đấu giá và loại bỏ nó khỏi hệ thống
    public Message deleteAuction(int id) {  
        runningAuctions.remove(id);
        Auction auction = AuctionDAO.selectById(id); // Cập nhật trạng thái đấu giá trong database nếu cần
        return new Message ("DELETE_AUCTION_SUCCESS", auction);
    }

    // Lấy thông tin phiên đấu giá theo ID
    public Auction getAuction(int id) {
        return runningAuctions.get(id);
    }
}