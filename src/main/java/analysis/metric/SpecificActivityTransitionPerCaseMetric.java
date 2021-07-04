package analysis.metric;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import query.analysis.SpecificActivityTransitionQuery;
import query.model.SpecificActivityTransition;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Implement metric to calculate the difference between 2 event logs based on the transition time between 2 specific activities
 * This transition time between the 2 specific activities can be AGGREGATED or SEPARATED in analysis
 *
 * @author Tung Doan
 */
public class SpecificActivityTransitionPerCaseMetric extends Metric{

    @JsonProperty("start")
    private String startActivity;

    @JsonProperty("end")
    private  String endActivity;

    public enum Mode {
        SEPARATE("separate"),
        AGGREGATE("aggregate");

        String mode;

        Mode(String mode) {
            this.mode = mode;
        }

        @JsonCreator
        public static Mode forValue(String mode) {
            if(mode.equals(SEPARATE.mode)) {
                return SEPARATE;
            }
            else if(mode.equals(AGGREGATE.mode)) {
                return AGGREGATE;
            }
            else {
                return null;
            }
        }
    }

    @JsonProperty("mode")
    private Mode mode;

    public SpecificActivityTransitionPerCaseMetric() {
        super(MetricType.SATPCM);
        this.analysisQuery = new SpecificActivityTransitionQuery();
    }

    public void setSpecificActivities(String startActivity, String endActivity) {
        this.startActivity = startActivity;
        this.endActivity = endActivity;
    }

    public void setAnalysisMode(SpecificActivityTransitionQuery.Mode mode) {
        if(this.getAnalysisQuery() != null) {
            ((SpecificActivityTransitionQuery) this.getAnalysisQuery()).setMode(mode);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Object analyze() {
        if(this.getDatabaseConnection() != null && this.getAnalysisQuery().getCommonQuery() != null && this.startActivity != null && this.endActivity != null) {
            ((SpecificActivityTransitionQuery)this.getAnalysisQuery()).setActivities(this.startActivity, this.endActivity);
            String query = this.getAnalysisQuery().getQuery();
            //System.out.println(query);
            try {
                ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();

                HashMap<Integer, Double> separate_result = new HashMap<>();
                double aggregate_result = 0.0;
                int numberOfRows = 0;

                while (resultSet.next()) {
                    SpecificActivityTransition specificActivityTransition = SpecificActivityTransition.parseFrom(resultSet);

                    if (specificActivityTransition != null) {
                        if(this.mode == Mode.SEPARATE) {
                            separate_result.put(specificActivityTransition.getCaseId(), specificActivityTransition.getAverage_transition_time());
                        }
                        else {
                            aggregate_result += specificActivityTransition.getAverage_transition_time();
                        }
                        ++numberOfRows;
                    }
                }

                return this.mode == Mode.SEPARATE ? separate_result : numberOfRows != 0 ? aggregate_result / numberOfRows : 0.0;
            } catch (SQLException e) {
                System.out.println(e);
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public double calculateDiff(Object obj1, Object obj2) {
        if(this.mode == Mode.AGGREGATE) {
            double o1 = (Double) obj1;
            double o2 = (Double) obj2;

            return o1 != 0 || o2 != 0 ? Math.abs(o1 - o2) / Math.max(o1, o2) : 0.0;
        }
        else {
            HashMap<Integer, Double> o1 = (HashMap<Integer, Double>) obj1;
            HashMap<Integer, Double> o2 = (HashMap<Integer, Double>) obj2;

            double result = 0;
            int numberOfCases = 0;

            for(int k1 : o1.keySet()) {
                if(o2.containsKey(k1)) {
                    double v1 = o1.get(k1), v2 = o2.get(k1);
                    result += v1 != 0 && v2 != 0 ? Math.abs(v1 - v2)*2 / (v1 + v2) : v1 != v2 ? 1.0 : 0.0;
                }
                else {
                    result += 1;
                }
                ++numberOfCases;
            }

            for(int k2 : o2.keySet()) {
                if(!o1.containsKey(k2)) {
                    result += 1;
                    ++numberOfCases;
                }
            }

            return result / numberOfCases;
        }
    }
}
