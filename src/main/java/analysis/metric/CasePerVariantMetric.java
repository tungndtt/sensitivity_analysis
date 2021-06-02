package analysis.metric;

import query.analysis.AnalysisQuery;
import query.analysis.CasesPerVariantQuery;
import query.common.CommonQuery;
import query.model.CasePerVariant;
import java.sql.ResultSet;
import java.util.HashMap;

public class CasePerVariantMetric extends Metric{
    public CasePerVariantMetric() {
        super("Calculate difference based on distribution of case activities variants");
        this.analysisQuery = new CasesPerVariantQuery();
    }


    @Override
    public Object analyze() {
        if(this.analysisQuery != null && this.analysisQuery.getCommonQuery() != null && this.getDatabaseConnection() != null) {
            String query = this.analysisQuery.getQuery();
            System.out.println(query);
            try {
                ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
                HashMap<String, Integer> variantDistribution = new HashMap<>();
                while (resultSet.next()) {
                    CasePerVariant casePerVariant = CasePerVariant.parseFrom(resultSet);
                    variantDistribution.put(casePerVariant.getVariant(), casePerVariant.getNumberOfCases());
                }
                return variantDistribution;
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return null;
    }

    @Override
    public double calculateDiff(Object obj1, Object obj2) {
        HashMap<String, Integer> o1 = (HashMap<String, Integer>) obj1;
        HashMap<String, Integer> o2 = (HashMap<String, Integer>) obj2;

        HashMap<String, int[]> variants = new HashMap<>();
        int numberOfCases_1 = 0, numberOfCases_2 = 0;

        for(String k1 : o1.keySet()) {
            numberOfCases_1 += o1.get(k1);
            int[] val = {o1.get(k1), 0};
            variants.put(k1, val);
        }

        for(String k2 : o2.keySet()) {
            numberOfCases_2 += o2.get(k2);
            if(variants.containsKey(k2)) {
                int[] val = variants.get(k2);
                val[1] = o2.get(k2);
            }
            else {
                int[] val = {0, o2.get(k2)};
                variants.put(k2, val);
            }
        }

        int n = variants.size();
        double[] p1 = new double[n], p2 = new double[n];

        for(int[] val : variants.values()) {
            p1[n-1] = val[0]*1.0/numberOfCases_1;
            p2[n-1] = val[1]*1.0/numberOfCases_2;
            --n;
        }

        return CasePerVariantMetric.jensenShannonDivergence(p1, p2);
    }

    /**
     * @source http://www.java2s.com/example/java/java.lang/returns-the-jensenshannon-divergence.html
     * @param p1
     * @param p2
     * Returns the value of similarity between 2 distributions
     */
    private static double jensenShannonDivergence(double[] p1, double[] p2) {
        assert (p1.length == p2.length);
        double[] average = new double[p1.length];
        for (int i = 0; i < p1.length; ++i) {
            average[i] += (p1[i] + p2[i]) / 2;
        }
        return (klDivergence(p1, average) + klDivergence(p2, average)) / 2;
    }

    /**
     * @source http://www.java2s.com/example/java/java.lang/returns-the-jensenshannon-divergence.html
     * Returns the KL divergence, K(p1 || p2).
     * The log is w.r.t. base 2.
     * Note: If any value in p2 is 0.0 then the KL-divergence
     * is infinite
     */
    private static double klDivergence(double[] p1, double[] p2) {
        assert (p1.length == p2.length);
        double klDiv = 0.0, log2 = Math.log(2);
        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0) {
                continue;
            }
            if (p2[i] == 0) {
                return Double.POSITIVE_INFINITY;
            }
            klDiv += p1[i] * Math.log(p1[i] / p2[i]);
        }
        return klDiv / log2; // moved this division out of the loop -DM
    }
}
