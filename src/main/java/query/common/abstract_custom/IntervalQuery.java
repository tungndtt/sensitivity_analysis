package query.common.abstract_custom;

import condition.Condition;
import condition.InIntervalCondition;
import condition.NotCondition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.Value;

import java.util.Date;

public class IntervalQuery extends CustomQuery {

    public IntervalQuery(String queryName, String attribute, String selectFrom, Object[] interval, boolean inside) {
        super(queryName, attribute, selectFrom);

        if(interval != null) {
            Value value = null;
            if(interval[0] instanceof Date) {
                DateValue startDate = new DateValue(interval[0]);
                DateValue endDate = new DateValue(interval[1]);
                value = new IntervalValue(new DateValue[]{startDate, endDate});
            }
            else if(interval[0] instanceof Number){
                value = new IntervalValue(interval);
            }
            Condition condition = null;
            if(inside) {
                condition = new InIntervalCondition(this.getAttribute(), value);
            }
            else {
                condition = new NotCondition(null, new InIntervalCondition(this.getAttribute(), value));
            }

            this.setCondition(condition);
        }
    }
}
