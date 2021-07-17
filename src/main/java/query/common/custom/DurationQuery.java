package query.common.custom;

import analysis.variation.VariationType;
import component.attribute.Attribute;
import component.condition.*;
import component.value.IntervalValue;
import component.value.NumericalValue;
import component.value.Value;
import query.common.DeterminableCommonQuery;

import java.util.HashMap;

public class DurationQuery extends DeterminableCommonQuery {

    private static String conditionAttribute = "(extract(epoch from max(t.time_stamp)) - extract(epoch from min(t.time_stamp)))/60";

    public DurationQuery(String selectFrom, double[] interval, boolean inside) {
        super("Duration per case in/out-side interval filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        Condition condition = null;
        Value[] _interval = new Value[]{
                new NumericalValue(interval[0]), new NumericalValue(interval[1])
        };
        IntervalValue value = new IntervalValue(_interval);

        if(inside) {
            condition = new InIntervalCondition(new Attribute(DurationQuery.conditionAttribute, null), value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition(new Attribute(DurationQuery.conditionAttribute, null), value));
        }

        this.setCondition(condition);
    }

    public DurationQuery(String selectFrom, double number, ComparisionType comparisionType) {
        super("Duration per case comparision filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        NumericalValue value = new NumericalValue(number);
        CompareCondition compareCondition = new CompareCondition(new Attribute(DurationQuery.conditionAttribute, null), value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select t1.* from %s as t1 right join (select distinct caseId from %s as t group by caseid having %s) as t2 on t1.caseId = t2.caseId",
                this.getSelectFrom(), this.getSelectFrom(), this.getCondition().getCondition());
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> durationPerCaseQueries = new HashMap<>() {{
            put(VariationType.NAIVE, getMinMaxDurationPerCase());
            put(VariationType.AVERAGE, getAllDurationPerCase());
            put(VariationType.ADAPTIVE, getMinMaxDurationPerCase());
        }};
        this.setAttributeValueSetQueries(
                new HashMap<>() {{
                    put(DurationQuery.conditionAttribute, durationPerCaseQueries);
                }}
        );
    }

    private String getMinMaxDurationPerCase() {
        return String.format("select min(t.duration) as minimum, max(t.duration) as maximum from (select %s as duration from %s as t group by caseid) as t", DurationQuery.conditionAttribute, this.getSelectFrom());
    }

    private String getAllDurationPerCase() {
        return String.format("select %s as element from %s as t group by caseid", DurationQuery.conditionAttribute, this.getSelectFrom());
    }

    @Override
    public HashMap<String, String> getBoundQueries() {
        return new HashMap<>() {{
            put(DurationQuery.conditionAttribute, getMinMaxDurationPerCase());
        }};
    }
}
