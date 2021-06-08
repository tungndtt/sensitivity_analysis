package condition;

import java.util.LinkedList;


public class NotCondition extends Condition {

	public NotCondition(String attribute, Condition reversedCondition) {
		super(attribute, null, new LinkedList<>(), ConditionType.NOT);
		this.getSubConditions().add(reversedCondition);
	}

	@Override
	public String getCondition() {
		Condition cond = ((LinkedList<Condition>) this.getSubConditions()).getFirst();
		return String.format("not( %s )", cond.getCondition());
	}

}
