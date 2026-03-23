package Sesson12.Bai3;
import Sesson12.dbConnect;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * [Bài tập] Module Tra cứu chi phí phẫu thuật (OUT Parameter)
 * Người thực hiện: Trần Việt Anh (Teddy)
 */
public class SurgeryFeeLookup {

    public void getSurgeryFee(int surgeryId) {
        // Cú pháp gọi Stored Procedure: { call Ten_Procedure(?, ?) }
        String sql = "{ call GET_SURGERY_FEE(?, ?) }";

        try (Connection conn = dbConnect.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            // Gán giá trị cho tham số đầu vào (IN parameter) tại vị trí dấu hỏi số 1
            cstmt.setInt(1, surgeryId);

            // =========================================================================
            // PHẦN 1: PHÂN TÍCH VỀ registerOutParameter()
            // =========================================================================
            // 1. TẠI SAO BẮT BUỘC PHẢI GỌI registerOutParameter()?
            // Khác với IN parameter (Java gửi đi), OUT parameter là giá trị DB trả về.
            // JDBC cần đăng ký trước để Driver biết:
            //   - Vị trí nào là tham số đầu ra (Index).
            //   - Kiểu dữ liệu mong muốn nhận về để cấp phát bộ nhớ phù hợp.
            // Nếu không đăng ký, hệ thống sẽ báo lỗi "Column index out of range"
            // vì không tìm thấy định nghĩa cho giá trị trả về.
            //
            // 2. XỬ LÝ KIỂU DECIMAL:
            // Trong SQL, nếu tham số là DECIMAL (kiểu tiền tệ/số thập phân chính xác),
            // trong Java ta phải đăng ký bằng hằng số: java.sql.Types.DECIMAL.
            // =========================================================================

            cstmt.registerOutParameter(2, Types.DECIMAL);

            // Thực thi Procedure
            cstmt.execute();

            // PHẦN 2: THỰC THI & LẤY GIÁ TRỊ
            // Sau khi execute(), giá trị từ DB sẽ được đổ vào tham số OUT đã đăng ký.
            // Ta dùng các hàm get... tương ứng để lấy dữ liệu ra.
            double totalCost = cstmt.getDouble(2);

            if (totalCost > 0) {
                System.out.println("--- KẾT QUẢ CHI PHÍ PHẪU THUẬT ---");
                System.out.println("ID phẫu thuật: " + surgeryId);
                System.out.println("Tổng chi phí: " + totalCost + " VND");
            } else {
                System.out.println("Không có dữ liệu chi phí cho mã này.");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi gọi Procedure: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SurgeryFeeLookup app = new SurgeryFeeLookup();
        // Giả sử tra cứu phí cho ca phẫu thuật mã số 10
        app.getSurgeryFee(10);
    }
}