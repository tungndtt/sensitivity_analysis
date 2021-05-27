package xlog;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import main.Application;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.*;

/**
 * @author Tung Doan
 *
 * Huge thanks to Dr.Alexander Seeliger
 * XLogUtil class extracts the information from event log and saves into the database
 * It supports event log files in format .xes.gz or .xes
 */
public class XLogUtil {

    private static String[] supportedFiles = {".xes.gz", ".xes"};

    /**
     * @author Dr.Alexander Seeliger
     *
     * Reads a log from the file system. (xes and xes.gz files are supported)
     * @param filePath
     * @return parsed XLog instance
     */
    private static XLog parseLog(String filePath) {
        XesXmlParser parser = null;

        if(filePath.endsWith(supportedFiles[0])) {
            parser = new XesXmlGZIPParser(new XFactoryBufferedImpl());
        }
        else if(filePath.endsWith(supportedFiles[1])) {
            parser = new XesXmlParser(new XFactoryBufferedImpl());
        }
        else {
            System.out.println("Unsupported file");
            return null;
        }

        try {
            List<XLog> xlogs = parser.parse(new File(filePath));
            return xlogs.get(0);
        }
        catch(Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private static String getTypeOfAttribute(XAttribute attribute) {
        if(attribute instanceof XAttributeBoolean) {
            return "boolean";
        }
        else if(attribute instanceof XAttributeDiscrete) {
            return "int";
        }
        else if(attribute instanceof XAttributeContinuous) {
            return "double precision";
        }
        else if(attribute instanceof XAttributeLiteral) {
            return "varchar(100)";
        }
        else if(attribute instanceof XAttributeTimestamp) {
            return "date";
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param attribute
     * @return
     */
    private static Object getValueOfAttribute(XAttribute attribute) {
        if(attribute instanceof XAttributeBoolean) {
            return ((XAttributeBoolean)attribute).getValue();
        }
        else if(attribute instanceof XAttributeDiscrete) {
            return ((XAttributeDiscrete)attribute).getValue();
        }
        else if(attribute instanceof XAttributeContinuous) {
            return ((XAttributeContinuous)attribute).getValue();
        }
        else if(attribute instanceof XAttributeLiteral) {
            return ((XAttributeLiteral)attribute).getValue();
        }
        else if(attribute instanceof XAttributeTimestamp) {
            XAttributeTimestamp xAttributeTimestamp = (XAttributeTimestamp)attribute;
            return new Date(xAttributeTimestamp.getValue().getTime());
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static boolean insertIntoDatabase(String filePath) {
        XLog xlog = XLogUtil.parseLog(filePath);

        HashMap<String, String> columns = new HashMap<>();
        HashMap<String, Integer> columnIndices = new HashMap<>();
        HashSet<String> importantColumns = new HashSet<>() {{
           add(XConceptExtension.KEY_NAME);
           add(XTimeExtension.KEY_TIMESTAMP);
           add(XOrganizationalExtension.KEY_RESOURCE);
           add(XOrganizationalExtension.KEY_GROUP);
        }};

        if(xlog != null) {
            Connection dbConnection = Application.getConnection();
            try {
                XLogInfo logInfo = XLogInfoFactory.createLogInfo(xlog);

                int colIndex = 6;
                for(XAttribute column : logInfo.getEventAttributeInfo().getAttributes()) {
                    if(!importantColumns.contains(column.getKey())) {
                        String type = XLogUtil.getTypeOfAttribute(column);
                        if(type != null) {
                            columns.put(column.getKey(), type);
                            columnIndices.put(column.getKey(), colIndex++);
                        }
                        else System.out.println("Type of attribute " + column.getKey() + " is unsupported!");
                    }
                }

                String logName = XLogUtil.getFileName(filePath);

                dbConnection.createStatement().executeUpdate("drop table if exists " + logName);

                String createQuery = "create table " + logName + "( caseId int, activity varchar(100), time_stamp date, resource varchar(100), group_name varchar(100)";

                for(String column : columns.keySet()) {
                    createQuery += ", col_" + columnIndices.get(column) + " " + columns.get(column);
                }
                createQuery += " )";

                System.out.println(createQuery);
                dbConnection.createStatement().executeUpdate(createQuery);


                int numberOfColumns = 5 + columns.size();

                String insertQuery = "insert into " + logName + " values ( " + XLogUtil.repeat("?", ",", numberOfColumns) + " )";
                System.out.println(insertQuery);

                PreparedStatement insertStatement = dbConnection.prepareStatement(insertQuery);

                int caseId = 1;
                for(XTrace trace : xlog) {
                    for(XEvent event : trace) {
                        insertStatement.setObject(1, caseId);
                        insertStatement.setObject(2, event.getAttributes().containsKey(XConceptExtension.KEY_NAME) ? event.getAttributes().get(XConceptExtension.KEY_NAME).toString() : null);
                        java.sql.Timestamp time_stamp = null;
                        if(event.getAttributes().containsKey(XTimeExtension.KEY_TIMESTAMP)) {
                            XAttributeTimestamp xAttributeTimestamp = (XAttributeTimestamp)(event.getAttributes().get(XTimeExtension.KEY_TIMESTAMP));
                            time_stamp = new java.sql.Timestamp(xAttributeTimestamp.getValue().getTime());
                        }
                        insertStatement.setObject(3,  time_stamp);
                        insertStatement.setObject(4, event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE) ? event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString() : null);
                        insertStatement.setObject(5, event.getAttributes().containsKey(XOrganizationalExtension.KEY_GROUP) ? event.getAttributes().get(XOrganizationalExtension.KEY_GROUP).toString() : null);

                        for(String column : columns.keySet()) {
                            insertStatement.setObject(columnIndices.get(column), XLogUtil.getValueOfAttribute(event.getAttributes().get(column)));
                        }
                        insertStatement.addBatch();
                    }
                    ++caseId;
                }

                insertStatement.executeBatch();


                return true;
            } catch (SQLException e) {
                System.out.println(e);
                return false;
            }
        }
        else {
            System.out.println("Cannot parse to XLog instance from file!");
            return false;
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    private static String getFileName(String filePath) {
        String[] splices = filePath.split("/");
        int n = splices.length;
        String file = splices[n-1];
        for(String supportedFile : XLogUtil.supportedFiles) {
            if(file.endsWith(supportedFile)) {
                return file.substring(0, file.length() - supportedFile.length());
            }
        }
        return null;
    }

    /**
     *
     * @param repeater
     * @param separator
     * @param number
     * @return desired string with number-times repeated and separated by separator
     */
    private static String repeat(String repeater, String separator, int number) {
        if(number > 0) {
            StringBuilder stringBuilder = new StringBuilder(repeater);
            --number;

            while (number > 0) {
                stringBuilder.append(separator + repeater);
                --number;
            }

            return stringBuilder.toString();
        }
        else return null;
    }
}

