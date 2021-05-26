package main;

import java.sql.*;

import xlog.XLogUtil;

public class Application {

    private static String UserName = "postgres";
    private static String Password = "tungndtt";
    private static String DatabaseName = "sensitive_analysis";

    private static Connection dbConnection;

    public static void main(String[] args) {
        //boolean success = XLogUtil.insertIntoDatabase("C:/Users/Tung Doan/Downloads/log_IEEE.xes.gz");
    }

    private static boolean establishConnection() {
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://localhost:5432/" + Application.DatabaseName;

            Application.dbConnection = DriverManager.getConnection(
                    url, Application.UserName, Application.Password
            );

            return true;
        }
        catch(Exception e) {
            System.out.println(e);

            return false;
        }
    }

    public static Connection getConnection() {
        if(Application.dbConnection == null) {
            boolean success = Application.establishConnection();

            if(success) {
                System.out.println("Establish connection successfully!");
            }
        }

        return Application.dbConnection;
    }
}
