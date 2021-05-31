package condition.value;

public class NumericalValue extends Value {
	public NumericalValue(Object value) {
		super(value, ValueType.NUMERICAL);
	}

	@Override
	public Value increase(Object... objects) {
		if(this.getValue() instanceof Integer) {
			return new NumericalValue((Integer)this.getValue() + ((Number)objects[0]).intValue());
		}
		else if(this.getValue() instanceof Long) {
			return new NumericalValue((Long)this.getValue() + ((Long)objects[0]).longValue());
		}
		else if(this.getValue() instanceof Double) {
			return new NumericalValue((Double)this.getValue() + ((Double)objects[0]).doubleValue());
		}
		else return null;
	}

	@Override
	public Value decrease(Object... objects) {
		if(this.getValue() instanceof Integer) {
			return new NumericalValue((Integer)this.getValue() - ((Integer)objects[0]).intValue());
		}
		else if(this.getValue() instanceof Long) {
			return new NumericalValue((Long)this.getValue() - ((Long)objects[0]).longValue());
		}
		else if(this.getValue() instanceof Double) {
			return new NumericalValue((Double)this.getValue() - ((Double)objects[0]).doubleValue());
		}
		else return null;
	}

	@Override
	public String toString() {
		return this.getValue().toString();
	}
}
