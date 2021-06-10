package analysis.metric;

import query.analysis.CaseInfoQuery;
import query.model.CaseInfo;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Implement the metric to calculate the difference between 2 event logs based on the general information in cases
 * (number of activities, number of resources, duration, average transition time between 2 activities)
 *
 * @author Tung Doan
 */
public class CaseVarianceMetric extends Metric{

    /**
     * coefficients = [alpha, beta, theta, gamma] with alpha + beta + theta + gamma = 1.0
     */
    private double[] coefficients;

    public CaseVarianceMetric() {
        super("Calculate the variance (difference) per case metric");
        this.analysisQuery = new CaseInfoQuery();
    }

    @Override
    public Object analyze() {
        if(this.analysisQuery != null && this.analysisQuery.getCommonQuery() != null && this.getDatabaseConnection() != null) {
            String query = this.analysisQuery.getQuery();
            //System.out.println(query);
            try {
                ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
                HashMap<Integer, double[]> cases = new HashMap<>();
                while (resultSet.next()) {
                    CaseInfo caseInfo = CaseInfo.parseFrom(resultSet);
                    double[] info = new double[]{
                            caseInfo.getNumberOfActivities(),
                            caseInfo.getNumberOfResources(),
                            caseInfo.getAverage_transition_time(),
                            caseInfo.getDuration()
                    };
                    cases.put(caseInfo.getCaseId(), info);
                }

                return cases;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public double[] getCoefficients() {
        return this.coefficients;
    }

    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }

    @Override
    public double calculateDiff(Object obj1, Object obj2) {
        double diff = 0;
        int numberOfCases = 0;
        HashMap<Integer, double[]> o1 = (HashMap<Integer, double[]>) obj1;
        HashMap<Integer, double[]> o2 = (HashMap<Integer, double[]>) obj2;

        for(int caseId : o1.keySet()) {
            double[] v1 = o1.get(caseId);
            if(o2.containsKey(caseId)) {
                double[] v2 = o2.get(caseId);
                for(int i=0; i<this.coefficients.length; i++) {
                    diff += v1[i] != 0 || v2[i] != 0 ? this.coefficients[i]*Math.abs(v1[i] - v2[i])/Math.max(v1[i], v2[i]) : 0;
                }
            }
            else {
                for(int i=0; i<this.coefficients.length; i++) {
                    diff += v1[i] != 0 ? this.coefficients[i] : 0;
                }
            }
            ++numberOfCases;
        }

        for(int caseId : o2.keySet()) {
            if(!o1.containsKey(caseId)) {
                double[] v2 = o2.get(caseId);
                for(int i=0; i<this.coefficients.length; i++) {
                    diff += v2[i] != 0 ? this.coefficients[i] : 0;
                }
                ++numberOfCases;
            }
        }

        return diff / numberOfCases;
    }
}
