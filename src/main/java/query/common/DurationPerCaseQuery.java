package query.common;

import condition.*;
import condition.value.IntervalValue;
import condition.value.NummericalValue;

public class DurationPerCaseQuery extends CommonQuery {
    public DurationPerCaseQuery(String selectFrom, double[] interval, boolean inside) {
        super("Duration per case in/out-side interval filter query", selectFrom);

        Condition condition = null;
        IntervalValue value = new IntervalValue(interval);

        if(inside) {
            condition = new InIntervalCondition("timestampdiff(minute, min(t.time_stamp), max(t.time_stamp))", value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition("timestampdiff(minute, min(t.time_stamp), max(t.time_stamp))", value));
        }

        this.setCondition(condition);
    }

    public DurationPerCaseQuery(String queryName, String selectFrom, double number, ComparisionType comparisionType) {
        super("Duration per case comparision filter query", selectFrom);

        NummericalValue value = new NummericalValue(number);
        CompareCondition compareCondition = new CompareCondition("timestampdiff(minute, min(t.time_stamp), max(t.time_stamp))", value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t1 right join (select distinct caseId from %s as t group by caseId having %s) as t2 on t1.caseId = t2.caseId",
                this.getSelectFrom(), this.getSelectFrom(), this.getCondition().toString());
    }
}
