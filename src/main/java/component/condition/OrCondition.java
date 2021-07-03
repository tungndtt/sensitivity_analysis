package component.condition;

import component.attribute.Attribute;

import java.util.Iterator;
import java.util.List;

public class OrCondition extends Condition {

	public OrCondition(Attribute attribute, List<Condition> subConditions) {
		super(attribute, null, subConditions, ConditionType.OR);
	}

	@Override
	public String getCondition() {
		Iterator<Condition> iterator = this.getSubConditions().iterator();
		String cond = "( " + iterator.next().getCondition() + " )";
		
		while(iterator.hasNext()) {
			cond += " or ( " + iterator.next().getCondition() + " )";
		}
		return cond;
	}

}
