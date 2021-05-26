package condition;

import condition.value.Value;

public class CompareCondition extends Condition {
	
	private ComparisionType compareType;
	
	public CompareCondition(String attribute, Value value, ComparisionType compareType) {
		super(attribute, value, null, ConditionType.COMPARISION);
		this.compareType = compareType;
	}
	
	public ComparisionType getCompareType() {
		return this.compareType;
	}
	
	public void setCompareType(ComparisionType compareType) {
		this.compareType = compareType;
	}

	@Override
	public String getCondition() {
		String compareSign = "";
		if(this.compareType == ComparisionType.GT) 
			compareSign = " > ";
		else if(this.compareType == ComparisionType.LT) 
			compareSign = " < ";
		else if(this.compareType == ComparisionType.GTE) 
			compareSign = " >= ";
		else 
			compareSign = " <= ";
			
		return this.getAttribute() + compareSign + this.getValue().toString();
	}

}
