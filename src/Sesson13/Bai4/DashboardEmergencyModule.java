package Sesson13.Bai4;
import Sesson13.dbConnect;
import java.sql.*;
import java.util.*;

/**
 * [BÁO CÁO GIẢI CỨU DASHBOARD - VẤN ĐỀ N+1 QUERY]
 * * 1. PHÂN TÍCH I/O:
 * - Input: ngayHienTai (String/Date) để lọc bệnh nhân trong ngày.
 * - Output: List<BenhNhanDTO> (Chứa thông tin BN và danh sách dịch vụ đi kèm).
 * * 2. SO SÁNH GIẢI PHÁP:
 * - Giải pháp 1 (N+1 Query): Lấy 500 BN, sau đó lặp 500 lần để lấy dịch vụ từng người.
 * => Nhược điểm: Tốn 501 Query, Network I/O cực cao, Dashboard bị treo (10-15s).
 * - Giải pháp 2 (LEFT JOIN + Map Grouping): Dùng 1 Query duy nhất lấy toàn bộ dữ liệu.
 * => Ưu điểm: Tốc độ < 1s, tối ưu Network I/O, xử lý gộp dữ liệu cực nhanh trên RAM.
 * * 3. CHỐT GIẢI PHÁP: Sử dụng Giải pháp 2 (LEFT JOIN) để đáp ứng yêu cầu thời gian vàng.
 */
public class DashboardEmergencyModule {

    public List<BenhNhanDTO> getDashboardData(String ngayHienTai) {
        // Sử dụng LinkedHashMap để gom nhóm dịch vụ theo Bệnh nhân và giữ đúng thứ tự từ DB
        Map<Integer, BenhNhanDTO> mapDashboard = new LinkedHashMap<>();

        // THIẾT KẾ SQL: Sử dụng LEFT JOIN (Thay vì INNER JOIN) 
        // để đảm bảo bệnh nhân chưa có dịch vụ vẫn không bị mất tên khỏi danh sách.
        String sql = "SELECT b.id, b.ten_benh_nhan, d.id AS dv_id, d.ten_dich_vu " +
                "FROM BenhNhan b " +
                "LEFT JOIN DichVuSuDung d ON b.id = d.maBenhNhan " +
                "WHERE b.ngay_nhap_vien = ?";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ngayHienTai);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int maBN = rs.getInt("id");

                // KIỂM TRA VÀ TẠO MỚI BỆNH NHÂN TRONG MAP (Xử lý Bẫy 2)
                BenhNhanDTO benhNhan = mapDashboard.get(maBN);

                if (benhNhan == null) {
                    benhNhan = new BenhNhanDTO();
                    benhNhan.setMaBenhNhan(maBN);
                    benhNhan.setTenBenhNhan(rs.getString("ten_benh_nhan"));

                    // KHỞI TẠO LIST TRỐNG: Chống lỗi NullPointerException khi Frontend truy cập dsDichVu
                    benhNhan.setDsDichVu(new ArrayList<>());

                    mapDashboard.put(maBN, benhNhan);
                }

                // XỬ LÝ BẪY 2 - DỮ LIỆU TRỐNG: 
                // Nếu BN chưa có dịch vụ, các cột bảng DichVuSuDung sẽ là NULL do LEFT JOIN.
                int maDV = rs.getInt("dv_id");

                // Kiểm tra xem cột dv_id có thực sự có dữ liệu hay không
                if (!rs.wasNull()) {
                    // Chỉ tạo Object và add vào List nếu dịch vụ thực sự tồn tại
                    DichVu dv = new DichVu();
                    dv.setMaDichVu(maDV);
                    dv.setTenDichVu(rs.getString("ten_dich_vu"));
                    benhNhan.getDsDichVu().add(dv);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn Dashboard: " + e.getMessage());
        }

        // Chuyển Map thành List để trả về cho Frontend
        return new ArrayList<>(mapDashboard.values());
    }
}

/**
 * [BẪY NGHIỆP VỤ] - ĐỊNH NGHĨA DTO
 * Đảm bảo tính đóng gói và cấu trúc phân cấp 1-N (Bệnh nhân có nhiều dịch vụ)
 */
class BenhNhanDTO {
    private int maBenhNhan;
    private String tenBenhNhan;
    private List<DichVu> dsDichVu; // Danh sách chi tiết dịch vụ

    // Getters và Setters...
    public int getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(int maBN) { this.maBenhNhan = maBN; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String ten) { this.tenBenhNhan = ten; }
    public List<DichVu> getDsDichVu() { return dsDichVu; }
    public void setDsDichVu(List<DichVu> list) { this.dsDichVu = list; }
}

class DichVu {
    private int maDichVu;
    private String tenDichVu;

    // Getters và Setters...
    public int getMaDichVu() { return maDichVu; }
    public void setMaDichVu(int maDV) { this.maDichVu = maDV; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String ten) { this.tenDichVu = ten; }
}