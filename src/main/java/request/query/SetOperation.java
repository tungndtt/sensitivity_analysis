package request.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import component.condition.Condition;

class SetOperation {

    private enum OperationType {
        UNION("union"),
        EXCEPT("except"),
        INTERSECT("intersect");

        String operation;
        OperationType(String operation) {
            this.operation = operation;
        }

        @JsonCreator
        static OperationType forValue(String operation) {
            if(operation.equals(UNION.operation)) {
                return UNION;
            }
            else if(operation.equals(EXCEPT.operation)) {
                return EXCEPT;
            }
            else if(operation.equals(INTERSECT.operation)) {
                return INTERSECT;
            }
            else {
                return null;
            }
        }
    }

    @JsonProperty("type")
    OperationType setOperation;

    @JsonProperty("other")
    Selection other;

    public String toString() {
        return " " + this.setOperation + " " + this.other.getQuery();
    }

    Condition getAnalyzingCondition() {
        if(this.other != null) {
            return this.other.getAnalyzingCondition();
        }
        else {
            return null;
        }
    }
}
