package component.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import component.attribute.Attribute;
import component.value.Value;

public class CompareCondition extends Condition {

	@JsonProperty("type")
	private ComparisionType compareOperation;
	
	public CompareCondition(Attribute attribute, Value value, ComparisionType compareOperation) {
		super(attribute, value, null, ConditionType.COMPARISION);
		this.compareOperation = compareOperation;
	}
	
	public ComparisionType getCompareOperation() {
		return this.compareOperation;
	}
	
	public void setCompareOperation(ComparisionType compareOperation) {
		this.compareOperation = compareOperation;
	}

	@Override
	public String getCondition() {
		if(this.compareOperation != null) {
			return this.getAttribute().toString() + " " + this.compareOperation.getType() + " " + this.getValue().toString();
		}
		else {
			return null;
		}
	}
}
