package query.common;

import condition.Condition;

import java.util.HashMap;

public abstract class DeterminableCommonQuery extends CommonQuery{
    public DeterminableCommonQuery(String queryName, String selectFrom) {
        super(queryName, selectFrom);
    }

    public DeterminableCommonQuery(String queryName, String selectFrom, Condition condition) {
        super(queryName, selectFrom, condition);
    }

    public abstract HashMap<String, String> getBoundQueries();
}
