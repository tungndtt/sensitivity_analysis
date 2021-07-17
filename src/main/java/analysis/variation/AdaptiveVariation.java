package analysis.variation;

import analysis.Pair;
import com.fasterxml.jackson.annotation.JsonProperty;
import component.condition.Condition;
import component.value.DateValue;
import component.value.IntervalValue;
import component.value.NumericalValue;
import component.value.Value;
import query.common.CommonQuery;
import java.sql.ResultSet;
import java.util.*;

/**
 * Implementation of adaptive variation, which adapts the variation size based on changing rate of previous step
 * Progress:
 * repeat(numberOfIterations):
 *      value = value +/- initialUnit * alpha * (1 - changing_rate)
 *      calcDiff( query(origin) , query(value) )
 *
 * @author Tung Doan
 */
public class AdaptiveVariation extends Variation {

    @JsonProperty("iteration")
    private int numberOfIterations;

    @JsonProperty("initial unit")
    private Number initialUnit;

    @JsonProperty("scale")
    private double alpha;

    public AdaptiveVariation() {
        super(VariationType.ADAPTIVE);
    }

    public AdaptiveVariation(CommonQuery commonQuery) {
        super(VariationType.ADAPTIVE);
        this.setCommonQuery(commonQuery);
    }

    private Object[] getMinMaxRange(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());
        if(query == null) {
            return null;
        }
        try {
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            Object[] result = new Object[2];

            while (resultSet.next()) {
                result[0] = resultSet.getObject("minimum");
                result[1] = resultSet.getObject("maximum");
            }

            return result;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public void setInitialUnitAndAlpha(Number initialUnit, double alpha) {
        this.initialUnit = initialUnit;
        this.alpha = alpha;
    }

    @Override
    public LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> vary(String attribute) {

        Object[] minMaxRange = this.getMinMaxRange(attribute);

        LinkedList<Condition> conditions = this.getVaryingConditions();
        if(conditions == null || this.getMetric() == null || minMaxRange == null) {
            return null;
        }

        LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result = new LinkedList<>();

        for(Condition condition : conditions) {
            if(condition.getAttribute().toString().equals(attribute)) {
                Value originalValue = condition.getValue();
                Object base = this.getMetric().analyze();

                if(base == null) {
                    return null;
                }

                if(originalValue instanceof NumericalValue || originalValue instanceof DateValue) {
                    this.handleValue(originalValue, base, condition, minMaxRange, result);
                }
                else if(originalValue instanceof IntervalValue) {
                    this.handleInterval(originalValue, base, condition, minMaxRange, result);
                }
                else {
                    System.out.println("Unsupported value for adaptive variation!");
                }
            }
        }

        return result;
    }

    private void handleValue(Value originalValue, Object base, Condition condition, Object[] minMaxRange,
                             LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
        negative.setValue1(String.format("condition : %s", condition.getCondition()));

        Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
        LinkedList<Number> changingSizes = new LinkedList<>();
        LinkedList<Double> changingRates = new LinkedList<>();

        changingRates.add(0.0);
        changingSizes.add(0);

        Class type = originalValue.getValue().getClass();
        Number size = this.initialUnit;

        Value variedValue = condition.getValue().decrease(size);

        int iterations = 0;
        double previousDiff = 0;
        Number previousSize = 0;

        while (((Comparable)variedValue.getValue()).compareTo(minMaxRange[0]) >= 0 && iterations < this.numberOfIterations) {
            condition.setValue(variedValue);
            Object variedBase = this.getMetric().analyze();

            double calcDiff = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(calcDiff);

            previousSize = AdaptiveVariation.add(type, size, previousSize);
            changingSizes.add(AdaptiveVariation.reverse(type, previousSize));
            size = AdaptiveVariation.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

            previousDiff = calcDiff;
            variedValue = condition.getValue().decrease(size);

            ++iterations;
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        negative.setValue2(part);

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
        negative.setValue1(String.format("condition : %s", condition.getCondition()));

        part = new Pair<>();
        changingSizes = new LinkedList<>();
        changingRates = new LinkedList<>();

        condition.setValue(originalValue);
        size = this.initialUnit;
        variedValue = condition.getValue().increase(size);

        iterations = 0;
        previousDiff = 0;
        previousSize = 0;

        while (((Comparable) variedValue.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
            condition.setValue(variedValue);
            Object variedBase = this.getMetric().analyze();

            double calcDiff = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(calcDiff);

            previousSize = AdaptiveVariation.add(type, size, previousSize);
            changingSizes.add(previousSize);
            size = AdaptiveVariation.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

            previousDiff = calcDiff;
            variedValue = condition.getValue().increase(size);

            ++iterations;
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        positive.setValue2(part);

        condition.setValue(originalValue);

        result.add(positive);
        result.add(negative);
    }

    private void handleInterval(Value originalValue, Object base, Condition condition, Object[] minMaxRange,
                             LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result) {
        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
        negative.setValue1(String.format("condition : %s", condition.getCondition()));

        Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
        LinkedList<Double> changingRates = new LinkedList<>();
        LinkedList<Number> changingSizes = new LinkedList<>();

        changingRates.add(0.0);
        changingSizes.add(0);

        Class type = ((Value[])originalValue.getValue())[0].getValue().getClass();
        Number size = this.initialUnit;

        Value new_value = condition.getValue().decrease(size, size);
        Value[] obj = (Value[]) new_value.getValue();
        int iterations = 0;
        double previousDiff = 0;
        Number previousSize = 0;

        while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) < 0) {
            condition.setValue(new_value);
            Object variedBase = this.getMetric().analyze();

            double calcDiff = this.getMetric().calculateDiff(base, variedBase);

            changingRates.add(calcDiff);

            previousSize = AdaptiveVariation.add(type, size, previousSize);
            changingSizes.add(AdaptiveVariation.reverse(type, previousSize));
            size = AdaptiveVariation.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

            previousDiff = calcDiff;

            new_value = condition.getValue().decrease(size, size);
            obj = (Value[]) new_value.getValue();
            iterations++;
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        negative.setValue2(part);

        condition.setValue(originalValue);

        Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
        positive.setValue1(String.format("condition : %s", condition.getCondition()));

        part = new Pair<>();
        changingRates = new LinkedList<>();
        changingSizes = new LinkedList<>();

        size = this.initialUnit;

        new_value = condition.getValue().increase(size, size);
        obj = (Value[]) new_value.getValue();
        iterations = 0;
        previousDiff = 0;
        previousSize = 0;

        while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
            condition.setValue(new_value);
            Object variedBase = this.getMetric().analyze();

            double calcDiff = this.getMetric().calculateDiff(base, variedBase);
            changingRates.add(calcDiff);

            previousSize = AdaptiveVariation.add(type, size, previousSize);
            changingSizes.add(previousSize);
            size = AdaptiveVariation.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

            previousDiff = calcDiff;

            Object left = null, right = null;
            if(((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) > 0) {
                left = size;
            }
            if(((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) < 0) {
                right = size;
            }

            if(left == null && right == null) {
                break;
            }

            new_value = condition.getValue().increase(left == null ? 0 : left, right == null ? 0 : right);
            obj = (Value[]) new_value.getValue();

            iterations++;
        }

        part.setValue1(changingSizes);
        part.setValue2(changingRates);
        positive.setValue2(part);

        condition.setValue(originalValue);

        result.add(positive);
        result.add(negative);
    }

    private static Number scale(Class type, Number unit, double alpha) {
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

    private static Number add(Class type, Number num1, Number num2) {
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
}
