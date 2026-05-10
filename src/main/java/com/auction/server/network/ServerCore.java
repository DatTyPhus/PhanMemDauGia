package com.auction.server.network;

import com.auction.shared.network.Message;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // Cấu trúc danh sách an toàn cho Đa luồng

public class ServerCore {

    private static final int PORT = 8080;

    /// Tạo 1 list lưu người dùng đang online.
    private static final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Trạm thu sóng Server đang mở tại cổng " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Khách hàng mới vừa kết nối: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);

                activeClients.add(handler);                 // Ghi tên khách hàng vào sổ ngay khi họ kết nối

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

    // TỐI ƯU : Dọn dẹp danh sách người đấu giá khi có khách rời đi (Được gọi từ khối finally của ClientHandler)
    public static void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
        System.out.println("Đã xoá 1 client ngắt kết nối. Số người đang online: " + activeClients.size());
    }
}

