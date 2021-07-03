package component.condition;

import component.attribute.Attribute;
import component.value.Value;

public class InIntervalCondition extends Condition {

	public InIntervalCondition() {}

	public InIntervalCondition(Attribute attribute, Value value) {
		super(attribute, value, null, ConditionType.IN_INTERVAL);
	}

	@Override
	public String getCondition() {
		return this.getAttribute().toString() + " between " + this.getValue().toString();
	}

}
