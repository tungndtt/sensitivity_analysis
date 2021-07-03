package component.value;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValue extends Value {

	public DateValue(@JsonProperty("value") String dateValue, @JsonProperty("type") ValueType type) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
			this.setValue(ft.parse(dateValue));
		} catch (ParseException e) {
			System.out.println(e);
		}
		this.setValueType(type);
	}

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
