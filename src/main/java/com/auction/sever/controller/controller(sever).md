# Bộ điều phối & Logic lõi:
# + Trạm kiểm soát không lưu của hệ thống.
# + Nhận "bưu kiện" từ hàng chục Client gửi tới cùng lúc. Đây là nơi chứa thuật toán đa luồng phức tạp nhất: kiểm tra xem giá đặt có hợp lệ không, phiên đấu giá còn giờ không, xử lý tranh chấp khi 2 người đặt giá cùng 1 mili-giây.
# + Mở bưu kiện từ Client $\rightarrow$ Chạy thuật toán kiểm tra $\rightarrow$ Nếu hợp lệ, nó ra lệnh cho dao lưu xuống Database $\rightarrow$ Phản hồi kết quả (Thành công/Thất bại) về lại cho Client.