package request.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import component.condition.Condition;

class Join {
    @JsonProperty("type")
    private String joinType;

    @JsonProperty("selection")
    private Selection selection;

    @JsonProperty("on")
    private Condition on;

    public String toString() {
        return this.joinType + " " + this.selection.getQuery() + " on " + this.on.getCondition();
    }

    Condition getAnalyzingCondition() {
        Condition cond = null;
        if(this.on != null) {
            cond = this.on.getAnalyzingCondition();
        }
        if(this.selection != null && cond == null){
            cond = this.selection.getAnalyzingCondition();
        }
        return cond;
    }
}
