package query.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CaseInfo {

    private int caseId;

    private int numberOfActivities;

    private int getNumberOfResources;

    private double duration;

    private double average_transition_time;

    private CaseInfo(int caseId, int numberOfActivities, int getNumberOfResources, double duration, double average_transition_time) {
        this.caseId = caseId;
        this.numberOfActivities = numberOfActivities;
        this.getNumberOfResources = getNumberOfResources;
        this.duration = duration;
        this.average_transition_time = average_transition_time;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getNumberOfActivities() {
        return numberOfActivities;
    }

    public void setNumberOfActivities(int numberOfActivities) {
        this.numberOfActivities = numberOfActivities;
    }

    public int getGetNumberOfResources() {
        return getNumberOfResources;
    }

    public void setGetNumberOfResources(int getNumberOfResources) {
        this.getNumberOfResources = getNumberOfResources;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getAverage_transition_time() {
        return average_transition_time;
    }

    public void setAverage_transition_time(double average_transition_time) {
        this.average_transition_time = average_transition_time;
    }

    public static CaseInfo parseFrom(ResultSet resultSet) {
        try {
            int caseId = resultSet.getInt("caseid");
            int numberOfActivities = resultSet.getInt("number_of_activities");
            int numberOfResources = resultSet.getInt("number_of_resources");
            double duration = resultSet.getDouble("duration");
            double average_transition_time = resultSet.getDouble("average_transition_time");

            return new CaseInfo(caseId, numberOfActivities, numberOfResources, duration, average_transition_time);
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }
}
