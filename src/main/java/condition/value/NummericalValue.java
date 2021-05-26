package condition.value;

public class NummericalValue extends Value {
	public NummericalValue(Number value) {
		super(value, ValueType.NUMMERICAL);
	}

	@Override
	public Object increase(Object... objects) {
		if(this.getValue() instanceof Integer) {
			return (Integer)this.getValue() + (Integer)objects[0];
		}
		else if(this.getValue() instanceof Long) {
			return (Long)this.getValue() + (Long)objects[0];
		}
		else if(this.getValue() instanceof Double) {
			return (Double)this.getValue() + (Double)objects[0];
		}
		else return null;
	}

	@Override
	public Object decrease(Object... objects) {
		if(this.getValue() instanceof Integer) {
			return (Integer)this.getValue() - (Integer)objects[0];
		}
		else if(this.getValue() instanceof Long) {
			return (Long)this.getValue() - (Long)objects[0];
		}
		else if(this.getValue() instanceof Double) {
			return (Double)this.getValue() - (Double)objects[0];
		}
		else return null;
	}

	@Override
	public String toString() {
		return this.getValue().toString();
	}
}
