package Sesson14.Ktradaugio;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnect {
    private static final String URL = "jdbc:mysql://localhost:3306/BankDB";
    private static final String USER = "root";
    private static final String PASS = "vanhdzpro2";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}