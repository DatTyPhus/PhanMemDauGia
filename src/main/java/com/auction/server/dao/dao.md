# Data Access Object (dao) - Giao tiếp Dữ liệu:
# + Cánh tay robot duy nhất được phép thò xuống Cơ sở dữ liệu (Database).
# + Chứa các câu lệnh SQL (SELECT, INSERT, UPDATE). Tuyệt đối không chứa logic tính toán thắng thua ở đây, chỉ làm nhiệm vụ "đọc" và "ghi" thuần túy.
# + Vận hành: Nhận lệnh từ Server Controller $\rightarrow$ Cập nhật giá 100.000đ vào bảng MySQL $\rightarrow$ Báo cáo lại là đã lưu xong.

