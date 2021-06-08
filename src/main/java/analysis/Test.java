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
import java.util.Iterator;
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
        // not in list
        List<Object> resources = new LinkedList<>();
        resources.add("user_232");
        resources.add("user_000");
        resources.add("user_013");
        CommonQuery resourceQuery = new ResourceQuery(tableName, resources, false);

        // common query - activity
        // in list
        List<Object> activities = new LinkedList<>();
        activities.add("SRM: Created");
        activities.add("SRM: Complete");
        activities.add("Record Service Entry Sheet");
        activities.add("Vendor creates invoice");
        activities.add("Vendor creates debit memo");
        activities.add("Record Invoice Receipt");
        CommonQuery activityQuery = new ActivityQuery(tableName, activities, true);

        // common query - duration
        // greater than a month
        CommonQuery durationQuery = new DurationPerCaseQuery(tableName, 60*24*30, ComparisionType.GT);
        // within 1-2 months
        CommonQuery durationIntervalQuery = new DurationPerCaseQuery(tableName, new double[]{60*24, 60*24*30*4}, true);

        // common query - caseid
        // in list
        List<Object> caseIds = new LinkedList<>();
        for(int id=0; id<224; id++) {
            caseIds.add(id);
        }
        CommonQuery caseidInListQuery = new CaseIdQuery(tableName, caseIds, false);
        // greater than 500
        CommonQuery caseIdCompareQuery = new CaseIdQuery(tableName, 500, ComparisionType.GTE);
        // in interval
        CommonQuery caseIdIntervalQuery = new CaseIdQuery(tableName, new int[]{100, 19980}, true);

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
        Test.testNaiveVariation(caseIdIntervalQuery, caseVarianceMetric);
    }

    // Test
    private static void testNaiveVariation(CommonQuery commonQuery, Metric metric) {
        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(commonQuery);
        naiveVariation.setMetric(metric);
        naiveVariation.setUnitAndNumberOfIterations(1, 40);

        Test.testPrint(naiveVariation);
    }

    // Test
    private static void testAverageVariation(CommonQuery commonQuery, Metric metric) {
        AverageVariation averageVariation = new AverageVariation();
        averageVariation.setCommonQuery(commonQuery);
        averageVariation.setMetric(metric);
        averageVariation.setGammaAndIterations(1, 15);
        averageVariation.setDifferenceBound(60*24*30);

        Test.testPrint(averageVariation);
    }

    // Test
    private static void testAdaptiveVariation(CommonQuery commonQuery, Metric metric) {
        AdaptiveVariation adaptiveVariation = new AdaptiveVariation();
        adaptiveVariation.setCommonQuery(commonQuery);
        adaptiveVariation.setMetric(metric);
        adaptiveVariation.setNumberOfIterations(15);
        adaptiveVariation.setInitialUnitAndAlpha(1, 1.2);

        Test.testPrint(adaptiveVariation);
    }

    // Test
    private static void testSetVariation(CommonQuery commonQuery, Metric metric) {
        SetVariation setVariation = new SetVariation();
        setVariation.setCommonQuery(commonQuery);
        setVariation.setMetric(metric);
        setVariation.setNumberOfIterationsAndUnit(10, 1);

        Test.testPrint(setVariation);
    }

    private static void testPrint(Variation variation) {
        Condition condition = variation.getVaryingConditions().getFirst();

        LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> result = variation.vary(condition.getAttribute());
        if(result != null) {
            for(Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> part : result) {
                System.out.println(part.getValue1());
                String str = part.getValue2() > 0 ? " increasing " : " decreasing ";
                System.out.println(str + "....");

                Number number = part.getValue3().getValue1();
                Iterator<Number> numbers = part.getValue3().getValue2().iterator();
                Iterator<Double> diff_values = part.getValue3().getValue3().iterator();

                int count = 0;

                while(numbers.hasNext()) {
                    Number n = numbers.next();
                    Double diff = diff_values.next();

                    Number value = null;
                    if(n instanceof Double || number instanceof Double) {
                        value = n.doubleValue()*number.doubleValue();
                    }
                    else if(n instanceof Long || number instanceof Long){
                        value = n.intValue()*number.intValue();
                    }
                    else if(n instanceof Integer && n instanceof Integer) {
                        value = n.longValue()*number.longValue();
                    }
                    else {
                        System.out.println("Unsupported number type!");
                        return;
                    }
                    System.out.println("Iteration " + (++count) + str + " by " + value.toString() + " has changing rate = " + diff.toString());
                }
            }
        }
    }
}
