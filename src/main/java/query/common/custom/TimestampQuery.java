package query.common.custom;

import analysis.variation.VariationType;
import condition.*;
import query.common.DeterminableCommonQuery;
import query.common.abstract_custom.CompareQuery;
import query.common.abstract_custom.CustomQuery;
import query.common.abstract_custom.IntervalQuery;

import java.util.Date;
import java.util.HashMap;

public class TimestampQuery extends DeterminableCommonQuery {

    private static String conditionAttribute = "t.time_stamp";

    private CustomQuery customQuery;

    public TimestampQuery(String selectFrom, Date[] interval, boolean inside) {
        super("Time stamp in/out-side interval filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new IntervalQuery("Time stamp in/out-side interval filter query", TimestampQuery.conditionAttribute, selectFrom, interval, inside);

        this.setCondition(this.customQuery.getCondition());
    }

    public TimestampQuery(String selectFrom, Date date, ComparisionType comparisionType) {
        super("Time stamp comparision filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new CompareQuery("Time stamp comparision filter query", TimestampQuery.conditionAttribute, selectFrom, date, comparisionType);

        this.setCondition(this.customQuery.getCondition());
    }

    @Override
    public String getQuery() {
        return this.customQuery.getQuery();
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> timestampQueries = new HashMap<>() {{
            put(VariationType.NAIVE, getMinMaxTimestamp());
            put(VariationType.AVERAGE, getAllTimestamps());
            put(VariationType.ADAPTIVE, getMinMaxTimestamp());
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
        return String.format("select %s as element from %s as t", TimestampQuery.conditionAttribute, this.getSelectFrom());
    }

    @Override
    public HashMap<String, String> getBoundQueries() {
        return new HashMap<>() {{
            put(TimestampQuery.conditionAttribute, getMinMaxTimestamp());
        }};
    }
}
