package main;

import analysis.Pair;
import analysis.determination.Determinator;
import analysis.determination.Score;
import analysis.metric.CasePerVariantMetric;
import analysis.metric.CaseVarianceMetric;
import analysis.metric.Metric;
import analysis.metric.SpecificActivityTransitionPerCaseMetric;
import analysis.variation.*;
import component.condition.ComparisionType;
import component.condition.Condition;
import main.plot.Plot;
import query.analysis.SpecificActivityTransitionQuery;
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
        // Test import event log
        //Test.testInsertingEventLog();



        // Create common query and metric
        CommonQueryType commonQueryType = CommonQueryType.ACTIVITY_IN_LIST;
        CommonQuery commonQuery = Test.getCommonQuery(commonQueryType);

        MetricType metricType = MetricType.CASE_VARIANCE_METRIC;
        Metric metric = Test.getMetric(metricType);

        LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result = Test.testSetVariation(commonQuery, metric);
        // Run test
        Test.testPlotting(result);




        /*
        // Check runtime
        BenchMark benchMark = new BenchMark();
        benchMark.setFunction(() -> {
            Test.testNaiveVariation(commonQuery, metric);
        });
        long runtime = benchMark.runCounter(1);
        System.out.println(runtime);
         */

        //Test.printSufficientRange(0.226, 0.005, 30, (DeterminableCommonQuery) commonQuery, metric);

        /*
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("D:\\Spring\\sensitve_analysis\\samples\\query2.json");
            Request request = objectMapper.readValue(file, Request.class);
            Test.testPlotting((LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>>) request.action.getResult());
        } catch (IOException e) {
            System.out.println(e);
        }
         */

        Test.calculateScore(result);
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
                commonQuery = new DurationQuery(tableName, 60*24*25, ComparisionType.GT);
                break;
            case DURATION_INTERVAL:
                // within 0.5-4 months
                commonQuery = new DurationQuery(tableName, new double[]{60*24*15 - 25000, 60*24*30*4 + 25000}, true);
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
                ((SpecificActivityTransitionPerCaseMetric) metric).setSpecificActivities("Record Invoice Receipt", "Clear Invoice");
                ((SpecificActivityTransitionPerCaseMetric) metric).setMode(SpecificActivityTransitionPerCaseMetric.Mode.SEPARATE);
                ((SpecificActivityTransitionPerCaseMetric) metric).setAnalysisMode(SpecificActivityTransitionQuery.Mode.ORDER);
                break;
        }
        return metric;
    }

    // Test
    private static void testInsertingEventLog() {
        String log_ieee = "C:/Users/Tung Doan/Downloads/log_IEEE.xes.gz";
        String bpi2012 = "C:/Users/Tung Doan/Downloads/BPI_Challenge_2012.xes.gz";
        boolean success = XLogUtil.insertIntoDatabase(bpi2012);
        if (success) {
            System.out.println("Successfully importing the event log!");
        }
        else {
            System.out.println("Fail to import the event log!");
        }
    }

    // Test
    private static LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> testNaiveVariation(CommonQuery commonQuery, Metric metric) {
        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(commonQuery);
        naiveVariation.setMetric(metric);
        naiveVariation.setUnitAndNumberOfIterations(100, 150);

        Condition condition = naiveVariation.getVaryingConditions().getFirst();
        return naiveVariation.vary(condition.getAttribute().toString());
    }

    // Test
    private static LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> testAverageVariation(CommonQuery commonQuery, Metric metric) {
        AverageVariation averageVariation = new AverageVariation();
        averageVariation.setCommonQuery(commonQuery);
        averageVariation.setMetric(metric);
        averageVariation.setGammaAndIterations(20, 150);
        averageVariation.setDifferenceBound(60*24*30);

        Condition condition = averageVariation.getVaryingConditions().getFirst();
        return averageVariation.vary(condition.getAttribute().toString());
    }

    // Test
    private static LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> testAdaptiveVariation(CommonQuery commonQuery, Metric metric) {
        AdaptiveVariation adaptiveVariation = new AdaptiveVariation();
        adaptiveVariation.setCommonQuery(commonQuery);
        adaptiveVariation.setMetric(metric);
        adaptiveVariation.setNumberOfIterations(80);
        adaptiveVariation.setInitialUnitAndAlpha(100, 1.06);

        Condition condition = adaptiveVariation.getVaryingConditions().getFirst();
        return adaptiveVariation.vary(condition.getAttribute().toString());
    }

    // Test
    private static LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> testGammaVariation(CommonQuery commonQuery, Metric metric) {
        GammaVariation gammaVariation = new GammaVariation();
        gammaVariation.setCommonQuery(commonQuery);
        gammaVariation.setMetric(metric);
        gammaVariation.setNumberOfIterations(60);
        gammaVariation.setUnitAndAlpha(80, 1.1);
        gammaVariation.setThreshold(0.03);

        Condition condition = gammaVariation.getVaryingConditions().getFirst();
        return gammaVariation.vary(condition.getAttribute().toString());
    }

    // Test
    private static LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> testSetVariation(CommonQuery commonQuery, Metric metric) {
        SetVariation setVariation = new SetVariation();
        setVariation.setCommonQuery(commonQuery);
        setVariation.setMetric(metric);
        setVariation.setNumberOfIterationsAndUnit(50, 1);

        Condition condition = setVariation.getVaryingConditions().getFirst();
        return setVariation.vary(condition.getAttribute().toString());
    }

    private static void testPlotting(LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> information) {
        if(information == null) return;

        List<Number[]> points = new LinkedList<>();

        String title = null;

        for(Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> part : information) {
            title = "Varying " + part.getValue1();

            Iterator<Number> variationSizes = part.getValue2().getValue1().iterator();
            Iterator<Double> changingRates = part.getValue2().getValue2().iterator();

            while(variationSizes.hasNext()) {
                Number vs = variationSizes.next();
                Double cr = changingRates.next();

                //System.out.println(value + " " +diff);
                points.add(new Number[]{vs, cr});
            }
        }
        new Plot(title, "changing rate", "variation size", "difference", points).display();
    }

    private static void testPrinting(LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        if(result != null) {
            for(Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> part : result) {
                System.out.println(part.getValue1());
                System.out.println("variation size ~ changing rate ....");

                Iterator<Number> variationSizes = part.getValue2().getValue1().iterator();
                Iterator<Double> changingRates = part.getValue2().getValue2().iterator();

                int count = 0;

                while(variationSizes.hasNext()) {
                    Number vs = variationSizes.next();
                    Double cr = changingRates.next();

                    System.out.println("Iteration " + (++count) + " varying by " + vs.toString() + " has changing rate = " + cr.toString());
                }
            }
        }
    }

    private static void printSufficientRange(double differenceBound, double differenceTolerance, Number precisionTolerance, DeterminableCommonQuery determinableCommonQuery, Metric metric) {
        Determinator determinator = new Determinator();
        determinator.setDeterminableCommonQuery(determinableCommonQuery);
        determinator.setMetric(metric);
        metric.setCommonQuery(determinableCommonQuery);
        determinator.setDifferenceBound(differenceBound);
        determinator.setTolerance(differenceTolerance, precisionTolerance);

        Condition condition = determinableCommonQuery.retrieveAllConditionsWithValue().getFirst();

        LinkedList<Pair<String, Object[]>> result = determinator.determine(condition.getAttribute().toString());

        System.out.println(result);
        for(Pair<String, Object[]> pair : result) {
            System.out.println(pair.getValue1());
            System.out.println("Determining ...");

            Object[] objects = pair.getValue2();
            System.out.println("Minimal variation size " + objects[1].toString() +  " has difference= " + objects[0].toString());
            System.out.println("Maximal variation size " + objects[3].toString() +  " has difference= " + objects[2].toString());
        }
    }

    private static void calculateScore(LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        LinkedList<Number[]> variations = new LinkedList<>();

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> pair = result.get(0);

        Iterator<Number> variationSizes = pair.getValue2().getValue1().iterator();
        Iterator<Double> changingRates = pair.getValue2().getValue2().iterator();

        while(variationSizes.hasNext()) {
            Number vs = variationSizes.next();
            Double cr = changingRates.next();
            variations.add(new Number[]{vs, cr});
        }

        pair = result.get(1);
        variationSizes = pair.getValue2().getValue1().iterator();
        changingRates = pair.getValue2().getValue2().iterator();

        while(variationSizes.hasNext()) {
            Number vs = variationSizes.next();
            Double cr = changingRates.next();
            variations.addFirst(new Number[]{vs, cr});
        }

        System.out.println("Score = " + Score.calculateScore(variations, 0.95));
    }
}
