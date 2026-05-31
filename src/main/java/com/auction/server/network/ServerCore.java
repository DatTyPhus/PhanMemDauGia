package com.auction.server.network;

import com.auction.shared.network.Message;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import com.auction.server.controller.*;
import com.auction.server.dao.*;
import java.util.concurrent.CopyOnWriteArrayList; // Cấu trúc danh sách an toàn cho Đa luồng
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerCore {

    private static final int PORT = 8080;

    /// Tạo 1 list lưu người dùng đang online.
    private static final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Trạm thu sóng Server đang mở tại cổng " + PORT + "...");
            AuctionSchedular.onServerStart(); // Gọi hàm này ngay khi Server khởi động để khởi tạo lại các đấu giá đang chờ

            ScheduledExecutorService heartbeatTimer = Executors.newSingleThreadScheduledExecutor();
            heartbeatTimer.scheduleAtFixedRate(() -> {
                try {
                    // Lấy thời gian chính xác tuyệt đối trên máy chủ và ép kiểu format
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String serverNow = java.time.LocalDateTime.now().format(dtf);

                    // Phát loa thời gian Server cho toàn bộ các máy Client đang kết nối
                    broadcastMessage(new com.auction.shared.network.Message("SERVER_TIME", serverNow));
                } catch (Exception e) {
                    System.out.println("[HEARTBEAT ERROR]: " + e.getMessage());
                }
            }, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
            /// =========================================================================

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Khách hàng mới vừa kết nối: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);

                activeClients.add(handler);                 // Ghi tên khách hàng vào sổ ngay khi họ kết nối

                broadcastMessage(new Message("UPDATE_ONLINE_COUNT", String.valueOf(activeClients.size()))); //số lượng người online mới nhất cho toàn bộ các Client đang mở màn hình Home

                Thread clientThread = new Thread(handler);
                clientThread.start();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Server: " + e.getMessage());
        }
    }

    // TỐI ƯU (REALTIME): Hàm phát thanh.
    // Bất cứ nơi nào trong Server gọi hàm này, toàn bộ màn hình Client sẽ nhận được tin nhắn!
    public static void broadcastMessage(Message msg) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(msg);
        }
    }

    /// Hàm lấy số lượng người đang online hiện tại để ClientHandler có thể gọi
    public static int getOnlineCount() {
        return activeClients.size();
    }

    // TỐI ƯU : Dọn dẹp danh sách người đấu giá khi có khách rời đi (Được gọi từ khối finally của ClientHandler)
    public static void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
        System.out.println("Đã xoá 1 client ngắt kết nối. Số người đang online: " + activeClients.size());

        broadcastMessage(new Message("UPDATE_ONLINE_COUNT", String.valueOf(activeClients.size()))); //số lượng khi có người vừa thoát app
    }
}

