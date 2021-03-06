package component.value;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class SetValue extends Value {

	@JsonProperty("element type")
	private SetElementType elementType;

	
	@SuppressWarnings("serial")
	private static HashMap<SetElementType, String> formats = new HashMap<>() {{
		put(SetElementType.VARCHAR, "'%s'");
		put(SetElementType.NUMBER, "%s");
	}};

	public SetValue(){}

	public SetValue(Object value, SetElementType elementType) {
		super(value, ValueType.SET);
		this.elementType = elementType;
	}
	
	public SetElementType getElementType() {
		return elementType;
	}

	@Override
	public Value increase(Object... objects) {
		int unit = (Integer) objects[0];
		List<Object> elements = (List<Object>) objects[1];
		HashSet<Object> existed = (HashSet<Object>) objects[2];
		List<Object> listElements = (List<Object>) this.getValue();

		while(unit > 0 && elements.size() > 0) {
			Object obj = elements.remove(0);
			if(!existed.contains(obj)) {
				listElements.add(obj);
				--unit;
			}
		}
		return this;
	}

	@Override
	public Value decrease(Object... objects) {
		int unit = (Integer) objects[0];
		List<Object> listElements = (List<Object>) this.getValue();

		while(unit-- > 0 && listElements.size() > 0) {
			listElements.remove(0);
		}
		return this;
	}

	@Override
	public String toString() {
		List<String> values = (List<String>)this.getValue();
		if(values.size() > 0) {
			Iterator<String> iterator = values.iterator();
			String set = "( " + String.format(SetValue.formats.get(this.getElementType()), iterator.next());
			
			while(iterator.hasNext()) {
				set += ", " + String.format(SetValue.formats.get(this.getElementType()), iterator.next());
			}
			set += " )";
			
			return set;
		}
		else return null;
	}
}
