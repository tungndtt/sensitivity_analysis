package analysis.variation;

import condition.Condition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.NumericalValue;
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

        LinkedList<Pair> result = new LinkedList<>();

        int condIdx = 1;
        LinkedList<Condition> conditions = this.getVaryingConditions();
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

        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Pair pair = new Pair(String.format("condition %d : %s", condIdx++, condition.getCondition()));
                Object originValue = condition.getValue().getValue();
                this.getMetric().setCommonQuery(this.getCommonQuery());
                Object base = this.getMetric().analyze();

                if(condition.getValue() instanceof DateValue || condition.getValue() instanceof NumericalValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();

                    Object obj = condition.getValue().decrease(this.unit);
                    int iterations = 0;
                    String prefix = "Plus ";
                    while(((Comparable) obj).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        condition.getValue().setValue(obj);
                        this.getMetric().setCommonQuery(this.getCommonQuery());
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit.toString(), diffValue);

                        obj = condition.getValue().decrease(this.unit);
                        iterations++;
                    }

                    condition.getValue().setValue(originValue);

                    obj = condition.getValue().increase(this.unit);
                    iterations = 0;
                    prefix = "Minus ";
                    while(((Comparable) obj).compareTo(minMaxRange[0]) >= 0 && ((Comparable) obj).compareTo(minMaxRange[1]) <= 0 && iterations < this.numberOfIterations) {
                        condition.getValue().setValue(obj);
                        this.getMetric().setCommonQuery(this.getCommonQuery());
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit.toString(), diffValue);

                        obj = condition.getValue().increase(this.unit);
                        iterations++;
                    }

                    condition.getValue().setValue(originValue);

                    pair.setChangingRate(changingRates);
                    result.add(pair);
                }
                else if(condition.getValue() instanceof IntervalValue) {
                    HashMap<String, Double> changingRates = new HashMap<>();

                    Object[] obj = (Object[]) condition.getValue().decrease(this.unit, this.unit);
                    int iterations = 0;
                    String prefix = "Shrink ";
                    while(iterations < this.numberOfIterations && ((Comparable) obj[0]).compareTo(obj[1]) <= 0) {
                        condition.getValue().setValue(obj);
                        this.getMetric().setCommonQuery(this.getCommonQuery());
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit, diffValue);

                        obj = (Object[]) condition.getValue().decrease(this.unit, this.unit);
                        iterations++;
                    }

                    condition.getValue().setValue(originValue);

                    obj = (Object[]) condition.getValue().increase(this.unit, this.unit);
                    iterations = 0;
                    prefix = "Extend ";
                    while(iterations < this.numberOfIterations && (((Comparable) obj[0]).compareTo(minMaxRange[0]) >= 0 || ((Comparable) obj[1]).compareTo(minMaxRange[1]) <= 0)) {
                        condition.getValue().setValue(obj);
                        this.getMetric().setCommonQuery(this.getCommonQuery());
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.put(prefix + (iterations+1) + "*" + this.unit, diffValue);

                        if(((Comparable) obj[0]).compareTo(minMaxRange[0]) > 0 && ((Comparable) obj[1]).compareTo(minMaxRange[1]) < 0) {
                            obj = (Object[]) condition.getValue().decrease(this.unit, this.unit);
                        }
                        else if(((Comparable) obj[0]).compareTo(minMaxRange[0]) > 0) {
                            obj = (Object[]) condition.getValue().decrease(this.unit, 0);
                        }
                        else if(((Comparable) obj[1]).compareTo(minMaxRange[1]) < 0) {
                            obj = (Object[]) condition.getValue().decrease(0, this.unit);
                        }
                        else break;
                        iterations++;
                    }

                    condition.getValue().setValue(originValue);

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
