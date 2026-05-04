package com.auction.client.network;

import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    public static void main(String[] args) {
        // Bước 3: Gọi điện từ máy khách
        // Địa chỉ IP là localhost (chạy trên cùng 1 máy), cổng 8080 phải khớp với Server
        try (Socket socket = new Socket("localhost", 8080)) {
            System.out.println("Đã kết nối thành công tới tổng đài Server!");

            // Bước 4 (Phía Client): Thiết lập ống Ghi dữ liệu (OutputStream)
            // Lớp PrintWriter giúp đẩy text dạng String qua mạng dễ dàng hơn.
            // Tham số 'true' (auto-flush) đảm bảo dữ liệu được đẩy đi ngay lập tức không bị kẹt trong ống.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Đây là chuỗi JSON chuẩn hóa nhóm thống nhất.
            String jsonLogin = "{\"action\": \"LOGIN\", \"payload\": {\"username\": \"NguyenTienDat\", \"password\": \"Dat@1008\"}}";

            // Ném bưu kiện vào đường ống mạng
            System.out.println("Đang gửi thông tin đăng nhập lên Server...");
            out.println(jsonLogin);

            // Tạm dừng một chút để xem kết quả trước khi đóng app nghiệm thu
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("Không thể kết nối. Hãy kiểm tra xem ServerCore đã được bật chưa!");
        }
    }
}