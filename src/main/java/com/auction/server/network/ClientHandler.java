package com.auction.server.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

// Implements Runnable để biến class này thành một tác vụ có thể chạy song song
public class ClientHandler implements Runnable {
    private Socket socket;

    // Đưa Socket của Client vào qua Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Bước 4 (Phía Server): Thiết lập ống Đọc dữ liệu (InputStream)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonGoiLen;
            // Vòng lặp liên tục đọc dữ liệu trong ống.
            // readLine() sẽ chờ cho đến khi Client ấn "gửi" một dòng text.
            while ((jsonGoiLen = in.readLine()) != null) {
                System.out.println("Hộp thư Server nhận được: " + jsonGoiLen);

                // (Sau này, chúng ta sẽ bóc tách chuỗi JSON ở đây để gọi Logic Đăng nhập / Đặt giá)
            }

        } catch (Exception e) {
            System.out.println("Client " + socket.getInetAddress() + " đã ngắt kết nối đột ngột.");
        }
    }


    
}
