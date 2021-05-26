package condition;

import java.util.Iterator;
import java.util.List;

public class AndCondition extends Condition {

	public AndCondition(String attribute, List<Condition> subConditions) {
		super(attribute, null, subConditions, ConditionType.AND);
	}

	@Override
	public String getCondition() {
		Iterator<Condition> iterator = this.getSubConditions().iterator();
		String cond = "( " + iterator.next().toString() + " )";
		
		while(iterator.hasNext()) {
			cond += " and ( " + iterator.next().toString() + " )";
		}
		return cond;
	}

}
