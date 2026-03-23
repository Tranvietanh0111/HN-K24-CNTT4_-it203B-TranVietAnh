package Sesson12.Bai1;
import Sesson12.dbConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorLogin {

    public boolean login(String doctorCode, String password) {
        String sql = "SELECT * FROM doctors WHERE doctor_code = ? AND password = ?";

        // Thay DatabaseConnection thành dbConnect cho đúng với file của bạn
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            /* PHÂN TÍCH: PreparedStatement là "tấm khiên" vì:
               - Nó tách biệt hoàn toàn giữa câu lệnh (SQL) và dữ liệu (Parameter).
               - Cơ chế Pre-compiled giúp hệ thống hiểu cấu trúc lệnh trước khi nhận dữ liệu.
               - Dữ liệu người dùng truyền vào qua pstmt.setString sẽ không bao giờ
                 có thể thay đổi logic của câu lệnh SELECT ban đầu.
            */

            pstmt.setString(1, doctorCode);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Đăng nhập thành công!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn: " + e.getMessage());
        }

        System.out.println("Thông tin đăng nhập không chính xác.");
        return false;
    }
}