package query.common;

import analysis.variation.VariationType;
import condition.*;
import condition.value.*;

import java.util.HashMap;
import java.util.List;

public class CaseIdQuery extends CommonQuery {

    private static String conditionAttribute = "t.caseid";

    public CaseIdQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Case id in/out-side list filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        SetValue value = new SetValue(list, SetElementType.NUMBER);
        Condition condition = null;
        if(inside) {
            condition = new InSetCondition(CaseIdQuery.conditionAttribute, value);
        }
        else {
            condition = new NotCondition(null, new InSetCondition(CaseIdQuery.conditionAttribute, value));
        }

        this.setCondition(condition);
    }

    public CaseIdQuery(String selectFrom, int[] interval, boolean inside) {
        super("Case id in/out-side interval filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        IntervalValue value = new IntervalValue(interval);
        Condition condition = null;
        if(inside) {
            condition = new InIntervalCondition(CaseIdQuery.conditionAttribute, value);
        }
        else {
            condition = new NotCondition(null, new InIntervalCondition(CaseIdQuery.conditionAttribute, value));
        }

        this.setCondition(condition);
    }

    public CaseIdQuery(String selectFrom, int number, ComparisionType comparisionType) {
        super("Case id comparision filter query", selectFrom);

        NumericalValue value = new NumericalValue(number);
        CompareCondition compareCondition = new CompareCondition(CaseIdQuery.conditionAttribute, value, comparisionType);

        this.setCondition(compareCondition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().getCondition());
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> caseidQueries = new HashMap<>(){{
            put(VariationType.SET, getAllCaseId());
            put(VariationType.NAIVE, getMinMaxCaseId());
        }};
        this.setAttributeValueSetQueries(
                new HashMap<>() {{
                    put(CaseIdQuery.conditionAttribute, caseidQueries);
                }}
        );
    }

    private String getAllCaseId() {
        return String.format("select distinct %s as element from %s as t", CaseIdQuery.conditionAttribute, this.getSelectFrom());
    }

    private String getMinMaxCaseId() {
        return String.format("select min(%s) as minimum, max(%s) as maximum from %s as t", CaseIdQuery.conditionAttribute, CaseIdQuery.conditionAttribute, this.getSelectFrom());
    }
}
