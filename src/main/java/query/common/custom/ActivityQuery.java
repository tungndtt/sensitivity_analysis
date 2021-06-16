package query.common.custom;

import analysis.variation.VariationType;
import query.common.CommonQuery;
import query.common.abstract_custom.CustomQuery;
import query.common.abstract_custom.SetQuery;
import java.util.HashMap;
import java.util.List;

public class ActivityQuery extends CommonQuery {

    private static String conditionAttribute = "t.activity";

    private CustomQuery customQuery;

    public ActivityQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Activity in/out-side list filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new SetQuery("Activity in/out-side list filter query", ActivityQuery.conditionAttribute, selectFrom, list, inside);

        this.setCondition(this.customQuery.getCondition());
    }

    @Override
    public String getQuery() {
        return this.customQuery.getQuery();
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> activityQueries = new HashMap<>() {{
            put(VariationType.SET, getAllActivities());
        }};
        this.setAttributeValueSetQueries(
                new HashMap<>() {{
                    put(ActivityQuery.conditionAttribute, activityQueries);
                }}
        );
    }

    private String getAllActivities() {
        return String.format("select distinct %s as element from %s as t", ActivityQuery.conditionAttribute, this.getSelectFrom());
    }
}
