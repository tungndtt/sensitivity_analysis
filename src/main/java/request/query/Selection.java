package request.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import component.attribute.Attribute;
import component.condition.Condition;
import query.common.CommonQuery;
import java.util.Iterator;
import java.util.List;

public class Selection extends CommonQuery {

    private List<Attribute> attributes;

    private Selection from;

    private String table;

    private String tableAlias;

    private boolean isSelectingTable;

    private Join join;

    private Condition where;

    private List<Attribute> groupBy;

    private Condition having;

    private SetOperation setOperation;

    private String alias;

    public Selection(@JsonProperty("select") List<Attribute> attributes, @JsonProperty("from") Selection from, @JsonProperty("table") String table,
                     @JsonProperty("table alias") String tableAlias, @JsonProperty("table select") boolean isSelectingTable, @JsonProperty("join") Join join,
                     @JsonProperty("where") Condition where, @JsonProperty("group by") List<Attribute> groupBy, @JsonProperty("having") Condition having,
                     @JsonProperty("set operation") SetOperation setOperation, @JsonProperty("alias") String alias) {
        this.attributes = attributes;
        this.from = from;
        this.table = table;
        this.tableAlias = tableAlias;
        this.isSelectingTable = isSelectingTable;
        this.join = join;
        this.where = where;
        this.groupBy = groupBy;
        this.having = having;
        this.setOperation = setOperation;
        this.alias = alias;

        this.setCondition(this.getAnalyzingCondition());
    }

    Condition getAnalyzingCondition() {
        Condition cond = null;
        if(this.where != null) {
            cond = this.where.getAnalyzingCondition();
        }
        if(this.having != null && cond == null) {
            cond = this.having.getAnalyzingCondition();
        }
        if(this.from != null && cond == null) {
            cond = this.from.getAnalyzingCondition();
        }
        if(this.join != null && cond == null) {
            cond = this.join.getAnalyzingCondition();
        }
        if(this.setOperation != null && cond == null) {
            cond = this.setOperation.getAnalyzingCondition();
        }
        return cond;
    }

    @Override
    public String getQuery() {
        Iterator<Attribute> attributeIterator = this.attributes.iterator();
        String select = "select " + attributeIterator.next().toString();
        while(attributeIterator.hasNext()) {
            select += " , " + attributeIterator.next().toString();
        }

        String from = " from ";
        if(this.isSelectingTable) {
            from += this.table + (this.tableAlias != null && this.tableAlias.length() > 0? " as " + this.tableAlias : "");
        }
        else {
            from += this.from.getQuery();
        }

        String join = this.join != null ? " " + this.join.toString() : "";

        String where = this.where != null ? " where " + this.where.getCondition() : "";

        String groupBy = "";
        if(this.groupBy != null && this.groupBy.size() > 0) {
            attributeIterator = this.groupBy.iterator();
            groupBy = " group by " + attributeIterator.next().toString();
            while(attributeIterator.hasNext()) {
                groupBy += " , " + attributeIterator.next().toString();
            }
        }

        String having = this.having != null ? " having " + this.having.getCondition() : "";
        String setOperation = this.setOperation != null ? this.setOperation.toString() : "";

        String result = select + from + join + where + groupBy + having + setOperation;
        return this.alias != null && this.alias.length() > 0? "( " + result + " ) as " + this.alias : result;
    }
}

