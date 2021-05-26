package query.common;

import condition.*;
import condition.value.SetElementType;
import condition.value.SetValue;

import java.util.List;

public class ResourceQuery extends CommonQuery {
    public ResourceQuery(String selectFrom, List<Object> list, boolean inside) {
        super("Resource in/out-side list filter query", selectFrom);

        SetValue value = new SetValue(list, SetElementType.VARCHAR);

        Condition condition = null;
        if(inside) {
            condition = new InSetCondition("t.resource", value);
        }
        else {
            condition = new NotCondition(null, new InSetCondition("t.resource", value));
        }

        this.setCondition(condition);
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s as t where %s", this.getSelectFrom(), this.getCondition().toString() );
    }
}
