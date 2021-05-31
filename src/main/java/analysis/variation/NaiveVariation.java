package analysis.variation;

import condition.Condition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.NumericalValue;
import condition.value.Value;
import query.common.CommonQuery;

import java.util.Date;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;

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
    public LinkedList<Pair> vary(String attribute) {

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

        LinkedList<Pair> result = new LinkedList<>();

        int condIdx = 1;

        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Pair pair = new Pair(String.format("condition %d : %s", condIdx++, condition.getCondition()));
                Value originValue = condition.getValue();
                this.getMetric().setCommonQuery(this.getCommonQuery());
                Object base = this.getMetric().analyze();

                if(condition.getValue() instanceof DateValue || condition.getValue() instanceof NumericalValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();

                    Value obj = condition.getValue().decrease(this.unit);
                    int iterations = 0;
                    String prefix = "Minus ";
                    while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        condition.setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit.toString(), diffValue);

                        obj = condition.getValue().decrease(this.unit);
                        iterations++;
                    }

                    condition.setValue(originValue);

                    obj = condition.getValue().increase(this.unit);
                    iterations = 0;
                    prefix = "Plus ";
                    while(((Comparable) obj.getValue()).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj.getValue()).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        condition.setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit.toString(), diffValue);

                        obj = condition.getValue().increase(this.unit);
                        iterations++;
                    }

                    condition.setValue(originValue);

                    pair.setChangingRate(changingRates);
                    result.add(pair);
                }
                else if(condition.getValue() instanceof IntervalValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();

                    Value[] obj = (Value[]) condition.getValue().decrease(this.unit, this.unit).getValue();
                    int iterations = 0;
                    String prefix = "Shrink ";
                    while(iterations < this.numberOfIterations && ((Comparable) obj[0].getValue()).compareTo(obj[1].getValue()) <= 0) {
                        condition.getValue().setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit, diffValue);

                        obj = (Value[]) condition.getValue().decrease(this.unit, this.unit).getValue();
                        iterations++;
                    }

                    condition.setValue(originValue);

                    obj = (Value[]) condition.getValue().increase(this.unit, this.unit).getValue();
                    iterations = 0;
                    prefix = "Extend ";
                    while(iterations < this.numberOfIterations && (((Comparable) obj[0].getValue()).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1].getValue()).compareTo(minMaxRange[1]) <= 0)) {
                        condition.getValue().setValue(obj);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit, diffValue);

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
                        obj = (Value[]) condition.getValue().increase(left == null ? 0 : left, right == null ? 0 : right).getValue();

                        iterations++;
                    }

                    condition.setValue(originValue);

                    pair.setChangingRate(changingRates);
                    result.add(pair);
                }
                else {
                    System.out.println("Unsupported value type for naive variation!");
                }
            }
        }

        return result;
    }
}
