package Sesson12.Bai4;
import Sesson12.dbConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * [Bài tập] Tối ưu hiệu năng nạp kết quả xét nghiệm (Execution Plan)
 * Người thực hiện: Trần Việt Anh (Teddy)
 */
public class LabResultsImporter {

    public void importResults(List<LabResult> results) {
        // 1. Khai báo cấu trúc SQL dùng dấu hỏi chấm (?)
        String sql = "INSERT INTO lab_results (patient_id, test_type, result_value) VALUES (?, ?, ?)";

        try (Connection conn = dbConnect.getConnection();
             // PHẦN 1: PHÂN TÍCH SỰ LÃNG PHÍ TÀI NGUYÊN (EXECUTION PLAN)
             // -------------------------------------------------------------------------
             // - Khi dùng Statement cũ (nối chuỗi), mỗi lần lặp Database phải thực hiện:
             //   Parse (Phân tích cú pháp) -> Validate (Kiểm tra quyền) -> Optimizer (Lập kế hoạch thực thi).
             // - Với 1.000 kết quả, Database phải làm việc này 1.000 lần cho cùng 1 cấu trúc lệnh.
             // - Sử dụng PreparedStatement giúp Database CHỈ LẬP KẾ HOẠCH THỰC THI 1 LẦN DUY NHẤT
             //   tại dòng code dưới đây (Pre-compiled). Các lần lặp sau chỉ việc nạp dữ liệu vào.
             // -------------------------------------------------------------------------
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Tắt Auto Commit để tối ưu hóa việc ghi dữ liệu theo lô (Batch Processing)
            conn.setAutoCommit(false);

            long startTime = System.currentTimeMillis();

            for (LabResult res : results) {
                // PHẦN 2: THỰC THI TỐI ƯU TRONG VÒNG LẶP
                // Chỉ nạp tham số (Parameters), không gửi lại cấu trúc lệnh SQL
                pstmt.setInt(1, res.getPatientId());
                pstmt.setString(2, res.getTestType());
                pstmt.setDouble(3, res.getResultValue());

                // Thêm vào Batch (Gói dữ liệu) thay vì thực thi ngay lập tức
                pstmt.addBatch();
            }

            // Thực thi toàn bộ 1.000 dòng trong 1 lần gửi duy nhất
            pstmt.executeBatch();
            conn.commit(); // Lưu thay đổi vào Database

            long endTime = System.currentTimeMillis();

            // ĐÁNH GIÁ SỰ KHÁC BIỆT:
            // Tốc độ có thể nhanh hơn gấp 10 - 50 lần so với cách nối chuỗi Statement cũ
            // do giảm tải được hàng ngàn lần giao tiếp (Round-trip) giữa Java và Database.
            System.out.println("Đã nạp " + results.size() + " kết quả xét nghiệm.");
            System.out.println("Thời gian thực hiện: " + (endTime - startTime) + "ms");

        } catch (SQLException e) {
            System.err.println("Lỗi nạp dữ liệu: " + e.getMessage());
        }
    }
}

/**
 * Lớp hỗ trợ LabResult để code không bị báo lỗi đỏ
 */
class LabResult {
    private int patientId;
    private String testType;
    private double resultValue;

    public LabResult(int patientId, String testType, double resultValue) {
        this.patientId = patientId;
        this.testType = testType;
        this.resultValue = resultValue;
    }

    public int getPatientId() { return patientId; }
    public String getTestType() { return testType; }
    public double getResultValue() { return resultValue; }
}