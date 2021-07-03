package request.action;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum ActionType {
    VARIATION("variation"),
    DETERMINATION("determination");

    private String type;

    ActionType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static ActionType forValue(String type) {
        if(type.equals(VARIATION.type)) {
            return VARIATION;
        }
        else if(type.equals(DETERMINATION.type)) {
            return DETERMINATION;
        }
        else {
            return null;
        }
    }
}
