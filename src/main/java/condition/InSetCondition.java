package condition;

import condition.value.Value;

public class InSetCondition extends Condition {

	public InSetCondition(String attribute, Value value) {
		super(attribute, value, null, ConditionType.IN_SET);
	}

	@Override
	public String getCondition() {
		return this.getAttribute() + " in " + this.getValue().toString();
	}

}
