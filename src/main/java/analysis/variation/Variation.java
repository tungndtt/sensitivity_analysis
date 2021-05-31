package analysis.variation;

import analysis.metric.Metric;
import condition.Condition;
import main.Application;
import query.common.CommonQuery;

import java.sql.Connection;
import java.util.LinkedList;

public abstract class Variation {

    private VariationType type;

    private CommonQuery commonQuery;

    private Metric metric;

    private LinkedList<Condition> varyingConditions;

    public Variation(VariationType type) {
        this.type = type;
    }

    public VariationType getType() {
        return type;
    }

    public CommonQuery getCommonQuery() {
        return commonQuery;
    }

    public void setCommonQuery(CommonQuery commonQuery) {
        this.commonQuery = commonQuery;
        this.retrieveAllVaryingConditions();
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
        this.metric.setCommonQuery(this.commonQuery);
    }

    public Metric getMetric() {
        return this.metric;
    }

    private void retrieveAllVaryingConditions() {
        if(this.commonQuery != null) {
            LinkedList<Condition> conditions = new LinkedList<>();

            LinkedList<Condition> queue = new LinkedList<>();
            queue.add(this.commonQuery.getCondition());

            while (queue.size() > 0) {
                Condition condition = queue.removeFirst();
                if(condition.getAttribute() != null) {
                    conditions.add(condition);
                }
                if(condition.getSubConditions() != null) {
                    for(Condition subCondition : condition.getSubConditions()) {
                        queue.add(subCondition);
                    }
                }
            }

            this.varyingConditions = conditions;
        }
    }

    public LinkedList<Condition> getVaryingConditions() {
        return this.varyingConditions;
    }

    protected Connection getDatabaseConnection() {
        return Application.getConnection();
    }

    public abstract LinkedList<Pair> vary(String condition);
}
