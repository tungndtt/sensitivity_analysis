package analysis.variation;

import condition.Condition;
import condition.value.SetValue;
import condition.value.Value;
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
    public LinkedList<Pair> vary(String attribute) {

        LinkedList<Condition> conditions = this.getVaryingConditions();

        if(conditions == null || this.getMetric() == null) {
            return null;
        }

        LinkedList<Pair> result = new LinkedList<>();
        int condIdx = 1;
        for(Condition condition : conditions) {
            if(condition.getAttribute().equals(attribute)) {
                Pair pair = new Pair(String.format("Condition %d: %s", condIdx++, condition.getCondition()));
                HashMap<String, Double> changingRates = new HashMap<>();
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
                String prefix = "Add to set by ";
                while(elements.size() > 0 && this.numberOfIterations >= iterations++) {
                    Value variedValue = setValue.increase(this.unit, elements, existed);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.put(prefix + iterations*unit, diffValue);
                }

                copied.clear();
                copied.addAll(originalElements);
                setValue.setValue(copied);

                iterations = 1;
                prefix = "Remove from set by ";
                while (copied.size() > 0 && iterations++ <= this.numberOfIterations) {
                    Value variedValue = setValue.decrease(this.unit);
                    Object variedBase = this.getMetric().analyze();

                    double diffValue = this.getMetric().calculateDiff(base, variedBase);
                    changingRates.put(prefix + iterations*unit, diffValue);
                }

                pair.setChangingRate(changingRates);
                result.add(pair);
            }
        }
        return result;
    }
}
