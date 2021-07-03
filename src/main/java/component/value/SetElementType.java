package component.value;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SetElementType {
	VARCHAR("string"),
	NUMBER("number");

	private String type;

	SetElementType(String type) {
		this.type = type;
	}

	@JsonCreator
	public static SetElementType forValue(String type) {
		if(type.equals(VARCHAR.type)) {
			return VARCHAR;
		}
		else if(type.equals(NUMBER.type)) {
			return NUMBER;
		}
		else {
			return null;
		}
	}

	public String getType() {
		return this.type;
	}
}
