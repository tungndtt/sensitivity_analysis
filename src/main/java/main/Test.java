package main;

import analysis.Pair;
import analysis.metric.CasePerVariantMetric;
import analysis.metric.CaseVarianceMetric;
import analysis.metric.Metric;
import analysis.metric.SpecificActivityTransitionPerCaseMetric;
import analysis.variation.*;
import condition.ComparisionType;
import condition.Condition;
import main.benchmark.BenchMark;
import main.plot.Plot;
import query.common.*;
import query.common.custom.*;
import xlog.XLogUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Test implemented variation algorithms and metrics
 *
 * @author Tung Doan
 */
public class Test {

    private static String tableName = "log_ieee";

    public static void main(String[] args) throws ParseException {

        // Create common query and metric
        CommonQueryType commonQueryType = CommonQueryType.TIMESTAMP_INTERVAL;
        CommonQuery commonQuery = Test.getCommonQuery(commonQueryType);

        MetricType metricType = MetricType.CASE_VARIANT_METRIC;
        Metric metric = Test.getMetric(metricType);

        // Run test
        //Test.testPlotting(Test.testAverageVariation(commonQuery, metric));

        // Check runtime
        BenchMark benchMark = new BenchMark();
        benchMark.setFunction(() -> {
            Test.testNaiveVariation(commonQuery, metric);
        });
        long runtime = benchMark.runCounter(1);
        System.out.println(runtime);
    }

    private enum CommonQueryType {
        TIMESTAMP_INTERVAL,
        TIMESTAMP_COMPARE,
        RESOURCE_IN_LIST,
        ACTIVITY_IN_LIST,
        DURATION_COMPARE,
        DURATION_INTERVAL,
        CASE_IN_LIST,
        CASE_COMPARE,
        CASE_INTERVAL
    }

    // common queries
    private static CommonQuery getCommonQuery(CommonQueryType type) throws ParseException {
        CommonQuery commonQuery = null;
        switch (type) {

            // common query - timestamp
            case TIMESTAMP_INTERVAL:
                // in interval
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
                commonQuery = new TimestampQuery(tableName, new Date[] {ft.parse("2017-06-15"), ft.parse("2019-01-12")}, true);
                break;
            case TIMESTAMP_COMPARE:
                // less than given timestamp
                ft = new SimpleDateFormat("yyyy-MM-dd");
                commonQuery = new TimestampQuery(tableName, ft.parse("2018-10-16"), ComparisionType.GT);
                break;

            // common query - resource
            case RESOURCE_IN_LIST:
                // not in list
                List<Object> resources = new LinkedList<>();
                resources.add("user_232");
                resources.add("user_000");
                resources.add("user_013");
                commonQuery = new ResourceQuery(tableName, resources, false);
                break;

            // common query - activity
            case ACTIVITY_IN_LIST:
                // in list
                List<Object> activities = new LinkedList<>();
                activities.add("SRM: Created");
                activities.add("SRM: Complete");
                activities.add("Record Service Entry Sheet");
                activities.add("Vendor creates invoice");
                activities.add("Vendor creates debit memo");
                activities.add("Record Invoice Receipt");
                commonQuery = new ActivityQuery(tableName, activities, false);
                break;

            // common query - duration
            case DURATION_COMPARE:
                // greater than a month
                commonQuery = new DurationPerCaseQuery(tableName, 60*24*45, ComparisionType.GT);
                break;
            case DURATION_INTERVAL:
                // within 0.5-4 months
                commonQuery = new DurationPerCaseQuery(tableName, new double[]{60*24*15, 60*24*30*4}, true);
                break;

            // common query - caseid
            case CASE_IN_LIST:
                // in list
                List<Object> caseIds = new LinkedList<>();
                for(int id=0; id<224; id++) {
                    caseIds.add(id);
                }
                commonQuery = new CaseIdQuery(tableName, caseIds, false);
                break;
            case CASE_COMPARE:
                // greater than 500
                commonQuery = new CaseIdQuery(tableName, 500, ComparisionType.GTE);
                break;
            case CASE_INTERVAL:
                // in interval
                commonQuery = new CaseIdQuery(tableName, new int[]{100, 19980}, true);
                break;
        }
        return commonQuery;
    }

    private enum MetricType {
        CASE_VARIANT_METRIC,
        CASE_VARIANCE_METRIC,
        SPECIFIC_ACTIVITY_TRANSITION_METRIC
    }

    // metrics
    private static Metric getMetric(MetricType type) {

        Metric metric = null;

        switch (type) {
            // case per variant metric
            case CASE_VARIANT_METRIC:
                metric = new CasePerVariantMetric();
                break;

            // case information metric
            case CASE_VARIANCE_METRIC:
                metric = new CaseVarianceMetric();
                ((CaseVarianceMetric) metric).setCoefficients(new double[]{0.25, 0.25, 0.25, 0.25});
                break;

            // specific activity transition time in cases metric
            case SPECIFIC_ACTIVITY_TRANSITION_METRIC:
                metric = new SpecificActivityTransitionPerCaseMetric();
                ((SpecificActivityTransitionPerCaseMetric) metric).setSpecificActivities("SRM: Created", "Clear Invoice");
                ((SpecificActivityTransitionPerCaseMetric) metric).setMode(SpecificActivityTransitionPerCaseMetric.Mode.SEPARATE);
                break;
        }
        return metric;
    }

    // Test
    private static void testInsertingEventLog() {
        boolean success = XLogUtil.insertIntoDatabase("C:/Users/Tung Doan/Downloads/log_IEEE.xes.gz");
        if (success) {
            System.out.println("Successfully importing the event log!");
        }
        else {
            System.out.println("Fail to import the event log!");
        }
    }

    // Test
    private static LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> testNaiveVariation(CommonQuery commonQuery, Metric metric) {
        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(commonQuery);
        naiveVariation.setMetric(metric);
        naiveVariation.setUnitAndNumberOfIterations(100, 150);

        Condition condition = naiveVariation.getVaryingConditions().getFirst();
        return naiveVariation.vary(condition.getAttribute());
    }

    // Test
    private static LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> testAverageVariation(CommonQuery commonQuery, Metric metric) {
        AverageVariation averageVariation = new AverageVariation();
        averageVariation.setCommonQuery(commonQuery);
        averageVariation.setMetric(metric);
        averageVariation.setGammaAndIterations(20, 150);
        averageVariation.setDifferenceBound(60*24*30);

        Condition condition = averageVariation.getVaryingConditions().getFirst();
        return averageVariation.vary(condition.getAttribute());
    }

    // Test
    private static LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> testAdaptiveVariation(CommonQuery commonQuery, Metric metric) {
        AdaptiveVariation adaptiveVariation = new AdaptiveVariation();
        adaptiveVariation.setCommonQuery(commonQuery);
        adaptiveVariation.setMetric(metric);
        adaptiveVariation.setNumberOfIterations(60);
        adaptiveVariation.setInitialUnitAndAlpha(100, 1.1);

        Condition condition = adaptiveVariation.getVaryingConditions().getFirst();
        return adaptiveVariation.vary(condition.getAttribute());
    }

    // Test
    private static LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> testSetVariation(CommonQuery commonQuery, Metric metric) {
        SetVariation setVariation = new SetVariation();
        setVariation.setCommonQuery(commonQuery);
        setVariation.setMetric(metric);
        setVariation.setNumberOfIterationsAndUnit(50, 1);

        Condition condition = setVariation.getVaryingConditions().getFirst();
        return setVariation.vary(condition.getAttribute());
    }

    private static void testPlotting(LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> information) {
        if(information == null) return;

        List<Number[]> points = new LinkedList<>();

        String title = null;

        for(Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> part : information) {
            title = "Varying " + part.getValue1();
            int sign = part.getValue2();

            Number number = part.getValue3().getValue1();
            Iterator<Number> numbers = part.getValue3().getValue2().iterator();
            Iterator<Double> diff_values = part.getValue3().getValue3().iterator();

            while(numbers.hasNext()) {
                Number n = numbers.next();
                Double diff = diff_values.next();

                Number value = null;
                if(n instanceof Double || number instanceof Double) {
                    value = n.doubleValue()*number.doubleValue()*sign;
                }
                else if(n instanceof Long || number instanceof Long){
                    value = n.intValue()*number.intValue()*sign;
                }
                else if(n instanceof Integer && n instanceof Integer) {
                    value = n.longValue()*number.longValue()*sign;
                }
                else {
                    System.out.println("Unsupported number type!");
                    return;
                }
                //System.out.println(value + " " +diff);
                points.add(new Number[]{value, diff});
            }
        }
        new Plot(title, "changing rate", "variation size", "difference", points).display();
    }

    private static void testPrinting(LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> result) {
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
