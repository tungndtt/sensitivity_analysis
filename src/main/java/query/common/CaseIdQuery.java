package query.common;

import analysis.variation.VariationType;
import condition.*;
import query.common.abstract_custom.CompareQuery;
import query.common.abstract_custom.CustomQuery;
import query.common.abstract_custom.IntervalQuery;
import query.common.abstract_custom.SetQuery;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class CaseIdQuery extends CommonQuery {

    private static String conditionAttribute = "t.caseid";

    private CustomQuery customQuery;

    public CaseIdQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Case id in/out-side list filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new SetQuery("Case id in/out-side list filter query", CaseIdQuery.conditionAttribute, selectFrom, list, inside);

        this.setCondition(this.customQuery.getCondition());
    }

    public CaseIdQuery(String selectFrom, int[] interval, boolean inside) {
        super("Case id in/out-side interval filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        Integer[] _interval = Arrays.stream(interval).boxed().toArray(Integer[]::new);
        this.customQuery = new IntervalQuery("Case id in/out-side interval filter query", CaseIdQuery.conditionAttribute, selectFrom, _interval, inside);

        this.setCondition(this.customQuery.getCondition());
    }

    public CaseIdQuery(String selectFrom, int number, ComparisionType comparisionType) {
        super("Case id comparision filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new CompareQuery("Case id comparision filter query", CaseIdQuery.conditionAttribute, selectFrom, number, comparisionType);

        this.setCondition(this.customQuery.getCondition());
    }

    @Override
    public String getQuery() {
        return this.customQuery.getQuery();
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
