package analysis.variation;

import java.util.HashMap;

public class Pair<V1, V2, V3> {
    private V1 value1;

    private V2 value2;

    private V3 value3;

    public V1 getValue1() {
        return value1;
    }

    public void setValue1(V1 value1) {
        this.value1 = value1;
    }

    public V2 getValue2() {
        return value2;
    }

    public void setValue2(V2 value2) {
        this.value2 = value2;
    }

    public V3 getValue3() {
        return value3;
    }

    public void setValue3(V3 value3) {
        this.value3 = value3;
    }

    public String toString(String prefix1, String prefix2, String prefix3) {
        return String.format("%s: %s , %s: %s , %s: %s", prefix1, value1.toString(), prefix2, value2.toString(), prefix3, value3.toString());
    }
}
