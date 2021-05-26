package query.common;

import condition.*;
import condition.value.DateValue;
import condition.value.IntervalValue;

import java.util.Date;

public class TimestampQuery extends CommonQuery {
    public TimestampQuery(String selectFrom, Date[] interval, boolean inside) {
        super("Time stamp in/out-side interval filter query", selectFrom);

        IntervalValue value = new IntervalValue(interval);
        Condition condition = null;
        if(inside) {
            condition = new InIntervalCondition("t.time_stamp", value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition("t.time_stamp", value));
        }

        this.setCondition(condition);
    }

    public TimestampQuery(String selectFrom, Date date, ComparisionType comparisionType) {
        super("Time stamp comparision filter query", selectFrom);

        DateValue value = new DateValue(date);
        CompareCondition compareCondition = new CompareCondition("t.time_stamp", value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().toString());
    }
}
