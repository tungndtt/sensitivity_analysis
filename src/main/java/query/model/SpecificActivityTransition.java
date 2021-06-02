package query.model;

import java.sql.ResultSet;

public class SpecificActivityTransition {

    private int caseId;

    private double average_transition_time;

    private SpecificActivityTransition(int caseId, double average_transition_time) {
        this.caseId = caseId;
        this.average_transition_time = average_transition_time;
    }

    public int getCaseId() {
        return this.caseId;
    }

    public double getAverage_transition_time() {
        return this.average_transition_time;
    }

    public static SpecificActivityTransition parseFrom(ResultSet resultSet) {
        try {
            int caseId = resultSet.getInt("caseid");
            double average_transition_time = resultSet.getDouble("average_transition_time");

            return new SpecificActivityTransition(caseId, average_transition_time);
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
