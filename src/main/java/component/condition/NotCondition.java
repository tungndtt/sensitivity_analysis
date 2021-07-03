package component.condition;

import component.attribute.Attribute;

import java.util.LinkedList;


public class NotCondition extends Condition {

	public NotCondition(Attribute attribute, Condition reversedCondition) {
		super(attribute, null, new LinkedList<>(), ConditionType.NOT);
		this.getSubConditions().add(reversedCondition);
	}

	@Override
	public String getCondition() {
		Condition cond = this.getSubConditions().get(0);
		return String.format("not( %s )", cond.getCondition());
	}

}
