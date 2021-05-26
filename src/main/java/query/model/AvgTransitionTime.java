package query.model;

import java.sql.ResultSet;

public class AvgTransitionTime {

    private int caseId;

    private double average_transition_time;

    private AvgTransitionTime(int caseId, double average_transition_time) {
        this.caseId = caseId;
        this.average_transition_time = average_transition_time;
    }


    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public double getAverage_transition_time() {
        return average_transition_time;
    }

    public void setAverage_transition_time(double average_transition_time) {
        this.average_transition_time = average_transition_time;
    }

    public static AvgTransitionTime parseFrom(ResultSet resultSet) {
        try {
            int caseId = resultSet.getInt("caseId");
            double average_transition_time = resultSet.getDouble("average_transition");

            return new AvgTransitionTime(caseId, average_transition_time);
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
