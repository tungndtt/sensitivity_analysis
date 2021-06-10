package db_connection;

import java.sql.*;

/**
 * Implementation to get the database connection
 *
 * @author Tung Doan
 */
public class DbConnection {

    private static String UserName = "postgres";
    private static String Password = "tungndtt";
    private static String url = "localhost:5432";
    private static String DatabaseName = "sensitive_analysis";

    private static Connection dbConnection;

    private static boolean establishConnection() {
        try {
            Class.forName("org.postgresql.Driver");

            String url = String.format("jdbc:postgresql://%s/%s", DbConnection.url, DbConnection.DatabaseName);

            DbConnection.dbConnection = DriverManager.getConnection(
                    url, DbConnection.UserName, DbConnection.Password
            );

            return true;
        }
        catch(Exception e) {
            System.out.println(e);

            return false;
        }
    }

    public static Connection getConnection() {
        if(DbConnection.dbConnection == null) {
            boolean success = DbConnection.establishConnection();

            if(success) {
                System.out.println("Establish connection successfully!");
            }
        }

        return DbConnection.dbConnection;
    }
}
