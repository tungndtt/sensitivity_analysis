package component.condition;

import component.attribute.Attribute;
import component.value.Value;

public class InSetCondition extends Condition {

	public InSetCondition() {}

	public InSetCondition(Attribute attribute, Value value) {
		super(attribute, value, null, ConditionType.IN_SET);
	}

	@Override
	public String getCondition() {
		return this.getAttribute().toString() + " in " + this.getValue().toString();
	}

}
