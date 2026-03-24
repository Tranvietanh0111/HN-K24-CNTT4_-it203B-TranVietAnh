package Sesson13.Bai2;
import Sesson13.dbConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentModule {

    /**
     * PHẦN 1 - PHÂN TÍCH LOGIC:
     * - Tại sao thuốc vẫn bị trừ: JDBC mặc định để Auto-Commit là true.
     * - Khi thực hiện xong lệnh update kho (ps1.executeUpdate), JDBC gửi lệnh COMMIT ngay lập tức xuống DB.
     * - Lỗi '10/0' xảy ra sau đó khiến chương trình dừng lại, nhưng lệnh trừ kho đã được lưu vĩnh viễn.
     * - Kết quả: Dữ liệu không đồng nhất (Thuốc mất nhưng không có bệnh án), vi phạm tính Atomicity.
     */
    public void capPhatThuoc(int medicineId, int patientId) {
        Connection conn = null;
        try {
            conn = dbConnect.getConnection();

            // PHẦN 2 - THỰC THI: Sửa mã nguồn
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Thao tác 1: Trừ kho
            String sql1 = "UPDATE Medicine_Inventory SET quantity = quantity - 1 WHERE medicine_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, medicineId);
            ps1.executeUpdate();

            // Giả lập lỗi giữa chừng
            // int x = 10 / 0;

            // Thao tác 2: Ghi lịch sử (Sử dụng NOW() cho MySQL)
            String sql2 = "INSERT INTO Prescription_History (patient_id, medicine_id, date) VALUES (?, ?, NOW())";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, patientId);
            ps2.setInt(2, medicineId);
            ps2.executeUpdate();

            // Xác nhận hoàn tất cả 2 thao tác
            conn.commit();
            System.out.println("Bài 1: Cấp phát thuốc thành công.");

        } catch (Exception e) {
            // Hủy bỏ giao dịch nếu có lỗi
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Bài 1: Đã Rollback dữ liệu.");
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