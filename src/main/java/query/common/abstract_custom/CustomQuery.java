package query.common.abstract_custom;

import query.common.CommonQuery;

/**
 *
 */
public class CustomQuery extends CommonQuery {

    private String attribute;

    public CustomQuery(String queryName, String attribute, String selectFrom) {
        super(queryName, selectFrom);
        this.attribute = attribute;
    }

    public String getAttribute() {
        return this.attribute;
    }

    @Override
    public String getQuery() {
        return String.format("select t.* from %s as t where %s", this.getSelectFrom(), this.getCondition().getCondition());
    }
}
