package query.common;

import analysis.variation.VariationType;
import condition.*;
import condition.value.SetElementType;
import condition.value.SetValue;

import java.util.HashMap;
import java.util.List;

public class ResourceQuery extends CommonQuery {

    private static String conditionAttribute = "t.resource";

    public ResourceQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Resource in/out-side list filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        SetValue value = new SetValue(list, SetElementType.VARCHAR);

        Condition condition = null;
        if(inside) {
            condition = new InSetCondition(ResourceQuery.conditionAttribute, value);
        }
        else {
            condition = new NotCondition(null, new InSetCondition(ResourceQuery.conditionAttribute, value));
        }

        this.setCondition(condition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().getCondition() );
    }

    private void setAttributeValueSetQueriesHashMap() {
        HashMap<VariationType, String> resourceQueries = new HashMap<>() {{
            put(VariationType.SET, getAllResources());
        }};
        this.setAttributeValueSetQueries(
                new HashMap<>() {{
                    put(ResourceQuery.conditionAttribute, resourceQueries);
                }}
        );
    }

    private String getAllResources() {
        return String.format("select distinct %s as element from %s as t", ResourceQuery.conditionAttribute, this.getSelectFrom());
    }
}
