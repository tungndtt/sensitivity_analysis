package analysis.variation;

import condition.Condition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.NumericalValue;
import condition.value.Value;
import query.common.CommonQuery;

import java.sql.ResultSet;
import java.util.*;

public class AdaptiveVariation extends Variation {

    private int numberOfIterations;

    private Number initialUnit;

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
    public LinkedList<Pair> vary(String attribute) {

        Object[] minMaxRange = this.getMinMaxRange(attribute);

        LinkedList<Condition> conditions = this.getVaryingConditions();
        if(conditions == null || this.getMetric() == null) {
            return null;
        }

        LinkedList<Pair> result = new LinkedList<>();

        int condIdx = 1;
        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Pair pair = new Pair(String.format("Condition %d: %s", condIdx++, condition.getCondition()));
                Value originalValue = condition.getValue();
                Object base = this.getMetric().analyze();

                if(base == null) {
                    return null;
                }

                if(originalValue instanceof NumericalValue || originalValue instanceof DateValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();
                    Class type = originalValue.getValue().getClass();
                    Number size = this.scale(type, this.initialUnit, 1);

                    Value variedValue = condition.getValue().decrease(size);
                    int iterations = 0;
                    String prefix = "Minus in iteration ";
                    double previousDiff = 0;
                    while (((Comparable)variedValue.getValue()).compareTo(minMaxRange[0]) >= 0 && iterations < this.numberOfIterations) {
                        condition.setValue(variedValue);
                        Object variedBase = this.getMetric().analyze();

                        double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + ".th with size " + size.toString(), calcDiff);

                        size = this.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);
                        previousDiff = calcDiff;
                        variedValue = condition.getValue().decrease(size);

                        ++iterations;
                    }

                    condition.setValue(originalValue);
                    size = this.scale(type, this.initialUnit, 1);
                    variedValue = condition.getValue().increase(size);

                    iterations = 0;
                    prefix = "Plus in iteration ";
                    previousDiff = 0;
                    while (((Comparable) variedValue.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        condition.setValue(variedValue);
                        Object variedBase = this.getMetric().analyze();

                        double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + ".th with size " + size.toString(), calcDiff);

                        size = this.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);
                        previousDiff = calcDiff;
                        variedValue = condition.getValue().increase(size);

                        ++iterations;
                    }

                    condition.setValue(originalValue);

                    pair.setChangingRate(changingRates);
                    result.add(pair);
                }
                else if(originalValue instanceof IntervalValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();

                    Class type = originalValue.getValue().getClass();
                    Number size = this.scale(type, this.initialUnit, 1);

                    Value[] obj = (Value[]) condition.getValue().decrease(size, size).getValue();
                    int iterations = 0;
                    String prefix = "Shrink in iteration ";
                    double previousDiff = 0;

                    while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) <= 0) {
                        condition.getValue().setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + " with size " + size.toString(), calcDiff);

                        size = this.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);
                        previousDiff = calcDiff;

                        obj = (Value[]) condition.getValue().decrease(size, size).getValue();
                        iterations++;
                    }

                    condition.setValue(originalValue);
                    size = this.scale(type, this.initialUnit, 1);

                    obj = (Value[]) condition.getValue().increase(size, size).getValue();
                    iterations = 0;
                    prefix = "Extend in iteration ";
                    previousDiff = 0;

                    while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
                        condition.getValue().setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + " with size " + size.toString(), calcDiff);
                        size = this.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);
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
                        obj = (Value[]) condition.getValue().increase(left == null ? 0 : left, right == null ? 0 : right).getValue();

                        iterations++;
                    }

                    condition.setValue(originalValue);

                    pair.setChangingRate(changingRates);
                    result.add(pair);
                }
                else {
                    System.out.println("Unsupported value for adaptive variation!");
                }
            }
        }

        return result;
    }

    private Number scale(Class type, Number unit, double alpha) {
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
}
