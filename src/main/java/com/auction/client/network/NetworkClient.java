package com.auction.client.network;

import com.auction.shared.network.Message;
import java.io.*;
import java.net.Socket;

//

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Khóa Constructor lại, tự động kết nối khi khởi tạo
    private NetworkClient() throws IOException {
        socket = new Socket("localhost", 8080);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Cấp phát đường ống dùng chung
    public static NetworkClient getInstance() throws IOException {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    // Hàm gửi hàng
    public void send(Message msg) {
        out.println(msg.toJson());
    }

    // Hàm nhận hàng
    public Message receive() throws IOException {
        String response = in.readLine();
        return Message.fromJson(response);
    }
}