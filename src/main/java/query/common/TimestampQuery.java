package query.common;

import analysis.variation.VariationType;
import condition.*;
import condition.value.DateValue;
import condition.value.IntervalValue;

import java.util.Date;
import java.util.HashMap;

public class TimestampQuery extends CommonQuery {

    private static String conditionAttribute = "t.time_stamp";

    public TimestampQuery(String selectFrom, Date[] interval, boolean inside) {
        super("Time stamp in/out-side interval filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        DateValue startDate = new DateValue(interval[0]);
        DateValue endDate = new DateValue(interval[1]);
        IntervalValue value = new IntervalValue(new DateValue[]{startDate, endDate});
        Condition condition = null;
        if(inside) {
            condition = new InIntervalCondition(TimestampQuery.conditionAttribute, value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition(TimestampQuery.conditionAttribute, value));
        }

        this.setCondition(condition);
    }

    public TimestampQuery(String selectFrom, Date date, ComparisionType comparisionType) {
        super("Time stamp comparision filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        DateValue value = new DateValue(date);
        CompareCondition compareCondition = new CompareCondition(TimestampQuery.conditionAttribute, value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().getCondition());
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> timestampQueries = new HashMap<>() {{
            put(VariationType.NAIVE, getMinMaxTimestamp());
            put(VariationType.AVERAGE, getAllTimestamps());
            put(VariationType.ADAPTIVE, getAllTimestamps());
        }};
        this.setAttributeValueSetQueries(
                new HashMap<>() {{
                    put(TimestampQuery.conditionAttribute, timestampQueries);
                }}
        );
    }

    private String getMinMaxTimestamp() {
        return String.format("select min(%s) as minimum, max(%s) as maximum from %s as t", TimestampQuery.conditionAttribute, TimestampQuery.conditionAttribute, this.getSelectFrom());
    }

    private String getAllTimestamps() {
        return String.format("select %s as element as duration from %s as t", TimestampQuery.conditionAttribute, this.getSelectFrom());
    }
}
