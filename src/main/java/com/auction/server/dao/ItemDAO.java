package com.auction.server.dao;

import com.auction.shared.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;     
import java.util.ArrayList;

public class ItemDAO  {
    public static ItemDAO instance() {
        return new ItemDAO();
    }

    public void create(Item obj) {
        String sql = "INSERT INTO items (seller_id, item_name, description, item_type, start_price, image_url, duration_minutes, status, special_info) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Bơm dữ liệu từ Object vào các dấu hỏi chấm (?)
            ps.setInt(1, obj.getSellerId());
            ps.setString(2, obj.getName());
            ps.setString(3, obj.getDescription());
            ps.setString(4, obj.getItemType());
            ps.setBigDecimal(5, obj.getStartingPrice());
            ps.setString(6, obj.getImageUrl() != null ? obj.getImageUrl() : "");
            ps.setInt(7, obj.getDurationMinutes());
            ps.setString(8, obj.getStatus());
            ps.setString(9, obj.getSpecialInfo() != null ? obj.getSpecialInfo() : "");

            // Thực thi lệnh chèn xuống CSDL
            int kq = ps.executeUpdate();

            if (kq > 0) {
                System.out.println("[DATABASE] Đã lưu sản phẩm '" + obj.getName() + "' vào kho CHỜ DUYỆT thành công!");
            } else {
                System.out.println("[DATABASE] Thêm thất bại, vui lòng kiểm tra lại dữ liệu.");
            }

        } catch (Exception e) {
            System.err.println("[DATABASE ERROR] Lỗi khi lưu sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /// Hàm cập nhật thông tin sản phẩm dưới Database
    public static void update(Item obj) {
        String sql = "UPDATE items SET seller_id = ?, item_name = ?, description = ?, "
                + "start_price = ?, image_url = ?, status = ?, special_info = ? "
                + "WHERE item_id = ?";

        try (Connection connection = JDBCUtil.getConnection();
             PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setInt(1, obj.getSellerId());
            pst.setString(2, obj.getName());
            pst.setString(3, obj.getDescription());
            pst.setBigDecimal(4, obj.getStartingPrice());
            pst.setString(5, obj.getImageUrl() != null ? obj.getImageUrl() : "");
            pst.setString(6, obj.getStatus() != null ? obj.getStatus() : "PENDING");
            pst.setString(7, obj.getSpecialInfo() != null ? obj.getSpecialInfo() : "");

            /// Dấu chấm hỏi cuối cùng cho điều kiện WHERE
            pst.setInt(8, obj.getId());

            int kq = pst.executeUpdate();
            if (kq > 0) {
                System.out.println("[DATABASE] Đã cập nhật trạng thái/thông tin sản phẩm thành công!");
            } else {
                System.out.println("[DATABASE] Cập nhật thất bại, không tìm thấy sản phẩm ID: " + obj.getId());
            }
        } catch (Exception e) {
            System.err.println("[DAO ERROR] Lỗi cập nhật Item: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /// Hàm xóa sản phẩm khỏi Database dựa vào ID sản phẩm
    /// Trả về true nếu xóa thành công, false nếu thất bại
    public static boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId); // Gắn ID cần xóa vào dấu ?

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Nếu có dòng bị ảnh hưởng nghĩa là đã xóa thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /// Hàm cập nhật trạng thái sản phẩm từ PENDING sang ACTIVE (Duyệt đưa lên sàn)
    public static boolean approveItem(int itemId) {
        String sql = "UPDATE items SET status = 'ACTIVE' WHERE item_id = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công dòng dữ liệu

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi thực thi duyệt sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /// Hàm truy vấn toàn bộ sản phẩm đang ở trạng thái PENDING (Chờ duyệt) phục vụ màn hình kiểm duyệt của Admin
    public static List<Item> findPendingItems() {
        List<Item> list = new ArrayList<>();

        /// Dùng UPPER và TRIM để chống sai lệch do viết hoa/thường hoặc thừa dấu cách dưới Database
        String sql = "SELECT * FROM items WHERE UPPER(TRIM(status)) = 'PENDING'";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("item_type");

                /// In ra màn hình xem Server có đang đọc được dòng này từ DB không
                System.out.println("[DEBUG] Đang xét sản phẩm ID: " + rs.getInt("item_id") + " | Loại lấy từ DB: [" + type + "]");

                /// Xóa khoảng trắng thừa của type trước khi đưa vào hàm tạo để chống lỗi ngầm
                if (type != null) type = type.trim();

                Item item = Item.createFromType(type);

                if (item != null) {
                    item.setItemType(type);
                    item.setId(rs.getInt("item_id"));
                    item.setSellerId(rs.getInt("seller_id"));
                    item.setName(rs.getString("item_name"));
                    item.setDescription(rs.getString("description"));

                    try {
                        item.setStartingPrice(rs.getBigDecimal("start_price"));
                    } catch (SQLException e1) {
                        try {
                            item.setStartingPrice(rs.getBigDecimal("starting_price"));
                        } catch (SQLException e2) {
                            item.setStartingPrice(java.math.BigDecimal.ZERO);
                        }
                    }

                    item.setImageUrl(rs.getString("image_url"));
                    item.setDurationMinutes(rs.getInt("duration_minutes"));
                    item.setStatus(rs.getString("status"));

                    /// Bọc an toàn cho cột special_info đề phòng DB chưa khởi tạo cột này
                    try {
                        item.setSpecialInfo(rs.getString("special_info"));
                    } catch (SQLException e) {
                        item.setSpecialInfo("");
                    }

                    list.add(item);
                    System.out.println("   -> THÀNH CÔNG: Đã nạp sản phẩm [" + item.getName() + "] vào mảng!");
                } else {
                    /// TRẠM GIÁM SÁT 2: BẮT QUẢ TANG nếu class Item từ chối khởi tạo!
                    System.err.println("   -> BỊ TỪ CHỐI BỎ QUA: Hàm createFromType không nhận diện được chữ [" + type + "] !");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi câu lệnh SQL khi tải PENDING: " + e.getMessage());
        }
        return list;
    }


    /// Lấy danh sách sản phẩm theo ID người bán, sắp xếp mới nhất lên đầu
    public static List<Item> findItemsBySellerId(int sellerId) {

        String sql = "SELECT * FROM items WHERE seller_id = ? ORDER BY item_id DESC";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId); // Gán giá trị sellerId vào dấu chấm hỏi
            ResultSet rs = stmt.executeQuery();

            List<Item> sellerItems = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString("item_type");
                Item item = Item.createFromType(type);
                item.setItemType(type);

                item.setId(rs.getInt("item_id"));
                item.setSellerId(rs.getInt("seller_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setStartingPrice(rs.getBigDecimal("start_price"));
                item.setImageUrl(rs.getString("image_url"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));

                // ĐÃ BỔ SUNG: Lấy thêm Trạng thái và Thông tin đặc biệt từ CSDL lên
                item.setStatus(rs.getString("status"));
                item.setSpecialInfo(rs.getString("special_info"));

                sellerItems.add(item);
            }
            return sellerItems;
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi lấy sản phẩm của Seller: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


    /// Hàm tìm kiếm sản phẩm theo tên .
    public Item selectByName(String name) {
    String sql = "SELECT * FROM items WHERE item_name = ?";

    try (Connection conn = JDBCUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, name); // Gán giá trị name vào dấu chấm hỏi
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Item item=null;
            String type= rs.getString("item_type");
            item = Item.createFromType(type);
            item.setId(rs.getInt("item_id"));
            item.setSellerId(rs.getInt("seller_id"));
            item.setName(rs.getString("item_name"));
            item.setDescription(rs.getString("description"));
            item.setStartingPrice(rs.getBigDecimal("starting_price"));
            item.setImageUrl(rs.getString("image_url"));
            item.setDurationMinutes(rs.getInt("duration_minutes"));
            item.setStatus(rs.getString("status"));
            return item;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Trả về null nếu không tìm thấy người dùng
}
    /// Hàm thống kê tổng số lượng tài sản (sản phẩm) hiện có trong database phục vụ trang Dashboard Admin
    public static int getTotalItemsCount() {
        String sql = "SELECT COUNT(*) AS total FROM items";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total"); // Trả về con số đếm được
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi tính tổng tài sản: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; /// Trả về 0 nếu hệ thống trống hoặc gặp lỗi kết nối
    }


    public static Item selectById(int id) {
        String sql = "SELECT * FROM items WHERE item_id = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("item_type");
                if (type != null) type = type.trim(); // Cắt khoảng trắng thừa bảo vệ dữ liệu

                Item item = Item.createFromType(type);
                if (item != null) {
                    item.setItemType(type);
                    item.setId(rs.getInt("item_id"));
                    item.setSellerId(rs.getInt("seller_id"));
                    item.setName(rs.getString("item_name"));
                    item.setDescription(rs.getString("description"));

                    /// [FIX BUG CỐT LÕI]: Quét cả 2 trường hợp tên cột giá để 100% không bị sập hàm
                    try {
                        item.setStartingPrice(rs.getBigDecimal("start_price"));
                    } catch (SQLException e1) {
                        try {
                            item.setStartingPrice(rs.getBigDecimal("starting_price"));
                        } catch (SQLException e2) {
                            item.setStartingPrice(java.math.BigDecimal.ZERO);
                        }
                    }

                    item.setImageUrl(rs.getString("image_url"));
                    item.setDurationMinutes(rs.getInt("duration_minutes"));
                    item.setStatus(rs.getString("status"));

                    /// Bổ sung nạp thông tin đặc biệt
                    try {
                        item.setSpecialInfo(rs.getString("special_info"));
                    } catch (SQLException e) {
                        item.setSpecialInfo("");
                    }

                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Lỗi selectById: " + e.getMessage());
            e.printStackTrace();
        }
        return null; /// Trả về null nếu không tìm thấy hoặc lỗi SQL
    }

}
