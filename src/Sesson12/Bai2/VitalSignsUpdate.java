package Sesson12.Bai2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import Sesson12.dbConnect;

/**
 * [Bài tập] Cập nhật chỉ số sinh tồn bệnh nhân (Type Handling)
 * Người thực hiện: Trần Việt Anh (Teddy)
 */
public class VitalSignsUpdate {

    public void updateVitals(int patientId, double temperature, int heartRate) {
        // SQL sử dụng tham số ? (Placeholder) để tách biệt cấu trúc và dữ liệu
        String sql = "UPDATE patient_vitals SET temperature = ?, heart_rate = ? WHERE patient_id = ?";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // =========================================================================
            // PHẦN 1: PHÂN TÍCH TẠI SAO setDouble(), setInt() GIÚP TRÁNH LỖI ĐỊNH DẠNG
            // =========================================================================
            // 1. ĐỘC LẬP VỚI LOCALE (VÙNG MIỀN):
            // Khi nối chuỗi trực tiếp, Java sẽ chuyển số double 37.5 thành String "37,5"
            // nếu máy tính cài đặt vùng miền là Việt Nam/Pháp. SQL sẽ báo lỗi cú pháp dấu phẩy.
            // Ngược lại, setDouble() truyền dữ liệu dưới dạng số thực thuần túy (Binary),
            // không đi qua bước chuyển đổi String của hệ điều hành, nên luôn đúng chuẩn SQL.
            //
            // 2. TỰ ĐỘNG ÉP KIỂU (TYPE MAPPING):
            // JDBC Driver tự động ánh xạ kiểu dữ liệu Java (double, int) sang kiểu tương ứng
            // của Database (FLOAT/DOUBLE, INT). Lập trình viên không cần lo lắng về việc
            // thêm dấu nháy đơn hay định dạng số sao cho khớp với DB.
            // =========================================================================

            pstmt.setDouble(1, temperature); // Truyền 37.5 trực tiếp dưới dạng số thực
            pstmt.setInt(2, heartRate);      // Truyền nhịp tim dưới dạng số nguyên
            pstmt.setInt(3, patientId);      // Điều kiện lọc theo ID bệnh nhân

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Cập nhật thành công cho bệnh nhân: " + patientId);
            } else {
                System.out.println("Không tìm thấy ID bệnh nhân.");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi hệ thống SQL: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        VitalSignsUpdate app = new VitalSignsUpdate();
        // Giả sử đầu vào là 37.5 (double)
        app.updateVitals(1, 37.5, 80);
    }
}