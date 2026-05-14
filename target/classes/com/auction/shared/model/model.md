# Thực thể Hướng đối tượng:
# + Nơi định nghĩa hình dáng của các vật thể trong hệ thống bằng các class Java (Ví dụ: class User, class Item, class Bid).
# + Chức năng & Vận hành: Khi Client muốn gửi thông tin đấu giá, nó không gửi đoạn text rời rạc. Nó sẽ tạo một đối tượng Bid(userId, itemId, price) được định nghĩa trong shared/model, biến nó thành chuỗi byte/JSON rồi gửi đi. Đầu bên kia, Server nhận được và cũng dùng chính class Bid trong shared/model để giải mã ra. Nếu không có thư mục dùng chung này, hai bên sẽ không thể dịch được dữ liệu của nhau.
