package analysis.variation;

import analysis.Pair;
import component.condition.Condition;
import component.value.Value;

import java.util.Date;
import java.util.LinkedList;

public class Util {
    @FunctionalInterface
    interface Functional {
        void runVariation(LinkedList<Number> changingSizes, LinkedList<Double> changingRates);
    }

    public static void handleFunctions(Value originValue, Condition condition, LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result, Functional[] functions) {

        Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
        LinkedList<Number> changingSizes = new LinkedList<>();
        LinkedList<Double> changingRates = new LinkedList<>();

        changingRates.add(0.0);
        changingSizes.add(0);

        for(Functional function : functions) {
            Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> _pair = new Pair<>();
            _pair.setValue1(String.format("condition : %s", condition.getCondition()));

            function.runVariation(changingSizes, changingRates);

            part.setValue1(changingSizes);
            part.setValue2(changingRates);
            _pair.setValue2(part);

            condition.setValue(originValue);
            result.add(_pair);
        }
    }

    public static Number reverse(Class type, Number unit) {
        if(type == Integer.class) {
            return -unit.intValue();
        }
        else if(type == Long.class) {
            return -unit.longValue();
        }
        else if(type == Double.class) {
            return -unit.doubleValue();
        }
        else if(type == Date.class) {
            return -unit.longValue();
        }
        return null;
    }

    public static Number scale(Class type, Number unit, double alpha) {
        if(type == Integer.class) {
            return unit.intValue()*alpha;
        }
        else if(type == Long.class) {
            return unit.longValue()*alpha;
        }
        else if(type == Double.class) {
            return unit.doubleValue()*alpha;
        }
        else if(type == Date.class) {
            return unit.longValue()*alpha;
        }
        return null;
    }

    public static Number add(Class type, Number num1, Number num2) {
        if(type == Integer.class) {
            return num1.intValue() + num2.intValue();
        }
        else if(type == Long.class) {
            return num1.longValue() + num2.longValue();
        }
        else if(type == Double.class) {
            return num1.doubleValue() + num2.doubleValue();
        }
        else if(type == Date.class) {
            return num1.longValue() + num2.longValue();
        }
        return null;
    }

    public static int compare(Number a, Number b) {
        if(a.getClass() == Long.class || b.getClass() == Long.class) {
            long res = a.longValue() - b.longValue();
            return res > 0 ? 1 : res < 0 ? -1 : 0;
        }
        else if(a.getClass() == Double.class || b.getClass() == Double.class) {
            double res = a.doubleValue() - b.doubleValue();
            return res > 0 ? 1 : res < 0 ? -1 : 0;
        }
        else if(a.getClass() == Integer.class || b.getClass() == Integer.class) {
            return a.intValue() - b.intValue();
        }
        else return 0;
    }
}
