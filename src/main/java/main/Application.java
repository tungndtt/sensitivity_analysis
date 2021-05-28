package main;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import condition.ComparisionType;
import query.common.TimestampQuery;
import xlog.XLogUtil;

public class Application {

    private static String UserName = "postgres";
    private static String Password = "tungndtt";
    private static String DatabaseName = "sensitive_analysis";

    private static Connection dbConnection;

    public static void main(String[] args) throws ParseException {
        //boolean success = XLogUtil.insertIntoDatabase("C:/Users/Tung Doan/Downloads/log_IEEE.xes.gz");

        /*
        String tableName = "log_ieee";
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        TimestampQuery caseIdQuery = new TimestampQuery(tableName, ft.parse("1949-01-26"), ComparisionType.LT);

        try {
            String query = caseIdQuery.getQuery();
            System.out.println(query);
            ResultSet resultSet = Application.getConnection().prepareStatement(query).executeQuery();
            resultSet.next();
            System.out.println(resultSet.getObject("activity"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
         */
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
