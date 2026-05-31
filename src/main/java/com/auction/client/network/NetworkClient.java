package com.auction.client.network;

import com.auction.shared.network.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    // Dùng một Danh sách để nhiều màn hình cùng nghe được 1 lúc.
    private final List<MessageListener> listeners = new ArrayList<>();       /// Danh sách các đối tượng nghe dữ liệu được truyền từ server lên.

    private NetworkClient() throws IOException {
        this.socket = new Socket("192.168.2.45", 8080);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));      //Tạo ống nghe xuống server qua cổng 8080.
        this.out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);

        this.startListening();
    }

    public static NetworkClient getInstance() throws IOException {
        if(instance == null){
            instance = new NetworkClient();
        }
        return instance;
    }

    // --- CÁC HÀM QUẢN LÝ MÀN HÌNH ĐANG NGHE (OBSERVER PATTERN) ---


    public void addListener(MessageListener listener) {      // Hàm thêm một màn hình vào danh sách lắng nghe (Đăng ký nghe)
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(MessageListener listener) {   // Hàm xóa một màn hình khỏi danh sách (Khi chuyển sang trang khác thì hủy nghe)
        listeners.remove(listener);
    }

    // --- CÁC HÀM GIAO TIẾP VỚI SERVER ---

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

                    //Dùng vòng lặp báo tin cho TẤT CẢ các đối tượng đang nằm trong danh sách nghe
                    if (!listeners.isEmpty()) {
                        for (MessageListener listener : listeners) {
                            listener.onMessageReceived(msg);     // Khi tín hiệu chứa thông tin và yêu cầu ... thì sẽ xử lý đối tượng MessageListener sẽ đọc dữ liệu để xử lý các bước tiếp theo.
                        }
                    } else {
                        // Dự phòng khi ứng dụng vừa bật, chưa có màn hình nào đăng ký
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
                            default:
                                System.out.println("Nhận tín hiệu ngầm: " + msg.getAction());
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