package analysis.variation;

import analysis.Pair;
import com.fasterxml.jackson.annotation.JsonProperty;
import component.condition.Condition;
import component.value.DateValue;
import component.value.IntervalValue;
import component.value.NumericalValue;
import component.value.Value;
import query.common.CommonQuery;
import java.util.Date;
import java.sql.ResultSet;
import java.util.LinkedList;

/**
 * Implementation the naive variation, which varies fixed size after each iteration
 * Progress:
 * repeat(numberOfIterations):
 *      value = value +/- unit
 *      calcDiff( query(origin) , query(value) )
 *
 * @author Tung Doan
 */
public class NaiveVariation extends Variation{

    @JsonProperty("unit")
    private Number unit;

    @JsonProperty("iteration")
    private int numberOfIterations;

    public NaiveVariation() {
        super(VariationType.NAIVE);
    }

    public NaiveVariation(CommonQuery commonQuery) {
        super(VariationType.NAIVE);
        this.setCommonQuery(commonQuery);
    }

    private Object[] getMinMaxRange(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());
        if(query == null) {
            return null;
        }
        try {
            //System.out.println(query);
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            Object[] range = new Object[2];

            while (resultSet.next()) {
                range[0] = resultSet.getObject("minimum");
                range[1] = resultSet.getObject("maximum");
            }

            return range;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void setUnitAndNumberOfIterations(Number unit, int numberOfIterations) {
        this.unit = unit;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> vary(String attribute) {

        LinkedList<Condition> conditions = this.getVaryingConditions();

        if(conditions == null || this.getMetric() == null) {
            return null;
        }

        Object[] minMaxRange = this.getMinMaxRange(attribute);

        if(minMaxRange == null) {
            return null;
        }
        else if(minMaxRange[0] instanceof Integer) {
            this.unit = this.unit.intValue();
        }
        else if(minMaxRange[0] instanceof Double) {
            this.unit = this.unit.doubleValue();
        }
        else if(minMaxRange[0] instanceof Long) {
            this.unit = this.unit.longValue();
        }
        else if(minMaxRange[0] instanceof Date) {
            this.unit = this.unit.longValue();
        }
        else {
            System.out.println("Unsupported type for naive variation!");
            return null;
        }

        LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result = new LinkedList<>();

        for(Condition condition : conditions) {
            if(condition.getAttribute().toString().equals(attribute)) {
                Value originValue = condition.getValue();
                this.getMetric().setCommonQuery(this.getCommonQuery());
                Object base = this.getMetric().analyze();

                if(originValue instanceof DateValue || originValue instanceof NumericalValue) {
                    this.handleValue(originValue, base, condition, minMaxRange, result);
                }
                else if(originValue instanceof IntervalValue) {
                    this.handleInterval(originValue, base, condition, minMaxRange, result);
                }
                else {
                    System.out.println("Unsupported value type for naive variation!");
                }
            }
        }

        return result;
    }

    private void handleValue(Value originValue, Object base, Condition condition, Object[] minMaxRange,
                             LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        Class type = originValue.getValue().getClass();

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
        negative.setValue1(String.format("condition : %s", condition.getCondition()));

        Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
        LinkedList<Number> changingSizes = new LinkedList<>();
        LinkedList<Double> changingRates = new LinkedList<>();

        changingRates.add(0.0);
        changingSizes.add(0);

        Value obj = condition.getValue().decrease(this.unit);
        int iterations = 0;
        while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
            Object variedBase = this.getMetric().analyze();

            double diffValue = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(diffValue);

            changingSizes.add(NaiveVariation.reverse(type, NaiveVariation.scale(type, this.unit, ++iterations)));

            obj = condition.getValue().decrease(this.unit);
            condition.setValue(obj);
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        negative.setValue2(part);

        condition.setValue(originValue);

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
        positive.setValue1(String.format("condition : %s", condition.getCondition()));

        part = new Pair<>();
        changingSizes = new LinkedList<>();
        changingRates = new LinkedList<>();

        obj = condition.getValue().increase(this.unit);
        iterations = 0;
        while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
            Object variedBase = this.getMetric().analyze();

            double diffValue = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(diffValue);

            changingSizes.add(NaiveVariation.scale(type, this.unit, ++iterations));

            obj = condition.getValue().increase(this.unit);
            condition.setValue(obj);
        }

        condition.setValue(originValue);

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        positive.setValue2(part);

        result.add(positive);
        result.add(negative);
    }

    private void handleInterval(Value originValue, Object base, Condition condition, Object[] minMaxRange,
                                LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        Class type = ((Value[])originValue.getValue())[0].getValue().getClass();

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
        negative.setValue1(String.format("condition : %s", condition.getCondition()));

        Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
        LinkedList<Double> changingRates = new LinkedList<>();
        LinkedList<Number> changingSizes = new LinkedList<>();

        changingRates.add(0.0);
        changingSizes.add(0);

        Value new_value = condition.getValue().decrease(this.unit, this.unit);
        Value[] obj = (Value[]) new_value.getValue();
        int iterations = 0;
        while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) <= 0) {
            Object variedBase = this.getMetric().analyze();

            double diffValue = this.getMetric().calculateDiff(base, variedBase);

            changingRates.add(diffValue);

            changingSizes.add(NaiveVariation.reverse(type, NaiveVariation.scale(type, this.unit, ++iterations)));

            new_value = condition.getValue().decrease(this.unit, this.unit);
            obj = (Value[]) new_value.getValue();
            condition.setValue(new_value);
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        negative.setValue2(part);

        condition.setValue(originValue);

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
        positive.setValue1(String.format("condition : %s", condition.getCondition()));

        part = new Pair<>();
        changingRates = new LinkedList<>();
        changingSizes = new LinkedList<>();

        new_value = condition.getValue().increase(this.unit, this.unit);
        obj = (Value[]) new_value.getValue();

        iterations = 0;
        while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
            Object variedBase = this.getMetric().analyze();

            double diffValue = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(diffValue);

            changingSizes.add(NaiveVariation.scale(type, this.unit, ++iterations));

            Object left = null, right = null;
            if(((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) > 0) {
                left = this.unit;
            }
            if(((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) < 0) {
                right = this.unit;
            }

            if(left == null && right == null) {
                break;
            }
            new_value = condition.getValue().increase(left == null ? 0 : left, right == null ? 0 : right);
            condition.setValue(new_value);
            obj = (Value[]) new_value.getValue();
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        positive.setValue2(part);

        condition.setValue(originValue);

        result.add(positive);
        result.add(negative);
    }

    private static Number scale(Class type, Number unit, int scale) {
        if(type == Integer.class) {
            return unit.intValue()*scale;
        }
        else if(type == Long.class) {
            return unit.longValue()*scale;
        }
        else if(type == Double.class) {
            return unit.doubleValue()*scale;
        }
        else if(type == Date.class) {
            return unit.longValue()*scale;
        }
        return null;
    }

    private static Number reverse(Class type, Number unit) {
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
}
