package component.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(value = DateValue.class, name = "date"),
		@JsonSubTypes.Type(value = IntervalValue.class, name = "interval"),
		@JsonSubTypes.Type(value = SetValue.class, name = "set"),
		@JsonSubTypes.Type(value = NumericalValue.class, name = "number")
})
public abstract class Value {

	@JsonProperty("type")
	private ValueType type;

	@JsonProperty("value")
	private Object value;

	public Value(){}

	public Value(Object value, ValueType type) {
		this.value = value;
		this.type = type;
	}

	public void setValueType(ValueType type) {
		this.type = type;
	}

	public ValueType getValueType() {
		return this.type;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

	public abstract Value increase(Object... objects);

	public abstract Value decrease(Object... objects);

	public abstract String toString();
}
