package analysis.variation;

import analysis.Pair;
import component.condition.Condition;
import component.value.SetValue;
import query.common.CommonQuery;
import java.sql.ResultSet;
import java.util.*;

/**
 * Implementation of the set variation, which varies the set-value
 * Progress:
 * repeat(numberOfIterations):
 *
 *
 * @author Tung Doan
 */
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
    public LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> vary(String attribute) {

        LinkedList<Condition> conditions = this.getVaryingConditions();

        if(conditions == null || this.getMetric() == null) {
            return null;
        }

        LinkedList<Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>>> result = new LinkedList<>();

        for(Condition condition : conditions) {
            if(condition.getAttribute().toString().equals(attribute)) {

                SetValue setValue = (SetValue) condition.getValue();
                List<Object> originalElements = (List<Object>) setValue.getValue();
                Object base = this.getMetric().analyze();

                Util.Functional[] functions = new Util.Functional[2];

                functions[0] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                    List<Object> copied = new LinkedList<>();
                    HashSet<Object> existed = new HashSet<>();

                    copied.addAll(originalElements);
                    existed.addAll(originalElements);
                    setValue.setValue(copied);

                    LinkedList<Object> elements = this.getAllElements(attribute);
                    Collections.shuffle(elements);

                    int iterations = 1;
                    while(elements.size() > 0 && this.numberOfIterations >= iterations) {
                        setValue.increase(this.unit, elements, existed);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.add(diffValue);

                        changingSizes.add((iterations++) * this.unit);
                    }
                    setValue.setValue(originalElements);
                };

                functions[1] = (LinkedList<Number> changingSizes, LinkedList<Double> changingRates) -> {
                    int iterations = 1;
                    List<Object> copied = new LinkedList<>();
                    copied.addAll(originalElements);
                    Collections.shuffle(copied);
                    setValue.setValue(copied);
                    while (copied.size() > 0 && iterations <= this.numberOfIterations && copied.size() > this.unit) {
                        setValue.decrease(this.unit);
                        Object variedBase = this.getMetric().analyze();

                        double diffValue = this.getMetric().calculateDiff(base, variedBase);
                        changingRates.add(diffValue);

                        changingSizes.add(-(iterations++) * this.unit);
                    }
                    setValue.setValue(originalElements);
                };

                Util.handleFunctions(setValue, condition, result, functions);

                /*
                Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> positive = new Pair<>();
                positive.setValue1(String.format("condition : %s", condition.getCondition()));

                Pair<LinkedList<Number>, LinkedList<Double>> part = new Pair<>();

                LinkedList<Number> changingSizes = new LinkedList<>();
                LinkedList<Double> changingRates = new LinkedList<>();

                changingRates.add(0.0);
                changingSizes.add(0);

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
                while(elements.size() > 0 && this.numberOfIterations >= iterations) {
                    setValue.increase(this.unit, elements, existed);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.add(diffValue);

                    changingSizes.add((++iterations) * this.unit);
                }

                part.setValue1(changingSizes);
                part.setValue2(changingRates);
                positive.setValue2(part);

                copied.clear();
                copied.addAll(originalElements);
                Collections.shuffle(copied);
                setValue.setValue(copied);

                Pair<String, Pair<LinkedList<Number>, LinkedList<Double>>> negative = new Pair<>();
                negative.setValue1(String.format("condition : %s", condition.getCondition()));

                part = new Pair<>();
                changingSizes = new LinkedList<>();
                changingRates = new LinkedList<>();

                iterations = 1;
                while (copied.size() > 0 && iterations <= this.numberOfIterations && copied.size() > this.unit) {
                    setValue.decrease(this.unit);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.add(diffValue);

                    changingSizes.add(-(++iterations) * this.unit);
                }
                part.setValue1(changingSizes);
                part.setValue2(changingRates);
                negative.setValue2(part);

                setValue.setValue(originalElements);

                result.add(positive);
                result.add(negative);

                 */
            }
        }
        return result;
    }
}
