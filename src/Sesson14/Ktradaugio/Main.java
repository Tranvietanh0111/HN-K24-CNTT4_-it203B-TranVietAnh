package Sesson14.Ktradaugio;
import java.sql.*;
import Sesson14.Ktradaugio.dbConnect;
public class Main {
    public static void main(String[] args) {
        String fromId = "ACC01";
        String toId = "ACC02";
        double amount = 1000.0;
        try (Connection conn = dbConnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!isEligible(conn, fromId, amount)) {
                    System.out.println("Tai khoan khong ton tai hoac khong du so du!");
                    return;
                }
                String sqlCall = "{call sp_UpdateBalance(?, ?)}";
                try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
                    cstmt.setString(1, fromId);
                    cstmt.setDouble(2, -amount);
                    cstmt.execute();
                    cstmt.setString(1, toId);
                    cstmt.setDouble(2, amount);
                    cstmt.execute();
                }
                conn.commit();
                System.out.println("Giao dich chuyen khoan thanh cong");
                showReport(conn, fromId, toId);

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Co loi xay ra, da rollback giao dich: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Loi ket noi CSDL: " + e.getMessage());
        }
    }
    private static boolean isEligible(Connection conn, String id, double amount) throws SQLException {
        String sql = "SELECT Balance FROM Accounts WHERE AccountId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double currentBalance = rs.getDouble("Balance");
                    return currentBalance >= amount;
                }
            }
        }
        return false;
    }
    private static void showReport(Connection conn, String id1, String id2) throws SQLException {
        String sql = "SELECT * FROM Accounts WHERE AccountId IN (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id1);
            pstmt.setString(2, id2);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n----BAng ket qua doi soat------");
                System.out.printf("%-10s | %-20s | %-10s\n", "ID", "Ho Ten", "So Du");
                while (rs.next()) {
                    System.out.printf("%-10s | %-20s | %-10.2f\n",
                            rs.getString("AccountId"),
                            rs.getString("FullName"),
                            rs.getDouble("Balance"));
                }
            }
        }
    }
}