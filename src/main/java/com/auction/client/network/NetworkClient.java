package com.auction.client.network;

import com.auction.shared.network.Message;
import java.io.*;
import java.net.Socket;

public class NetworkClient {

    public interface MessageListener {
        void onMessageReceived(Message msg);
    }

    private static NetworkClient instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageListener currentListener;

    private NetworkClient() throws IOException {
        this.socket = new Socket("localhost", 8080);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public static NetworkClient getInstance() throws IOException {
        if(instance == null){
            instance = new NetworkClient();
        }
        return instance;
    }

    public void setListener(MessageListener listener) {
        this.currentListener = listener;
    }

    public void send(Message msg) {
        out.println(msg.toJson());
    }

    public Message receive() throws IOException {
        String msg = in.readLine();
        return Message.fromJson(msg);
    }

    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    Message msg = this.receive();
                    if (msg == null) {
                        System.out.println("Mất kết nối với Server!");
                        break;
                    }

                    if (currentListener != null) {
                        currentListener.onMessageReceived(msg);
                    } else {
                        switch (msg.getAction()){                            // Thử khi
                            case "LOGIN_SUCCESS":
                                System.out.println("Đăng nhập thành công");
                                break;
                            case "LOGIN_FAIL":
                                System.out.println("Đăng nhập thất bại");
                                break;
                            case  "REGISTER_SUCCESS":
                                System.out.println("Đăng ký thành công");
                                break;
                            case "REGISTER_FAIL":
                                System.out.println("Đăng ký thất bại");
                                break;
                        }
                    }
                } // Đã fix lỗi thiếu dấu ngoặc nhọn đóng while ở đây
            } catch (IOException e) {
                System.out.println("Đứt kết nối mạng: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}