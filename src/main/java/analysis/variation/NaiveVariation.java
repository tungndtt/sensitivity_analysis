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

                Util.Functional[] functions = new Util.Functional[2];

                if(originValue instanceof DateValue || originValue instanceof NumericalValue) {

                    Class type = originValue.getValue().getClass();

                    functions[0] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Value obj = condition.getValue().decrease(this.unit);
                        int iterations = 0;
                        while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                            Object variedBase = this.getMetric().analyze();

                            double diffValue = this.getMetric().calculateDiff(base, variedBase);
                            changingRates.add(diffValue);

                            changingSizes.add(Util.reverse(type, Util.scale(type, this.unit, ++iterations)));

                            obj = condition.getValue().decrease(this.unit);
                            condition.setValue(obj);
                        }
                    };

                    functions[1] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Value obj = condition.getValue().increase(this.unit);
                        int iterations = 0;
                        while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                            Object variedBase = this.getMetric().analyze();

                            double diffValue = this.getMetric().calculateDiff(base, variedBase);
                            changingRates.add(diffValue);

                            changingSizes.add(Util.scale(type, this.unit, ++iterations));

                            obj = condition.getValue().increase(this.unit);
                            condition.setValue(obj);
                        }
                    };

                    Util.handleFunctions(originValue, condition, result, functions);
                }
                else if(originValue instanceof IntervalValue) {

                    Class type = ((Value[])originValue.getValue())[0].getValue().getClass();

                    functions[0] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Value new_value = condition.getValue().decrease(this.unit, this.unit);
                        Value[] obj = (Value[]) new_value.getValue();
                        int iterations = 0;
                        while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) <= 0) {
                            Object variedBase = this.getMetric().analyze();

                            double diffValue = this.getMetric().calculateDiff(base, variedBase);

                            changingRates.add(diffValue);

                            changingSizes.add(Util.reverse(type, Util.scale(type, this.unit, ++iterations)));

                            new_value = condition.getValue().decrease(this.unit, this.unit);
                            obj = (Value[]) new_value.getValue();
                            condition.setValue(new_value);
                        }
                    };
                    functions[1] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                        Value new_value = condition.getValue().increase(this.unit, this.unit);
                        Value[] obj = (Value[]) new_value.getValue();

                        int iterations = 0;
                        while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
                            Object variedBase = this.getMetric().analyze();

                            double diffValue = this.getMetric().calculateDiff(base, variedBase);
                            changingRates.add(diffValue);

                            changingSizes.add(Util.scale(type, this.unit, ++iterations));

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
                    };

                    Util.handleFunctions(originValue, condition, result, functions);
                }
                else {
                    System.out.println("Unsupported value type for naive variation!");
                }
            }
        }

        return result;
    }
}
