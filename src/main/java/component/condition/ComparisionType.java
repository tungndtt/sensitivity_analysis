package component.condition;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ComparisionType {
	GT(">"),
	LT("<"),
	GTE(">="),
	LTE("<="),
	EQ("=");

	private String type;

	ComparisionType(String type) {
		this.type = type;
	}

	@JsonCreator
	public static ComparisionType forValue(String type) {
		if(type.equals(GT.type)) return GT;
		else if(type.equals(LT.type)) return LT;
		else if(type.equals(GTE.type)) return GTE;
		else if(type.equals(LTE.type)) return LTE;
		else if(type.equals(EQ.type)) return EQ;
		else return null;
	}

	public String getType() {
		return this.type;
	}
}
