package com.auction.client.network;

import com.auction.shared.network.Message;
import java.io.*;
import java.net.Socket;

// Class kết nối tới Server.

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private BufferedReader in;
    private  PrintWriter out;

    // Khởi tạo (dùng Singleton)
    private NetworkClient() throws IOException{
        this.socket = new Socket("localhost",8080);                        // Kết nối đến server.
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Tạo luồng để đọc dữ liệu từ server.
        this.out = new PrintWriter(socket.getOutputStream(),true);            // Tạo luồng để ghi dữ liệu và gửi tới server.
    }

    // Dùng chung 1 luồng khi gửi yêu cầu xuống server.
    public static NetworkClient getInstance() throws IOException{
        if(instance == null){
            instance = new NetworkClient();
        }
        return instance;
    }

    //Gửi đi đến server.
    public void send(Message msg){
        out.println(msg.toJson());
    }

    //Đọc dữ liệu từ server.
    public Message receive() throws IOException{
        String msg = in.readLine();
        return Message.fromJson(msg);
    }
}