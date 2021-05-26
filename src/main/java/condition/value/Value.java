package condition.value;

public abstract class Value {
	
	private ValueType type;
	
	private Object value;
	
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
	
	public abstract Object increase(Object... objects);
	
	public abstract Object decrease(Object... objects);
	
	public abstract String toString();
}
