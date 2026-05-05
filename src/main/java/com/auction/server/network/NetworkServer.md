# Chức năng:
## +ServerCore:
    - Mở luồng mạng để nhận tín hiệu từ client.
    - Mỗi một user khi yêu cầu sẽ tạo 1 luồng riêng để xử lý cho nhiều user cùng lúc (Đa luồng).
## +ClientHandler:
    - Xác định được yêu cầu thì sẽ chuyển Object về khu vực xử lý , và thực hiện các tác vụ từ những dữ liệu của Object đưa về.