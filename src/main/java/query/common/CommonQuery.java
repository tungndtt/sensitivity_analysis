package query.common;

import analysis.variation.VariationType;
import condition.Condition;

import java.util.HashMap;

public abstract class CommonQuery {
	
	private String queryName;

	private String selectFrom;
	
	private Condition condition;

	private HashMap<String, HashMap<VariationType, String>> attributeValueSetQueries;
	
	public CommonQuery(String queryName, String selectFrom) {
		this.queryName = queryName;
		this.selectFrom = selectFrom;
	}
	
	public CommonQuery(String queryName, String selectFrom, Condition condition) {
		this.queryName = queryName;
		this.selectFrom = selectFrom;
		this.condition = condition;
	}
	
	public void setQueryName(String name) {
		this.queryName = name;
	}
	
	public String getQueryName() {
		return this.queryName;
	}

	public String getSelectFrom() {
		return selectFrom;
	}

	public void setSelectFrom(String selectFrom) {
		this.selectFrom = selectFrom;
	}
	
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public Condition getCondition() {
		return this.condition;
	}

	protected void setAttributeValueSetQueries(HashMap<String, HashMap<VariationType, String>> attributeValueSetQueries) {
		this.attributeValueSetQueries = attributeValueSetQueries;
	}
	
	public abstract String getQuery();

	public boolean supportVariationType(String attribute, VariationType variationType) {
		if(this.attributeValueSetQueries.containsKey(attribute)){
			HashMap<VariationType, String> e = this.attributeValueSetQueries.get(attribute);
			return e.containsKey(variationType);
		}
		else {
			return false;
		}
	}

	public String getQueryForVariation(String attribute, VariationType variationType) {
		if(this.attributeValueSetQueries.containsKey(attribute)) {
			if (this.attributeValueSetQueries.get(attribute).containsKey(variationType)) {
				return this.attributeValueSetQueries.get(attribute).get(variationType);
			}
		}
		return null;
	}
}
