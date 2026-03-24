package Sesson13.Bai1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import Sesson13.dbConnect;

public class MedicineModule {

    /**
     * PHẦN 1 - PHÂN TÍCH LOGIC:
     * - Trong JDBC, chế độ Auto-Commit mặc định là true. Mỗi khi executeUpdate() chạy xong,
     * dữ liệu sẽ được COMMIT (lưu vĩnh viễn) ngay lập tức xuống Database.
     * - Khi lỗi 'int x = 10 / 0' xảy ra, Thao tác 1 (Trừ kho) đã thực thi và commit thành công.
     * - Chương trình bị ngắt và nhảy vào khối catch trước khi Thao tác 2 (Ghi bệnh án) kịp chạy.
     * - Hệ thống không có cơ chế quay lại (Rollback), dẫn đến thuốc mất nhưng bệnh án không có.
     */
    public void capPhatThuoc(int medicineId, int patientId) {
        Connection conn = null;
        try {
            conn = dbConnect.getConnection();

            // PHẦN 2 - THỰC THI:
            // 1. Tắt Auto-commit để bắt đầu một Transaction thủ công
            conn.setAutoCommit(false);

            // Thao tác 1: Trừ kho
            String sql1 = "UPDATE Medicine_Inventory SET quantity = quantity - 1 WHERE medicine_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, medicineId);
            ps1.executeUpdate();

            // Giả lập lỗi ở giữa dòng code
            // int x = 10 / 0;

            // Thao tác 2: Ghi lịch sử (Dùng NOW() cho MySQL thay cho GETDATE())
            String sql2 = "INSERT INTO Prescription_History (patient_id, medicine_id, date) VALUES (?, ?, NOW())";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, patientId);
            ps2.setInt(2, medicineId);
            ps2.executeUpdate();

            // 2. Chỉ xác nhận lưu dữ liệu khi cả hai thao tác trên đều thành công
            conn.commit();
            System.out.println("Bài 1: Cấp phát thuốc thành công.");

        } catch (Exception e) {
            // 3. Nếu có lỗi, thực hiện khôi phục dữ liệu về trạng thái ban đầu
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Bài 1: Đã Rollback dữ liệu do lỗi: " + e.getMessage());
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            close(conn);
        }
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}