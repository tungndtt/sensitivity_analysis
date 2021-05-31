package condition.value;

import java.util.Date;

public class DateValue extends Value {

	public DateValue(Object value) {
		super(value, ValueType.DATE);
	}

	@Override
	public Value increase(Object... objects) {
		Number unit = (Number) objects[0];
		Date date = (Date) this.getValue();
		return new DateValue(new Date(date.getTime() + unit.longValue()*60000));
	}

	@Override
	public Value decrease(Object... objects) {
		Number unit = (Number) objects[0];
		Date date = (Date) this.getValue();
		return new DateValue(new Date(date.getTime() - unit.longValue()*60000));
	}

	@Override
	public String toString() {
		return String.format("date('%s')", this.getValue().toString());
	}

}
