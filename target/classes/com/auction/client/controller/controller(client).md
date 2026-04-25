# Bộ điều khiển giao diện:
#   + Bộ não của Client, đứng ngay phía sau View.
#   + Bắt các sự kiện click chuột từ View, kiểm tra lỗi nhập liệu cơ bản (ví dụ: cấm nhập chữ vào ô tiền). Sau đó, nó đóng gói dữ liệu và gửi qua mạng (Socket/API) đến Server.
#   + Nhận tiếng hét từ View $\rightarrow$ Lấy số "100.000đ" $\rightarrow$ Đóng gói lại thành một bưu kiện $\rightarrow$ Ném bưu kiện đó qua đường truyền mạng tới Server.
