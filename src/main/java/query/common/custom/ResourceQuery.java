package query.common.custom;

import analysis.variation.VariationType;
import query.common.CommonQuery;
import query.common.abstract_custom.CustomQuery;
import query.common.abstract_custom.SetQuery;

import java.util.HashMap;
import java.util.List;

public class ResourceQuery extends CommonQuery {

    private static String conditionAttribute = "t.resource";

    private CustomQuery customQuery;

    public ResourceQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Resource in/out-side list filter query", selectFrom);
        this.setAttributeValueSetQueriesHashMap();

        this.customQuery = new SetQuery("Resource in/out-side list filter query", ResourceQuery.conditionAttribute, selectFrom, list, inside);

        this.setCondition(this.customQuery.getCondition());
    }

    @Override
    public String getQuery() {
        return this.customQuery.getQuery();
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
