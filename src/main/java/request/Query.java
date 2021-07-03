package request;

import com.fasterxml.jackson.annotation.JsonProperty;
import component.attribute.Attribute;
import component.condition.Condition;
import query.common.CommonQuery;
import java.util.Iterator;
import java.util.List;

public class Query extends CommonQuery {

    private List<Attribute> attributes;

    private Selection from;

    private Join join;

    private Condition where;

    private List<Attribute> groupBy;

    private Condition having;

    private SetOperation setOperation;

    private String alias;

    @JsonProperty("analysis attribute")
    private AnalysisAttribute analysisAttribute;

    public Query(@JsonProperty("select") List<Attribute> attributes, @JsonProperty("from") Selection from, @JsonProperty("join") Join join,
                 @JsonProperty("where") Condition where, @JsonProperty("group by") List<Attribute> groupBy, @JsonProperty("having") Condition having,
                 @JsonProperty("set operation") SetOperation setOperation, @JsonProperty("alias") String alias) {
        this.attributes = attributes;
        this.from = from;
        this.join = join;
        this.where = where;
        this.groupBy = groupBy;
        this.having = having;
        this.setOperation = setOperation;
        this.alias = alias;

        if(this.where != null) {
            this.setCondition(this.where.getAnalyzingCondition());
        }
        if(this.having != null && this.getCondition() == null) {
            this.setCondition(this.having.getAnalyzingCondition());
        }
    }

    public AnalysisAttribute getAnalysisAttribute() {
        return this.analysisAttribute;
    }

    public static class AnalysisAttribute {
        @JsonProperty("attribute")
        Attribute attribute;

        @JsonProperty("variationType")
        Object variationType;

        @JsonProperty("query")
        Query query;

        public Query getQuery() {
            return this.query;
        }

        public Object getVariationType() {
            return this.variationType;
        }

        public Attribute getAttribute() {
            return this.attribute;
        }
    }

    private static class Join {
        @JsonProperty("type")
        private String joinType;

        @JsonProperty("selection")
        private Selection selection;

        @JsonProperty("on")
        private Condition on;

        public String toString() {
            return this.joinType + " " + this.selection.toString() + " on " + this.on.getCondition();
        }
    }

    private static class Selection {
        @JsonProperty("table")
        String table;

        @JsonProperty("selection")
        Query selection;

        @JsonProperty("isTable")
        boolean isTable;

        @JsonProperty("alias")
        String alias;

        public String toString() {
            String result = this.isTable ? table : "( " + this.selection.getQuery() + " )";

            if(this.alias != null && this.alias.length() > 0) {
                result += " as " + alias;
            }

            return result;
        }
    }

    private static class SetOperation {
        @JsonProperty("type")
        String setOperation;

        @JsonProperty("other")
        Query other;

        public String toString() {
            return " " + this.setOperation + " " + this.other.getQuery();
        }
    }



    @Override
    public String getQuery() {
        Iterator<Attribute> attributeIterator = this.attributes.iterator();
        String select = "select " + attributeIterator.next().toString();
        while(attributeIterator.hasNext()) {
            select += " , " + attributeIterator.next().toString();
        }

        String from = " from " + this.from.toString();

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
