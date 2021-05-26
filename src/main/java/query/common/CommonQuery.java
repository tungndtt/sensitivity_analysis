package query.common;

import condition.Condition;

public abstract class CommonQuery {
	
	private String queryName;

	private String selectFrom;
	
	private Condition condition;
	
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
	
	public abstract String getQuery();
}
