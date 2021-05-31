package analysis.variation;

import java.util.HashMap;

public class Pair {
    private String condition;

    private HashMap<String, Double> changingRates;

    public Pair(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setChangingRate(HashMap<String, Double> changingRates) {
        this.changingRates = changingRates;
    }

    public HashMap<String, Double> getChangingRate() {
        return this.changingRates;
    }
}
