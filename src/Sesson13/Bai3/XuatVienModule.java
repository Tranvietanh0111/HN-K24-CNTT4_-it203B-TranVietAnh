package Sesson13.Bai3;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Sesson13.dbConnect;

/**
 * BÁO CÁO PHÂN TÍCH NGHIỆP VỤ XUẤT VIỆN (ALL OR NOTHING)
 * * 1. PHÂN TÍCH I/O:
 * - Input: maBenhNhan (int), tienVienPhi (double).
 * - Output: Thông báo trạng thái giao dịch (Thành công/Thất bại & Rollback).
 * * 2. ĐỀ XUẤT GIẢI PHÁP:
 * - Sử dụng JDBC Transaction (setAutoCommit(false)) để đảm bảo tính Atomicity.
 * - Xử lý Bẫy 1 (Thiếu tiền): Dùng SELECT kiểm tra trước khi UPDATE. Nếu balance < viện phí,
 * chủ động throw Exception để kích hoạt Rollback.
 * - Xử lý Bẫy 2 (Dữ liệu ảo): Kiểm tra giá trị trả về của executeUpdate().
 * Nếu bằng 0 (không tìm thấy ID), chủ động throw Exception để Rollback.
 * * 3. THIẾT KẾ CÁC BƯỚC:
 * - B1: Mở kết nối qua dbConnect và tắt AutoCommit.
 * - B2: Kiểm tra số dư ví (Bẫy 1).
 * - B3: Thực hiện 3 lệnh UPDATE (Ví tiền -> Giường bệnh -> Trạng thái BN).
 * - B4: Kiểm tra Row Affected sau mỗi lệnh UPDATE (Bẫy 2).
 * - B5: Commit nếu tất cả thành công hoặc Rollback nếu có bất kỳ lỗi nào.
 * - B6: Đóng kết nối an toàn trong khối finally.
 */
public class XuatVienModule {

    public void xuatVienVaThanhToan(int maBenhNhan, double tienVienPhi) {
        Connection conn = null;
        try {
            // Bước 1: Mở kết nối từ file dbConnect dùng chung
            conn = dbConnect.getConnection();

            // Bước 2: Bắt đầu giao dịch thủ công
            conn.setAutoCommit(false);

            // --- BẪY 1: KIỂM TRA LOGIC NGHIỆP VỤ (THIẾU TIỀN) ---
            // Phải lấy số dư lên kiểm tra trước khi trừ tiền để tránh số dư bị âm
            String sqlCheck = "SELECT balance FROM Patient_Wallet WHERE patient_id = ?";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, maBenhNhan);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                double soDuHienTai = rs.getDouble("balance");
                if (soDuHienTai < tienVienPhi) {
                    // Nếu không đủ tiền, ném ngoại lệ để nhảy xuống khối catch thực hiện Rollback
                    throw new Exception("BẪY 1: Số dư không đủ (" + soDuHienTai + "). Không thể thanh toán!");
                }
            } else {
                throw new Exception("Lỗi: Không tìm thấy thông tin ví của bệnh nhân!");
            }

            // --- THAO TÁC 1: TRỪ TIỀN VIỆN PHÍ ---
            String sql1 = "UPDATE Patient_Wallet SET balance = balance - ? WHERE patient_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setDouble(1, tienVienPhi);
            ps1.setInt(2, maBenhNhan);
            ps1.executeUpdate();

            // --- THAO TÁC 2: GIẢI PHÓNG GIƯỜNG BỆNH ---
            String sql2 = "UPDATE Beds SET status = 'EMPTY' WHERE patient_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, maBenhNhan);
            int rowsBed = ps2.executeUpdate();

            // --- BẪY 2: KIỂM TRA DỮ LIỆU ẢO (ROW AFFECTED = 0) ---
            // Nếu bệnh nhân không có giường, lệnh UPDATE vẫn chạy nhưng trả về 0 dòng ảnh hưởng
            if (rowsBed == 0) {
                throw new Exception("BẪY 2: Bệnh nhân không có giường bệnh đang sử dụng. Hủy giao dịch!");
            }

            // --- THAO TÁC 3: CẬP NHẬT TRẠNG THÁI XUẤT VIỆN ---
            String sql3 = "UPDATE Patients SET status = 'DISCHARGED' WHERE id = ?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setInt(1, maBenhNhan);
            int rowsPatient = ps3.executeUpdate();

            // --- BẪY 2 (TIẾP TỤC): KIỂM TRA MÃ BỆNH NHÂN TỒN TẠI ---
            if (rowsPatient == 0) {
                throw new Exception("BẪY 2: Mã bệnh nhân không tồn tại trên hệ thống!");
            }

            // BƯỚC CUỐI: XÁC NHẬN GIAO DỊCH (COMMIT)
            // Chỉ chạy đến đây nếu không gặp bất kỳ Exception nào ở trên
            conn.commit();
            System.out.println(">>> THÀNH CÔNG: Đã trừ tiền, giải phóng giường và cho BN xuất viện.");

        } catch (Exception e) {
            // XỬ LÝ LỖI VÀ ROLLBACK
            System.err.println(">>> THẤT BẠI: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Đưa mọi thứ về trạng thái ban đầu
                    System.out.println(">>> HỆ THỐNG: Đã thực hiện Rollback an toàn.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Bước đóng kết nối và dọn dẹp tài nguyên
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}