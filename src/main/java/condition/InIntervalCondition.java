package condition;

import condition.value.Value;

public class InIntervalCondition extends Condition {

	public InIntervalCondition(String attribute, Value value) {
		super(attribute, value, null, ConditionType.IN_INTERVAL);
	}

	@Override
	public String getCondition() {
		return this.getAttribute() + " between " + this.getValue().toString();
	}

}
