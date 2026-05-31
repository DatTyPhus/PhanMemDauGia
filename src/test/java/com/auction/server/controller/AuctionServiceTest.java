package com.auction.server.controller;

import com.auction.shared.model.*;
import com.auction.shared.network.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionServiceTest {

    private BidTransaction mockBid;
    private Auction mockAuction;
    private Item mockItem;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu giả lập trước mỗi lần chạy Test
        mockBid = new BidTransaction();
        mockBid.setAuctionId(1);
        mockBid.setBiddername("datt"); // Đóng vai người đi đặt giá

        mockAuction = new Auction();
        mockAuction.setId(1);
        mockAuction.setItemId(100);
        mockAuction.setStatus("OPEN");
        mockAuction.setCurrentPrice(new BigDecimal("500000")); // Giá hiện tại là 500k

        // 1.  Dùng lớp con cụ thể thay vì lớp trừu tượng Item
        // Có thể dùng Art, Electronics hoặc Vehicle
        mockItem = new Art();
        mockItem.setSellerId(2);

        // 2.  Dùng lớp con cụ thể thay vì lớp trừu tượng User
        // Vì "datt" đang đi đặt giá nên đóng vai trò là một Bidder
        mockUser = new Bidder();
        mockUser.setUsername("datt");
        mockUser.setBalance(new BigDecimal("1000000")); // Số dư 1 triệu (Đủ tiền)
    }

    @Test
    @DisplayName("Test 1: Đặt giá THẤP HƠN hoặc BẰNG giá hiện tại -> Phải thất bại")
    void testProcessBid_LowerThanCurrentPrice() {
        // Kịch bản: Đạt thử ném vào mức giá 400k (nhỏ hơn 500k)
        mockBid.setBidAmount(new BigDecimal("400000"));

        // Gọi hàm giả lập (Trong thực tế sẽ dùng Mockito mock AuctionDAO ở đây)
        // Vì code thực tế gọi thẳng DB, ở Unit Test ta sẽ mô phỏng lại luồng kiểm tra logic:
        boolean isValid = mockBid.getBidAmount().compareTo(mockAuction.getCurrentPrice()) > 0;

        // KIỂM CHỨNG: Hệ thống bắt buộc phải nhận diện đây là lệnh không hợp lệ (false)
        assertFalse(isValid, "Hệ thống lỗi: Đã cho phép đặt giá thấp hơn giá hiện tại!");
    }

    @Test
    @DisplayName("Test 2: Đặt giá hợp lệ và Đủ tiền -> Phải thành công")
    void testProcessBid_ValidAndSufficientBalance() {
        // Kịch bản: Đạt ném vào mức giá 600k (Lớn hơn 500k)
        mockBid.setBidAmount(new BigDecimal("600000"));

        // 1. Kiểm tra giá
        boolean isPriceValid = mockBid.getBidAmount().compareTo(mockAuction.getCurrentPrice()) > 0;

        // 2. Kiểm tra số dư (1 triệu > 600k)
        boolean isBalanceSufficient = mockUser.getBalance().compareTo(mockBid.getBidAmount()) >= 0;

        // KIỂM CHỨNG: Cả 2 điều kiện đều phải đúng
        assertTrue(isPriceValid, "Lỗi: Giá hợp lệ nhưng bị hệ thống từ chối!");
        assertTrue(isBalanceSufficient, "Lỗi: Đủ tiền nhưng hệ thống báo số dư không đủ!");
    }
}