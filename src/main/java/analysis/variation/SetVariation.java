package analysis.variation;

import condition.Condition;
import condition.value.SetValue;
import query.common.CommonQuery;
import java.sql.ResultSet;
import java.util.*;

public class SetVariation extends Variation {

    private int numberOfIterations;

    private int unit;

    public SetVariation() {
        super(VariationType.SET);
    }

    public SetVariation(CommonQuery commonQuery) {
        super(VariationType.SET);
        this.setCommonQuery(commonQuery);
    }

    private LinkedList<Object> getAllElements(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());

        if(query == null) {
            return null;
        }

        try {
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            LinkedList<Object> elements = new LinkedList<>();

            while (resultSet.next()) {
                elements.add(resultSet.getObject("element"));
            }

            return elements;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void setNumberOfIterationsAndUnit(int numberOfIterations, int unit) {
        this.numberOfIterations = numberOfIterations;
        this.unit = unit;
    }

    @Override
    public LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> vary(String attribute) {

        LinkedList<Condition> conditions = this.getVaryingConditions();

        if(conditions == null || this.getMetric() == null) {
            return null;
        }

        LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> result = new LinkedList<>();
        int condIdx = 1;
        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
                positive.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));
                Pair<Number, LinkedList<Number>, LinkedList<Double>> part = new Pair<>();
                part.setValue1(this.unit);
                LinkedList<Number> changingSizes = new LinkedList<>();
                LinkedList<Double> changingRates = new LinkedList<>();

                SetValue setValue = (SetValue) condition.getValue();
                List<Object> originalElements = (List<Object>) setValue.getValue();
                List<Object> copied = new LinkedList<>();
                HashSet<Object> existed = new HashSet<>();

                copied.addAll(originalElements);
                existed.addAll(originalElements);
                setValue.setValue(copied);

                LinkedList<Object> elements = this.getAllElements(attribute);
                Collections.shuffle(elements);

                Object base = this.getMetric().analyze();

                int iterations = 1;
                positive.setValue2(1);
                while(elements.size() > 0 && this.numberOfIterations >= iterations) {
                    setValue.increase(this.unit, elements, existed);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.add(diffValue);

                    changingSizes.add(++iterations);
                }

                part.setValue2(changingSizes);
                part.setValue3(changingRates);
                positive.setValue3(part);

                copied.clear();
                copied.addAll(originalElements);
                Collections.shuffle(copied);
                setValue.setValue(copied);

                Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
                negative.setValue1(String.format("condition %d : %s", condIdx, condition.getCondition()));
                part = new Pair<>();
                part.setValue1(this.unit);
                changingSizes = new LinkedList<>();
                changingRates = new LinkedList<>();

                iterations = 1;
                negative.setValue2(-1);
                while (copied.size() > 0 && iterations <= this.numberOfIterations && copied.size() > this.unit) {
                    setValue.decrease(this.unit);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.add(diffValue);

                    changingSizes.add(++iterations);
                }
                part.setValue2(changingSizes);
                part.setValue3(changingRates);
                negative.setValue3(part);

                setValue.setValue(originalElements);

                result.add(positive);
                result.add(negative);

                condIdx++;
            }
        }
        return result;
    }
}
