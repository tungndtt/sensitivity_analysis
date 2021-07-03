package query.common.abstract_custom;

import component.attribute.Attribute;
import component.condition.Condition;
import component.condition.InSetCondition;
import component.condition.NotCondition;
import component.value.SetElementType;
import component.value.SetValue;
import java.util.List;

/**
 *
 */
public class SetQuery extends CustomQuery {

    public SetQuery(String queryName, String attribute, String selectFrom, List<Object> elements, boolean inside) {
        super(queryName, attribute, selectFrom);

        SetElementType elementType = null;
        if(elements != null) {
            if(elements.size() == 0) {
                elementType = SetElementType.VARCHAR;
            }
            else {
                Object obj = elements.get(0);
                if(obj instanceof Number) {
                    elementType = SetElementType.NUMBER;
                }
                else if(obj instanceof String) {
                    elementType = SetElementType.VARCHAR;
                }
                else {
                    System.out.println("Unsupported element type!");
                }
            }
            if(elementType != null) {
                SetValue value = new SetValue(elements, elementType);
                Condition condition = null;
                if(inside) {
                    condition = new InSetCondition(new Attribute(this.getAttribute(), null), value);
                }
                else {
                    condition = new NotCondition(null, new InSetCondition(new Attribute(this.getAttribute(), null), value));
                }

                this.setCondition(condition);
            }
        }
    }
}
