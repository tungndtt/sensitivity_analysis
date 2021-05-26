package condition;

import java.util.Iterator;
import java.util.List;

public class OrCondition extends Condition {

	public OrCondition(String attribute, List<Condition> subConditions) {
		super(attribute, null, subConditions, ConditionType.OR);
	}

	@Override
	public String getCondition() {
		Iterator<Condition> iterator = this.getSubConditions().iterator();
		String cond = "( " + iterator.next().toString() + " )";
		
		while(iterator.hasNext()) {
			cond += " or ( " + iterator.next().toString() + " )";
		}
		return cond;
	}

}
