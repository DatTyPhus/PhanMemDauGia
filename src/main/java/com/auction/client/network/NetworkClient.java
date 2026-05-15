package com.auction.client.network;

import com.auction.shared.network.Message;
import java.io.*;
import java.net.Socket;

/// Class NetworkClient chứa interface MessageListener trong phần nội dung , vì chức năng của 2 cái này mật thiết với nhau.
/// Đối tượng duy nhất của NetworkClient dùng để mở một đường ống cắm vào cổng của server từ đó có thể gửi đi các yêu cầu và nhận lại các phản hồi từ server.
/// Đối tượng của MessageListener có chức năng như người nghe từ đường ống ,lấy các dữ liệu đã được mã hoá và thực hiện chuyển hướng xử lý theo các hàm khác được Override

public class NetworkClient {

    public interface MessageListener {                  /// Hàm định nghĩa đối tượng nghe và lấy dữ liệu từ server qua hàm onMessageReceived.
        void onMessageReceived(Message msg);            /// hàm này để các class xử lý phản hồi của server Override lại để xử lý chính xác các phản hồi để được phân loại.(Action)
    }

    private static NetworkClient instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageListener currentListener;       /// Đối tượng nghe dữ liệu được truyền từ server lên.

    private NetworkClient() throws IOException {
        this.socket = new Socket("localhost", 8080);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));      //Tạo ống nghe xuống server qua cổng 8080.
        this.out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);   //Tạo ống in ra dữ liệu để truyền xuôn server.

        this.startListening(); //Gọi hàm để nghe từ server
    }

    ///Chỉ có 1 đối tượng (đường ống) để nghe hay truyền dữ liệu xuống server hoặc nhận phản hồi.
    public static NetworkClient getInstance() throws IOException {
        if(instance == null){
            instance = new NetworkClient();
        }
        return instance;
    }

    public void setListener(MessageListener listener) {          /// Thiết lập đối tượng nghe được truyền từ ngoài vào.
        this.currentListener = listener;
    }

    public void send(Message msg) {                  //method này dùng để gửi yêu cầu từ client xuống server

        out.println(msg.toJson());
    }

    public Message receive() throws IOException {      // Bắt được tín hiệu của server gửi về,mã hoá từ JSON sang đối tượng Message(action,payload).
        String msg = in.readLine();
        return Message.fromJson(msg);
    }

    public void startListening() {                            ///  Mở luồng bắt đầu nghe phản hồi từ server.
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {                           //Vòng lặp vô tận -> đảm bảo luôn bắt được tín hiệu từ server.
                    Message msg = this.receive();
                    if (msg == null) {
                        System.out.println("Mất kết nối với Server!");   //Khi nhận được tín hiệu nhưng không chứa thông tin hay yêu cầu gì từ server thì in lên dòng chữ ....
                        break;
                    }

                    if (currentListener != null) {
                        currentListener.onMessageReceived(msg);     // Khi tín hiệu chứa thông tin và yêu cầu ... thì sẽ xử lý đối tượng MessageListener sẽ đọc dữ liệu để xử lý các bước tiếp theo.
                    } else {
                        switch (msg.getAction()){
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
                }
            } catch (IOException e) {
                System.out.println("Đứt kết nối mạng: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);           /// Khi người dùng thoát khỏi app thì tự động ngắt luôn luồng nghe
        listenerThread.start();
    }
}