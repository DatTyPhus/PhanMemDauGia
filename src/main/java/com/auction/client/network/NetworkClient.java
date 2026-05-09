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
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                NetworkClient serverSocket = NetworkClient.getInstance();   // Tạo luồng để đọc phản hồi từ server và ghi yêu cầu đến server.

                while (true){
                    Message msg = serverSocket.receive();                   // Mã hoá JSON thành đối tượng Message.
                    if(msg==null){
                        System.out.println("Mất kết nối với Server! Đóng radar.");
                        break;
                    }
                    switch (msg.getAction()){                              // So sánh các action để chuyển hướng đến khu vực xử lý
                        case "LOGIN_SUCCESS":
                            // Xử lý khi đăng nhập thành công.
                        case "LOGIN_FAIL":
                            // Xử lý khi đăng nhập thất bại.
                        case  "REGISTER_SUCCESS":
                            // Xử lý khi đăng ký thành công.
                        case "REGISTER_FAIL":
                            // Xử lý khi đăng ký thất bại.
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        listenerThread.setDaemon(true);                                // Đặt là Daemon để khi người dùng ấn nút [X] tắt app, luồng này cũng tự chết theo
        listenerThread.start();
    }

    static void main(String[] args) {
        try{
            NetworkClient serverSocket = NetworkClient.getInstance();
        } catch (IOException e) {
            System.out.println("Lỗi kết nối đến server.Vui lòng thử lại....");
        }
    }
}