package condition.value;

import java.util.Date;

public class DateValue extends Value {

	public DateValue(Object value) {
		super(value, ValueType.DATE);
	}

	@Override
	public Object increase(Object... objects) {
		Number unit = (Number) objects[0];
		Date date = (Date) this.getValue();
		return new Date(date.getTime() + unit.longValue()*60000);
	}

	@Override
	public Object decrease(Object... objects) {
		Number unit = (Number) objects[0];
		Date date = (Date) this.getValue();
		return new Date(date.getTime() - unit.longValue()*60000);
	}

	@Override
	public String toString() {
		return String.format("date('%s')", this.getValue().toString());
	}

}