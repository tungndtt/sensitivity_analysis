package analysis.determination;

import analysis.Pair;
import analysis.metric.Metric;
import component.condition.Condition;
import component.value.DateValue;
import component.value.IntervalValue;
import component.value.NumericalValue;
import component.value.Value;
import db_connection.DbConnection;
import query.common.DeterminableCommonQuery;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Determinator {

    private DeterminableCommonQuery determinableCommonQuery;

    private double differenceBound, differenceTolerance;

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

    public double getDifferenceTolerance() {
        return this.differenceTolerance;
    }

    public void setDifferenceBound(double differenceBound) {
        this.differenceBound = differenceBound;
    }

    public void setTolerance(double differenceTolerance, Number precisionTolerance) {
        this.differenceTolerance = differenceTolerance;
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

    public LinkedList<Pair<String, Object[]>> determine(String attribute) {
        if(this.determinableCommonQuery == null || this.metric == null) {
            System.out.println("Query or metric is not provided yet!");
            return null;
        }
        Object[] bound = this.getBound(attribute);

        LinkedList<Pair<String, Object[]>> result = new LinkedList<>();

        for(Condition condition : this.allConditions) {
            if(condition.getAttribute().toString().equals(attribute)) {
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

    private void handleValue(Condition condition, Object[] bound, LinkedList<Pair<String, Object[]>> result) {
        Value originValue = condition.getValue();

        Object base = this.metric.analyze();

        Object[][] forIteration = {
                {originValue.getValue(), bound[1], 1},
                {bound[0], originValue.getValue(), -1}
        };

        for(Object[] iteration : forIteration) {
            if(((Comparable) iteration[0]).compareTo(iteration[1]) < 0) {

                Pair<String, Object[]> pair = new Pair<>();
                pair.setValue1("Condition: " + condition.getCondition());

                Object[] object = new Object[4];

                Number max = Determinator.subtract(iteration[1], iteration[0]);
                Number min = Determinator.cast(0, max.getClass());
                double diff = 0;
                Number tolerance = Determinator.cast(this.precisionTolerance, max.getClass());

                Value varied_value;
                Object _varied;

                while(((Comparable) tolerance).compareTo(Determinator.subtract(max, min)) < 0) {
                    Number middle = Determinator.divide(Determinator.add(min, max), 2);
                    varied_value = (Integer) iteration[2] > 0 ? originValue.increase(middle) : originValue.decrease(middle);
                    condition.setValue(varied_value);
                    _varied = this.metric.analyze();
                    diff = this.metric.calculateDiff(base, _varied);

                    if(diff <= this.differenceBound - this.differenceTolerance) {
                        min = middle;
                    }
                    else {
                        max = middle;
                    }
                }

                object[1] = (Integer) iteration[2] > 0 ? max : Determinator.reverse(max);
                varied_value = (Integer) iteration[2] > 0 ? originValue.increase(max, max) : originValue.decrease(max, max);
                condition.setValue(varied_value);
                _varied = this.metric.analyze();
                object[0] = this.metric.calculateDiff(base, _varied);

                max = Determinator.subtract(iteration[1], iteration[0]);
                min = Determinator.cast(0, max.getClass());
                varied_value = (Integer) iteration[2] == 1 ? originValue.increase(max) : originValue.decrease(max);
                condition.setValue(varied_value);
                _varied = this.metric.analyze();
                diff = this.metric.calculateDiff(base, _varied);

                if(Math.abs(this.differenceBound - diff) > this.differenceTolerance) {
                    while(((Comparable) tolerance).compareTo(Determinator.subtract(max, min)) < 0) {
                        Number middle = Determinator.divide(Determinator.add(min, max), 2);
                        varied_value = (Integer) iteration[2] > 0 ? originValue.increase(middle) : originValue.decrease(middle);
                        condition.setValue(varied_value);
                        _varied = this.metric.analyze();
                        diff = this.metric.calculateDiff(base, _varied);

                        if(diff >= this.differenceBound + this.differenceTolerance) {
                            max = middle;
                        }
                        else {
                            min = middle;
                        }
                    }
                    object[3] = (Integer) iteration[2] > 0 ? min : Determinator.reverse(min);
                    varied_value = (Integer) iteration[2] > 0 ? originValue.increase(min, min) : originValue.decrease(min, min);
                    condition.setValue(varied_value);
                    _varied = this.metric.analyze();
                    object[2] = this.metric.calculateDiff(base, _varied);
                }
                else {
                    object[3] = (Integer) iteration[2] > 0 ? max : Determinator.reverse(max);
                    object[2] = diff;
                }

                condition.setValue(originValue);
                pair.setValue2(object);
                result.add(pair);
            }
        }
    }

    private void handleInterval(Condition condition, Object[] bound, LinkedList<Pair<String, Object[]>> result) {

        Value originValue = condition.getValue();
        Value[] interval = (Value[]) originValue.getValue();
        Object base = this.metric.analyze();

        Number d1 = Determinator.subtract(bound[1], interval[1].getValue()), d2 = Determinator.subtract(interval[0].getValue(), bound[0]);

        Number[][] forIteration = {
                {((Comparable) d1).compareTo(d2) > 0? d1 : d2, 1},
                {Determinator.divide(Determinator.subtract(interval[1].getValue(), interval[0].getValue()), 2), -1},
        };
        Number tolerance = Determinator.cast(this.precisionTolerance, d1.getClass());

        for(Number[] iteration : forIteration) {
            if(((Comparable) iteration[0]).compareTo(tolerance) > 0) {
                Pair<String, Object[]> pair = new Pair<>();
                pair.setValue1("Condition: " + condition.getCondition());

                Object[] object = new Object[4];

                Number min = Determinator.cast(0, iteration[0].getClass()), max = iteration[0];
                double diff;

                Value varied_value;
                Object _varied;

                while(((Comparable) tolerance).compareTo(Determinator.subtract(max, min)) < 0) {
                    Number middle = Determinator.divide(Determinator.add(min, max), 2);
                    varied_value = iteration[1].intValue() > 0 ? originValue.increase(middle, middle) : originValue.decrease(middle, middle);
                    condition.setValue(varied_value);
                    _varied = this.metric.analyze();
                    diff = this.metric.calculateDiff(base, _varied);

                    if(diff <= this.differenceBound - this.differenceTolerance) {
                        min = middle;
                    }
                    else {
                        max = middle;
                    }
                }

                object[1] = iteration[1].intValue() > 0 ? max : Determinator.reverse(max);
                varied_value = iteration[1].intValue() > 0 ? originValue.increase(max, max) : originValue.decrease(max, max);
                condition.setValue(varied_value);
                _varied = this.metric.analyze();
                object[0] = this.metric.calculateDiff(base, _varied);


                min = Determinator.cast(0, iteration[0].getClass());
                max = iteration[0];
                varied_value = iteration[1].intValue() > 0 ? originValue.increase(max, max) : originValue.decrease(max, max);
                condition.setValue(varied_value);
                _varied = this.metric.analyze();
                diff = this.metric.calculateDiff(base, _varied);

                if(Math.abs(this.differenceBound - diff) > this.differenceTolerance) {
                    while(((Comparable) tolerance).compareTo(Determinator.subtract(max, min)) < 0) {
                        Number middle = Determinator.divide(Determinator.add(min, max), 2);
                        varied_value = iteration[1].intValue() > 0 ? originValue.increase(middle, middle) : originValue.decrease(middle, middle);
                        condition.setValue(varied_value);
                        _varied = this.metric.analyze();
                        diff = this.metric.calculateDiff(base, _varied);

                        if(diff >= this.differenceBound + this.differenceTolerance) {
                            max = middle;
                        }
                        else {
                            min = middle;
                        }
                    }
                    object[3] = iteration[1].intValue() > 0 ? min : Determinator.reverse(min);
                    varied_value = iteration[1].intValue() > 0 ? originValue.increase(min, min) : originValue.decrease(min, min);
                    condition.setValue(varied_value);
                    _varied = this.metric.analyze();
                    object[2] = this.metric.calculateDiff(base, _varied);
                }
                else {
                    object[3] = iteration[1].intValue() > 0 ? max : Determinator.reverse(max);
                    object[2] = diff;
                }

                condition.setValue(originValue);
                pair.setValue2(object);

                result.add(pair);
            }
        }
    }

    private static Number subtract(Object a, Object b) {
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
            return (((Date) a).getTime() - ((Date) b).getTime()) / 60000;
        }
        else return null;
    }


    private static Number add(Object a, Object b) {
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

    private static Number divide(Number a, Number b) {
        if(a instanceof Integer) {
            return a.intValue() / b.intValue();
        }
        else if(a instanceof Double) {
            return a.doubleValue() / b.doubleValue();
        }
        else if(a instanceof Long) {
            return a.longValue() / b.longValue();
        }
        else return null;
    }

    private static Number cast(Number number, Class type) {
        if(type == Integer.class) {
            return number.intValue();
        }
        else if(type == Double.class) {
            return number.doubleValue();
        }
        else if(type == Long.class) {
            return number.longValue();
        }
        else {
            return null;
        }
    }

    private static Number reverse(Number number) {
        if(number instanceof Integer) {
            return -number.intValue();
        }
        else if(number instanceof Double) {
            return -number.doubleValue();
        }
        else if(number instanceof Long) {
            return -number.longValue();
        }
        else return null;
    }
}
