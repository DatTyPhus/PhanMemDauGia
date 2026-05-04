package com.auction.server.network;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerCore {

    // Khai báo hằng số cho cổng kết nối
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // Bước 1: Mở cổng trạm thu sóng bằng ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Trạm thu sóng Server đang mở tại cổng " + PORT + "...");

            // Bước 2: Vòng lặp vô tận để liên tục đón khách.
            while (true) {

                // Lệnh accept() sẽ dừng luồng hiện tại và chờ đợi.
                // Khi có 1 Client kết nối, nó trả về một đối tượng Socket đại diện cho Client đó.
                Socket clientSocket = serverSocket.accept();
                System.out.println("Khách hàng mới vừa kết nối từ IP: " + clientSocket.getInetAddress());

                // NGAY LẬP TỨC: Tạo một luồng (Thread) mới để xử lý vị khách này.
                // Việc này giúp vòng lặp while lập tức quay lại lệnh accept() để đón người tiếp theo.
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Server: " + e.getMessage());
        }
    }
}

