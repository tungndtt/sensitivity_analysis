package component.condition;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import component.attribute.Attribute;
import component.value.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(value = AndCondition.class, name = "and"),
		@JsonSubTypes.Type(value = CompareCondition.class, name = "compare"),
		@JsonSubTypes.Type(value = OrCondition.class, name = "or"),
		@JsonSubTypes.Type(value = NotCondition.class, name = "not"),
		@JsonSubTypes.Type(value = InSetCondition.class, name = "in set"),
		@JsonSubTypes.Type(value = InIntervalCondition.class, name = "in interval")
})
public abstract class Condition {

	@JsonProperty("attribute")
	private Attribute attribute;

	@JsonProperty("subConditions")
	private List<Condition> subConditions;

	@JsonProperty("value")
	private Value value;

	@JsonProperty("type")
	private ConditionType type;

	@JsonProperty("toAnalyze")
	private boolean toAnalyze;

	public Condition(){}
	
	public Condition(Attribute attribute, Value value, List<Condition> subConditions, ConditionType type) {
		this.attribute = attribute;
		this.value = value;
		this.subConditions = subConditions;
		this.type = type;
	}
	
	public void setAttribute(Attribute attributeName) {
		this.attribute = attributeName;
	}
	
	public Attribute getAttribute() {
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

	public Condition getAnalyzingCondition() {
		if(this.toAnalyze) {
			return this;
		}
		else if(this.subConditions != null){
			for(Condition subCond : subConditions) {
				Condition cond = subCond.getAnalyzingCondition();
				if(cond != null) {
					return cond;
				}
			}
		}
		return null;
	}
	
	public abstract String getCondition();
}
