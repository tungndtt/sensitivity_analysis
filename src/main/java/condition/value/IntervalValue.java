package condition.value;

public class IntervalValue extends Value{

	public IntervalValue(Object value) {
		super(value, ValueType.INTERVAL);
	}

	@Override
	public Object increase(Object... objects) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object decrease(Object... objects) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		Object[] objs = (Object[]) this.getValue();
		return objs[0].toString() + " and " + objs[1].toString();
	}

}
