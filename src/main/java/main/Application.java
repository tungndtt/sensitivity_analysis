package main;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import condition.ComparisionType;
import query.common.TimestampQuery;
import xlog.XLogUtil;

public class Application {

    private static String UserName = "postgres";
    private static String Password = "tungndtt";
    private static String DatabaseName = "sensitive_analysis";

    private static Connection dbConnection;

    public static void main(String[] args) {
        Application.testPlotting();
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

    // Test
    private static void testPlotting() {
        LinkedList<double[]> points = new LinkedList<>();
        points.add(new double[]{1.0, 0.1});
        points.add(new double[]{2.0, 0.12});
        points.add(new double[]{3.0, 0.15});
        points.add(new double[]{4.0, 0.15});
        points.add(new double[]{5.0, 0.18});
        points.add(new double[]{6.0, 0.61});
        points.add(new double[]{7.0, 0.65});
        points.add(new double[]{8.0, 0.66});
        points.add(new double[]{9.0, 0.68});

        Plot plotChart = new Plot("Case information", "variation", "Duration (day)", "Difference (%)", points);
        plotChart.display();
    }

    // Test
    private static void testInsertingEventLog() {
        boolean success = XLogUtil.insertIntoDatabase("C:/Users/Tung Doan/Downloads/log_IEEE.xes.gz");
        if (success) {
            System.out.println("Successfully importing the event log!");
        }
        else {
            System.out.println("Fail to import the event log!");
        }
    }

    // Test
    private static void testQuery() {
        String tableName = "log_ieee";
        try {
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
            TimestampQuery caseIdQuery = new TimestampQuery(tableName, ft.parse("1949-01-26"), ComparisionType.LT);
            String query = caseIdQuery.getQuery();
            System.out.println(query);
            ResultSet resultSet = Application.getConnection().prepareStatement(query).executeQuery();
            resultSet.next();
            System.out.println(resultSet.getObject("activity"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
