package analysis;


import analysis.metric.CasePerVariantMetric;
import analysis.metric.CaseVarianceMetric;
import analysis.metric.Metric;
import analysis.variation.AverageVariation;
import analysis.variation.NaiveVariation;
import analysis.variation.Pair;
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
        /*
        String tableName = "log_ieee";
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        //CommonQuery timestampQuery = new TimestampQuery(tableName, new Date[] {ft.parse("2018-05-15"), ft.parse("2018-5-16")}, true);

        CommonQuery timestampQuery = new TimestampQuery(tableName, ft.parse("2018-5-16"), ComparisionType.LT);

        Metric casePerVariantMetric = new CasePerVariantMetric();
        CaseVarianceMetric caseVarianceMetric = new CaseVarianceMetric();
        caseVarianceMetric.setCoefficients(new double[]{0.25, 0.25, 0.25, 0.25});


        AverageVariation averageVariation = new AverageVariation();
        averageVariation.setCommonQuery(timestampQuery);
        averageVariation.setMetric(caseVarianceMetric);
        averageVariation.setGammaAndIterations(0.001, 4);

        Condition condition = averageVariation.getVaryingConditions().getFirst();

        LinkedList<Pair> result = averageVariation.vary(condition.getAttribute());

        /*
        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(timestampQuery);
        naiveVariation.setMetric(casePerVariantMetric);
        //naiveVariation.setMetric(caseVarianceMetric);
        naiveVariation.setUnitAndNumberOfIterations(1440, 5);

        Condition condition = naiveVariation.getVaryingConditions().getFirst();

        LinkedList<Pair> result = naiveVariation.vary(condition.getAttribute());


        for(Pair pair : result) {
            System.out.println(pair.getCondition());
            for(String key : pair.getChangingRate().keySet()) {
                System.out.println(key + " has changing rate " + pair.getChangingRate().get(key));
            }
        }
        */
        Object obj = new LinkedList<Integer>();
        System.out.println(((List<Object>) obj).size());
    }
}
