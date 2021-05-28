package condition.value;

public class IntervalValue extends Value{

	public IntervalValue(Object value) {
		super(value, ValueType.INTERVAL);
	}

	@Override
	public Object increase(Object... objects) {
		Number left = (Number) objects[0];
		Number right = (Number) objects[1];
		Value[] interval = (Value[]) this.getValue();
		return new Object[]{interval[0].decrease(left), interval[1].increase(right)};
	}

	@Override
	public Object decrease(Object... objects) {
		Number left = (Number) objects[0];
		Number right = (Number) objects[1];
		Value[] interval = (Value[]) this.getValue();
		return new Object[]{interval[0].increase(left), interval[1].decrease(right)};
	}

	@Override
	public String toString() {
		Object[] objs = (Object[]) this.getValue();
		return objs[0].toString() + " and " + objs[1].toString();
	}

}
