package condition.value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SetValue extends Value {
	
	private SetElementType elementType;
	
	@SuppressWarnings("serial")
	private static HashMap<SetElementType, String> formats = new HashMap<>() {{
		put(SetElementType.VARCHAR, "'%s'");
		put(SetElementType.NUMBER, "%s");
	}};

	public SetValue(Object value, SetElementType elementType) {
		super(value, ValueType.SET);
		this.elementType = elementType;
	}
	
	public SetElementType getElementType() {
		return elementType;
	}

	public void setElementType(SetElementType elementType) {
		this.elementType = elementType;
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

	@SuppressWarnings("unchecked")
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
