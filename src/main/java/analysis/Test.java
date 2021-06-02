package analysis;


import analysis.metric.CasePerVariantMetric;
import analysis.metric.CaseVarianceMetric;
import analysis.metric.Metric;
import analysis.variation.*;
import condition.ComparisionType;
import condition.Condition;
import query.common.CommonQuery;
import query.common.DurationPerCaseQuery;
import query.common.TimestampQuery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Test {
    public static void main(String[] args) throws ParseException {

        String tableName = "log_ieee";
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        //CommonQuery timestampQuery = new TimestampQuery(tableName, new Date[] {ft.parse("2018-05-15"), ft.parse("2018-5-16")}, true);

        CommonQuery timestampQuery = new TimestampQuery(tableName, ft.parse("2018-5-16"), ComparisionType.LT);

        Metric casePerVariantMetric = new CasePerVariantMetric();
        CaseVarianceMetric caseVarianceMetric = new CaseVarianceMetric();
        caseVarianceMetric.setCoefficients(new double[]{0.25, 0.25, 0.25, 0.25});

        Test.testAdaptiveVariation(timestampQuery, casePerVariantMetric);
    }

    // Test
    private static void testNaiveVariation(CommonQuery commonQuery, Metric metric) {
        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(commonQuery);
        naiveVariation.setMetric(metric);
        naiveVariation.setUnitAndNumberOfIterations(1440, 5);

        Test.testPrint(naiveVariation);
    }

    // Test
    private static void testAverageVariation(CommonQuery commonQuery, Metric metric) {
        AverageVariation averageVariation = new AverageVariation();
        averageVariation.setCommonQuery(commonQuery);
        averageVariation.setMetric(metric);
        averageVariation.setGammaAndIterations(0.001, 4);

        Test.testPrint(averageVariation);
    }

    // Test
    private static void testAdaptiveVariation(CommonQuery commonQuery, Metric metric) {
        AdaptiveVariation adaptiveVariation = new AdaptiveVariation();
        adaptiveVariation.setCommonQuery(commonQuery);
        adaptiveVariation.setMetric(metric);
        adaptiveVariation.setNumberOfIterations(11);
        adaptiveVariation.setInitialUnitAndAlpha(60, 1.5);

        Test.testPrint(adaptiveVariation);
    }

    private static void testPrint(Variation variation) {
        Condition condition = variation.getVaryingConditions().getFirst();

        LinkedList<Pair> result = variation.vary(condition.getAttribute());

        for(Pair pair : result) {
            System.out.println(pair.getCondition());
            for(String key : pair.getChangingRate().keySet()) {
                System.out.println(key + " has changing rate " + pair.getChangingRate().get(key));
            }
        }
    }
}
