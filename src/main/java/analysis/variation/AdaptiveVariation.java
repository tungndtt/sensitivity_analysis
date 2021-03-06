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

                Util.Functional[] functions = new Util.Functional[2];

                if(originalValue instanceof NumericalValue || originalValue instanceof DateValue) {

                    Class type = originalValue.getValue().getClass();

                    functions[0] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
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

                            previousSize = Util.add(type, size, previousSize);
                            changingSizes.add(Util.reverse(type, previousSize));
                            size = Util.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

                            previousDiff = calcDiff;
                            variedValue = condition.getValue().decrease(size);

                            ++iterations;
                        }
                    };

                    functions[1] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Number size = this.initialUnit;
                        Value variedValue = condition.getValue().increase(size);

                        int iterations = 0;
                        double previousDiff = 0;
                        Number previousSize = 0;

                        while (((Comparable) variedValue.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                            condition.setValue(variedValue);
                            Object variedBase = this.getMetric().analyze();

                            double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                            changingRates.add(calcDiff);

                            previousSize = Util.add(type, size, previousSize);
                            changingSizes.add(previousSize);
                            size = Util.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

                            previousDiff = calcDiff;
                            variedValue = condition.getValue().increase(size);

                            ++iterations;
                        }
                    };

                    Util.handleFunctions(originalValue, condition, result, functions);
                }
                else if(originalValue instanceof IntervalValue) {
                    Class type = ((Value[])originalValue.getValue())[0].getValue().getClass();

                    functions[0] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
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

                            previousSize = Util.add(type, size, previousSize);
                            changingSizes.add(Util.reverse(type, previousSize));
                            size = Util.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

                            previousDiff = calcDiff;

                            new_value = condition.getValue().decrease(size, size);
                            obj = (Value[]) new_value.getValue();
                            iterations++;
                        }
                    };

                    functions[1] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Number size = this.initialUnit;
                        Value variedValue = condition.getValue().increase(size);

                        int iterations = 0;
                        double previousDiff = 0;
                        Number previousSize = 0;

                        while (((Comparable) variedValue.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                            condition.setValue(variedValue);
                            Object variedBase = this.getMetric().analyze();

                            double calcDiff = this.getMetric().calculateDiff(base, variedBase);
                            changingRates.add(calcDiff);

                            previousSize = Util.add(type, size, previousSize);
                            changingSizes.add(previousSize);
                            size = Util.scale(type, size, (1 - Math.abs(calcDiff - previousDiff)) * this.alpha);

                            previousDiff = calcDiff;
                            variedValue = condition.getValue().increase(size);

                            ++iterations;
                        }
                    };

                    Util.handleFunctions(originalValue, condition, result, functions);
                }
                else {
                    System.out.println("Unsupported value for adaptive variation!");
                }
            }
        }

        return result;
    }
}
