package query.common;

import condition.*;
import condition.value.*;

import java.util.List;

public class CaseIdQuery extends CommonQuery {

    public CaseIdQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Case id in/out-side list filter query", selectFrom);

        SetValue value = new SetValue(list, SetElementType.NUMBER);
        Condition condition = null;
        if(inside) {
            condition = new InSetCondition("t.caseId", value);
        }
        else {
            condition = new NotCondition(null, new InSetCondition("t.caseId", value));
        }

        this.setCondition(condition);
    }

    public CaseIdQuery(String selectFrom, int[] interval, boolean inside) {
        super("Case id in/out-side interval filter query", selectFrom);

        IntervalValue value = new IntervalValue(interval);
        Condition condition = null;
        if(inside) {
            condition = new InIntervalCondition("t.caseId", value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition("t.caseId", value));
        }

        this.setCondition(condition);
    }

    public CaseIdQuery(String selectFrom, int number, ComparisionType comparisionType) {
        super("Case id comparision filter query", selectFrom);

        NummericalValue value = new NummericalValue(number);
        CompareCondition compareCondition = new CompareCondition("t.caseId", value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().toString() );
    }
}
