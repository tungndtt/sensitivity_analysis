package condition;

import java.util.List;

import condition.value.Value;

public abstract class Condition {
	
	private String attribute;
	
	private List<Condition> subConditions;
	
	private Value value;
	
	private ConditionType type;
	
	public Condition(String attribute, Value value, List<Condition> subConditions, ConditionType type) {
		this.attribute = attribute;
		this.value = value;
		this.subConditions = subConditions;
		this.type = type;
	}
	
	public void setAttribute(String attributeName) {
		this.attribute = attributeName;
	}
	
	public String getAttribute() {
		return this.attribute;
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	public Value getValue() {
		return this.value;
	}
	
	public void setSubConditions(List<Condition> subConditions) {
		this.subConditions = subConditions;
	}
	
	public List<Condition> getSubConditions() {
		return this.subConditions;
	}
	
	public ConditionType getConditionType() {
		return this.type;
	}
	
	public void setConditionType(ConditionType type) {
		this.type = type;
	}
	
	public abstract String getCondition();
}
