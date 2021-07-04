package component.condition;

import component.attribute.Attribute;
import java.util.Iterator;
import java.util.List;

public class AndCondition extends Condition {

	public AndCondition(){}

	public AndCondition(Attribute attribute, List<Condition> subConditions) {
		super(attribute, null, subConditions, ConditionType.AND);
	}

	@Override
	public String getCondition() {
		Iterator<Condition> iterator = this.getSubConditions().iterator();
		String cond = "( " + iterator.next().getCondition() + " )";
		
		while(iterator.hasNext()) {
			cond += " and ( " + iterator.next().getCondition() + " )";
		}
		return cond;
	}

}
