package analysis.variation;

import analysis.Pair;
import condition.Condition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.NumericalValue;
import condition.value.Value;
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

    private Number unit;

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
            System.out.println(query);
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
    public LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> vary(String attribute) {

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

        LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> result = new LinkedList<>();

        int condIdx = 1;

        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Value originValue = condition.getValue();
                this.getMetric().setCommonQuery(this.getCommonQuery());
                Object base = this.getMetric().analyze();

                if(condition.getValue() instanceof DateValue || condition.getValue() instanceof NumericalValue) {

                    Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
                    negative.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));

                    Pair<Number, LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
                    part.setValue1(this.unit);
                    LinkedList<Number> changingSizes = new LinkedList<>();
                    LinkedList<Double> changingRates = new LinkedList<>();

                    changingRates.add(0.0);
                    changingSizes.add(0);

                    Value obj = condition.getValue().decrease(this.unit);
                    int iterations = 0;
                    negative.setValue2(-1);
                    while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.add(diffValue);

                        changingSizes.add(++iterations);

                        obj = condition.getValue().decrease(this.unit);
                        condition.setValue(obj);
                    }

                    part.setValue2(changingSizes);
                    part.setValue3(changingRates);
                    negative.setValue3(part);

                    condition.setValue(originValue);

                    Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
                    positive.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));

                    part = new Pair<>();
                    part.setValue1(this.unit);
                    changingSizes = new LinkedList<>();
                    changingRates = new LinkedList<>();

                    obj = condition.getValue().increase(this.unit);
                    iterations = 0;
                    positive.setValue2(1);
                    while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.add(diffValue);

                        changingSizes.add(++iterations);

                        obj = condition.getValue().increase(this.unit);
                        condition.setValue(obj);
                    }

                    condition.setValue(originValue);

                    part.setValue2(changingSizes);
                    part.setValue3(changingRates);
                    positive.setValue3(part);

                    result.add(positive);
                    result.add(negative);

                    condIdx++;
                }
                else if(condition.getValue() instanceof IntervalValue) {

                    Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
                    negative.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));

                    Pair<Number, LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
                    part.setValue1(this.unit);
                    LinkedList<Double> changingRates = new LinkedList<>();
                    LinkedList<Number> changingSizes = new LinkedList<>();

                    changingRates.add(0.0);
                    changingSizes.add(0);

                    Value new_value = condition.getValue().decrease(this.unit, this.unit);
                    Value[] obj = (Value[]) new_value.getValue();
                    int iterations = 0;
                    negative.setValue2(-1);
                    while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) <= 0) {
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);

                        changingRates.add(diffValue);

                        changingSizes.add(++iterations);

                        new_value = condition.getValue().decrease(this.unit, this.unit);
                        obj = (Value[]) new_value.getValue();
                        condition.setValue(new_value);
                    }

                    part.setValue2(changingSizes);
                    part.setValue3(changingRates);
                    negative.setValue3(part);

                    System.out.println(condition.getValue().toString());
                    condition.setValue(originValue);

                    Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
                    positive.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));
                    part = new Pair<>();
                    part.setValue1(this.unit);
                    changingRates = new LinkedList<>();
                    changingSizes = new LinkedList<>();

                    new_value = condition.getValue().increase(this.unit, this.unit);
                    obj = (Value[]) new_value.getValue();

                    iterations = 0;
                    positive.setValue2(1);
                    while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.add(diffValue);

                        changingSizes.add(++iterations);

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

                    part.setValue2(changingSizes);
                    part.setValue3(changingRates);
                    positive.setValue3(part);

                    condition.setValue(originValue);

                    result.add(positive);
                    result.add(negative);

                    condIdx++;
                }
                else {
                    System.out.println("Unsupported value type for naive variation!");
                }
            }
        }

        return result;
    }
}
