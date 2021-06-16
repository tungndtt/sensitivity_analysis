package analysis.determinator;

import analysis.Pair;
import analysis.metric.Metric;
import condition.Condition;
import condition.value.DateValue;
import condition.value.IntervalValue;
import condition.value.NumericalValue;
import condition.value.Value;
import db_connection.DbConnection;
import query.common.DeterminableCommonQuery;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Determination {

    private DeterminableCommonQuery determinableCommonQuery;

    private double differenceBound;

    private Number precisionTolerance;

    private Metric metric;

    private HashMap<String, String> boundQueries;

    private LinkedList<Condition> allConditions;

    public void setDeterminableCommonQuery(DeterminableCommonQuery determinableCommonQuery) {
        this.determinableCommonQuery = determinableCommonQuery;
        this.boundQueries = this.determinableCommonQuery.getBoundQueries();
        this.retrieveAllConditions();
    }

    public DeterminableCommonQuery getDeterminableCommonQuery() {
        return this.determinableCommonQuery;
    }

    public double getDifferenceBound() {
        return  this.differenceBound;
    }

    public Number getPrecisionTolerance() {
        return this.precisionTolerance;
    }

    public void setDifferenceBoundAndIterationsPrecision(double differenceBound, Number precisionTolerance) {
        this.differenceBound = differenceBound;
        this.precisionTolerance = precisionTolerance;
    }

    public Metric getMetric() {
        return this.metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    private void retrieveAllConditions() {
        if(this.determinableCommonQuery != null) {
            this.allConditions = this.determinableCommonQuery.retrieveAllConditionsWithValue();
        }
    }

    private Object[] getBound(String attribute) {
        if(this.boundQueries.containsKey(attribute) && DbConnection.getConnection() != null) {
            String boundQuery = this.boundQueries.get(attribute);
            try {
                ResultSet resultSet = DbConnection.getConnection().prepareStatement(boundQuery).executeQuery();
                Object[] result = new Object[2];

                while (resultSet.next()) {
                    result[0] = resultSet.getObject("minimum");
                    result[1] = resultSet.getObject("maximum");
                }

                return result;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public LinkedList<Pair<String, Integer, Object[]>> determine(String attribute) {
        if(this.determinableCommonQuery == null || this.metric == null) {
            System.out.println("Query or metric is not provided yet!");
            return null;
        }
        Object[] bound = this.getBound(attribute);

        LinkedList<Pair<String, Integer, Object[]>> result = new LinkedList<>();

        for(Condition condition : this.allConditions) {
            if(condition.getAttribute().equals(attribute)) {
                Value value = condition.getValue();
                if(value instanceof NumericalValue || value instanceof DateValue) {
                    this.handleValue(condition, bound, result);
                }
                else if(value instanceof IntervalValue) {
                    this.handleInterval(condition, bound, result);
                }
            }
        }
        return result;
    }

    private void handleValue(Condition condition, Object[] bound, LinkedList<Pair<String, Integer, Object[]>> result) {
        Value originValue = condition.getValue();

        Object base = this.metric.analyze();

        Object[][] forIteration = {
                {originValue, bound[1], 1},
                {bound[0], originValue, -1}
        };

        for(Object[] iteration : forIteration) {
            if(((Comparable) iteration[0]).compareTo(iteration[1]) < 0) {

                Pair<String, Integer, Object[]> pair = new Pair<>();
                pair.setValue1("Condition: " + condition.getCondition());
                pair.setValue2((Integer) iteration[2]);

                Object[] object = new Object[4];

                Number min = 0, max = this.subtract(iteration[1], iteration[0]);
                double diff = 0;

                while(((Comparable) this.precisionTolerance).compareTo(this.subtract(max, min)) < 0) {
                    Number middle = this.divide(this.add(min, max), 2);
                    Value varied_value = (Integer) iteration[2] == 1 ? originValue.increase(middle) : originValue.decrease(middle);
                    condition.setValue(varied_value);
                    Object _varied = this.metric.analyze();
                    diff = this.metric.calculateDiff(base, _varied);

                    if(diff >= this.differenceBound) {
                        max = middle;
                    }
                    else {
                        min = middle;
                    }
                }
                object[0] = diff;
                object[1] = min;

                min = 0;
                max = this.subtract(iteration[1], iteration[0]);
                Value varied_value = (Integer) iteration[2] == 1 ? originValue.increase(max) : originValue.decrease(max);
                condition.setValue(varied_value);
                Object _varied = this.metric.analyze();
                diff = this.metric.calculateDiff(base, _varied);

                if(diff > this.differenceBound) {
                    while(((Comparable) this.precisionTolerance).compareTo(this.subtract(max, min)) < 0) {
                        Number middle = this.divide(this.add(min, max), 2);
                        varied_value = (Integer) iteration[2] == 1 ? originValue.increase(middle) : originValue.decrease(middle);
                        condition.setValue(varied_value);
                        _varied = this.metric.analyze();
                        diff = this.metric.calculateDiff(base, _varied);

                        if(diff > this.differenceBound) {
                            max = middle;
                        }
                        else {
                            min = middle;
                        }
                    }
                }
                object[2] = diff;
                object[3] = max;

                condition.setValue(originValue);
                pair.setValue3(object);
                result.add(pair);
            }
        }
    }

    private void handleInterval(Condition condition, Object[] bound, LinkedList<Pair<String, Integer, Object[]>> result) {
        Value originValue = condition.getValue();

        Value[] interval = (Value[]) originValue.getValue();
        Object base = this.metric.analyze();

        Number d1 = this.subtract(bound[1], interval[1].getValue()), d2 = this.subtract(interval[0].getValue(), bound[0]);

        Number[][] forIteration = {
                {((Comparable) d1).compareTo(d2) > 0? d1 : d2, 1},
                {this.divide(this.subtract(interval[1], interval[0]), 2), -1},
        };

        for(Number[] iteration : forIteration) {
            if(((Comparable) iteration[1]).compareTo(this.precisionTolerance) > 0) {
                Pair<String, Integer, Object[]> pair = new Pair<>();
                pair.setValue1("Condition: " + condition.getCondition());
                pair.setValue2(iteration[1].intValue());

                Object[] object = new Object[4];

                Number min = 0, max = iteration[0];
                double diff = 0;

                while(((Comparable) this.precisionTolerance).compareTo(this.subtract(max, min)) < 0) {
                    Number middle = this.divide(this.add(min, max), 2);
                    Value varied_value = iteration[1].intValue() > 0 ? originValue.increase(middle) : originValue.decrease(middle);
                    condition.setValue(varied_value);
                    Object _varied = this.metric.analyze();
                    diff = this.metric.calculateDiff(base, _varied);

                    if(diff >= this.differenceBound) {
                        max = middle;
                    }
                    else {
                        min = middle;
                    }
                }
                object[0] = diff;
                object[1] = min;

                min = 0;
                max = iteration[0];
                Value varied_value = originValue.increase(max);
                condition.setValue(varied_value);
                Object _varied = this.metric.analyze();
                diff = this.metric.calculateDiff(base, _varied);

                if(diff > this.differenceBound) {
                    while(((Comparable) this.precisionTolerance).compareTo(this.subtract(max, min)) < 0) {
                        Number middle = this.divide(this.add(min, max), 2);
                        varied_value = iteration[1].intValue() > 0 ? originValue.increase(middle) : originValue.decrease(middle);
                        condition.setValue(varied_value);
                        _varied = this.metric.analyze();
                        diff = this.metric.calculateDiff(base, _varied);

                        if(diff > this.differenceBound) {
                            max = middle;
                        }
                        else {
                            min = middle;
                        }
                    }
                }
                object[2] = diff;
                object[3] = max;

                condition.setValue(originValue);
                pair.setValue3(object);
                result.add(pair);
            }
        }
    }

    private Number subtract(Object a, Object b) {
        if(a instanceof Integer && b instanceof Integer) {
            return (Integer) a - (Integer) b;
        }
        else if(a instanceof Double && b instanceof Double) {
            return (Double) a - (Double) b;
        }
        else if(a instanceof Long && b instanceof Long) {
            return (Long) a - (Long) b;
        }
        else if(a instanceof Date && b instanceof Date) {
            return ((Date) a).getTime() - ((Date) b).getTime();
        }
        else return null;
    }


    private Number add(Object a, Object b) {
        if(a instanceof Integer && b instanceof Integer) {
            return (Integer) a + (Integer) b;
        }
        else if(a instanceof Double && b instanceof Double) {
            return (Double) a + (Double) b;
        }
        else if(a instanceof Long && b instanceof Long) {
            return (Long) a + (Long) b;
        }
        else if(a instanceof Date && b instanceof Date) {
            return ((Date) a).getTime() + ((Date) b).getTime();
        }
        else return null;
    }

    private Number divide(Number a, Number b) {
        if(a instanceof Integer && b instanceof Integer) {
            return a.intValue() / b.intValue();
        }
        else if(a instanceof Double && b instanceof Double) {
            return a.doubleValue() / b.doubleValue();
        }
        else if(a instanceof Long && b instanceof Long) {
            return a.longValue() / b.longValue();
        }
        else return null;
    }
}
