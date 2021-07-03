package analysis.variation;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VariationType {
    NAIVE("naive"), // varying fixed unit
    AVERAGE("average"), // varying unit based on average distance in value set
    ADAPTIVE("adaptive"), // varying unit based on changing rate from previous variation
    SET("set"); // varying elements in set

    private String type;

    VariationType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static VariationType forValue(String type) {
        if(type.equals(NAIVE.type)) {
            return NAIVE;
        }
        else if(type.equals(AVERAGE.type)) {
            return AVERAGE;
        }
        else if(type.equals(ADAPTIVE.type)) {
            return ADAPTIVE;
        }
        else if(type.equals(SET.type)) {
            return SET;
        }
        else {
            return null;
        }
    }
}
