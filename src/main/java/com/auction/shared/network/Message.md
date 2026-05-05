# Chức năng:
## + Dùng để chuỗi hoá từ Object ra file JSON gồm : 
    - action (String): Các nhãn yêu cầu để đưa về khu vực riêng để xử lý yêu cầu đó (như LOGIN,UPDATEBID,BID...).

    - payload (Object) : Đối tượng chứa  bất kì dữ liệu gì (User,Product,Seller...)

    - Khi xác định được yêu cầu thì sẽ chuyển Object về khu vực xử lý , và thực hiện các tác vụ từ những dữ liệu của Object đưa về.