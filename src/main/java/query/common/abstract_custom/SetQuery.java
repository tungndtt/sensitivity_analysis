package query.common.abstract_custom;

import condition.Condition;
import condition.InSetCondition;
import condition.NotCondition;
import condition.value.SetElementType;
import condition.value.SetValue;
import java.util.List;

public class SetQuery extends CustomQuery {

    public SetQuery(String queryName, String attribute, String selectFrom, List<Object> elements, boolean inside) {
        super(queryName, attribute, selectFrom);

        SetElementType elementType = null;
        if(elements != null && elements.size() > 0) {
            Object obj = elements.get(0);
            if(obj instanceof Number) {
                elementType = SetElementType.NUMBER;
            }
            else if(obj instanceof String) {
                elementType = SetElementType.VARCHAR;
            }
            if(elementType != null) {
                SetValue value = new SetValue(elements, elementType);
                Condition condition = null;
                if(inside) {
                    condition = new InSetCondition(this.getAttribute(), value);
                }
                else {
                    condition = new NotCondition(null, new InSetCondition(this.getAttribute(), value));
                }

                this.setCondition(condition);
            }
        }
    }
}
