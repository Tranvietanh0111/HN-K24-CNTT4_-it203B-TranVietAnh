package Sesson12;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnect {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Hospital_DB";
    private static final String USER = "root";
    private static final String PASS = "";
    public static void main(String[] args) {
        exampleQuery();

    }
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy Driver JDBC!");
            return null;
        }
    }
    public static void exampleQuery() {
        Connection conn = null;
        try {
            conn = getConnection();
            // Thực hiện các thao tác SQL tại đây...
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 2. Đảm bảo đóng kết nối trong khối finally để giải phóng tài nguyên
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}