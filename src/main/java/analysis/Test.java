package analysis;

import analysis.variation.NaiveVariation;
import query.common.TimestampQuery;

import java.util.Date;

public class Test {
    public static void main(String[] args) {
        String tableName = "log_ieee";
        Date[] range = {new Date("1949-01-26"), new Date("2019-02-28")};
        TimestampQuery caseIdQuery = new TimestampQuery(tableName, range, true);


        NaiveVariation naiveVariation = new NaiveVariation(caseIdQuery);

        Object[] objects = naiveVariation.getMinMaxRange(caseIdQuery.getCondition().getAttribute());

        System.out.println(objects[0]);
        System.out.println(objects[1]);

    }
}
