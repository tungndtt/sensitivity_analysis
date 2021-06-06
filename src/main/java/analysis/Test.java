package analysis;


import analysis.metric.CasePerVariantMetric;
import analysis.metric.CaseVarianceMetric;
import analysis.metric.Metric;
import analysis.metric.SpecificActivityTransitionPerCaseMetric;
import analysis.variation.*;
import condition.ComparisionType;
import condition.Condition;
import query.common.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Test {
    public static void main(String[] args) throws ParseException {

        String tableName = "log_ieee";

        // common queries
        // common query - timestamp
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        // in interval
        CommonQuery timestampIntervalQuery = new TimestampQuery(tableName, new Date[] {ft.parse("2018-05-15"), ft.parse("2018-11-22")}, true);
        // less than given timestamp
        CommonQuery timestampQuery = new TimestampQuery(tableName, ft.parse("2018-5-16"), ComparisionType.GT);

        // common query - resource
        // in list
        List<Object> resources = new LinkedList<>();
        CommonQuery resourceQuery = new ResourceQuery(tableName, resources, true);

        // common query - duration
        // greater than a month
        CommonQuery durationQuery = new DurationPerCaseQuery(tableName, 60*24*30, ComparisionType.GT);
        // within 1-2 months
        CommonQuery durationIntervalQuery = new DurationPerCaseQuery(tableName, new double[]{60*24*30, 60*24*30*2}, true);

        // common query - caseid
        // in list
        List<Object> caseIds = new LinkedList<>();
        CommonQuery caseidInListQuery = new CaseIdQuery(tableName, caseIds, true);
        // greater than 500
        CommonQuery caseIdCompareQuery = new CaseIdQuery(tableName, 500, ComparisionType.GTE);
        // in interval
        CommonQuery caseIdIntervalQuery = new CaseIdQuery(tableName, new int[]{244, 1998}, true);

        // metrics
        // case per variant metric
        Metric casePerVariantMetric = new CasePerVariantMetric();

        // case information metric
        Metric caseVarianceMetric = new CaseVarianceMetric();
        ((CaseVarianceMetric)caseVarianceMetric).setCoefficients(new double[]{0.25, 0.25, 0.25, 0.25});

        // specific activity transition time in cases metric
        Metric specificActivityTransitionPerCaseMetric = new SpecificActivityTransitionPerCaseMetric();
        ((SpecificActivityTransitionPerCaseMetric)specificActivityTransitionPerCaseMetric).setSpecificActivities("SRM: Created", "Clear Invoice");
        ((SpecificActivityTransitionPerCaseMetric)specificActivityTransitionPerCaseMetric).setMode(SpecificActivityTransitionPerCaseMetric.Mode.SEPARATE);


        // Run test
        Test.testAverageVariation(timestampQuery, specificActivityTransitionPerCaseMetric);
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
        averageVariation.setGammaAndIterations(100, 10);
        averageVariation.setDifferenceBound(60*24*30);

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

    // Test
    private static void testSetVariation(CommonQuery commonQuery, Metric metric) {
        SetVariation setVariation = new SetVariation();
        setVariation.setCommonQuery(commonQuery);
        setVariation.setMetric(metric);
        setVariation.setNumberOfIterationsAndUnit(8, 2);

        Test.testPrint(setVariation);
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
