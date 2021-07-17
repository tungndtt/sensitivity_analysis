package analysis;

public class Pair<V1, V2> {
    private V1 value1;

    private V2 value2;

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

    public String toString(String prefix1, String prefix2, String prefix3) {
        return String.format("%s: %s , %s: %s", prefix1, value1.toString(), prefix2, value2.toString(), prefix3);
    }
}
