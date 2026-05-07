package com.auction.server.network;

import com.auction.shared.network.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Class dùng để đọc file JSON trên client gửi xuống , vừa kiểm tra nhãn dán rồi ném đến controller.

public class ClientHandler implements Runnable {
    private Socket socket;

    // Đưa Socket của Client vào qua Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Bước 4 (Phía Server): Thiết lập ống Đọc dữ liệu (InputStream)
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String jsonReceived;

            while ((jsonReceived = in.readLine()) != null) {
                // Dùng Gson bóc hộp ngay lập tức
                Message msg = Message.fromJson(jsonReceived);

                // Điều phối công việc dựa trên nhãn dán "action"
                switch (msg.getAction()) {
                    case "LOGIN":
                        System.out.println("Nhận lệnh LOGIN với dữ liệu: " + msg.getPayload());
                        break;
                    case "BID":
                        // Xử lý đặt giá...
                        break;
                    default:
                        System.out.println("Không hiểu lệnh này!");
                    }
                }
        }
        catch(IOException e){
            System.out.println("Client " + socket.getInetAddress() + " đã ngắt kết nối đột ngột.");
        }
    }
}
