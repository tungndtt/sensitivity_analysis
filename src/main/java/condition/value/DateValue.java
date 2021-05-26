package condition.value;

public class DateValue extends Value {

	public DateValue(Object value) {
		super(value, ValueType.DATE);
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
		return String.format("date('%s')", this.getValue().toString());
	}

}
