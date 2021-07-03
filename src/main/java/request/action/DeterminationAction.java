package request.action;

import analysis.determinator.Determination;
import analysis.metric.Metric;
import com.fasterxml.jackson.annotation.JsonProperty;
import component.attribute.Attribute;
import query.common.CommonQuery;
import query.common.DeterminableCommonQuery;
import request.Query;

import java.util.HashMap;

public class DeterminationAction extends Action {

    private class DeterminationQuery extends DeterminableCommonQuery {

        @Override
        public HashMap<String, String> getBoundQueries() {
            return new HashMap<>(){{
                put(attribute.toString(), boundQuery.getQuery());
            }};
        }

        @Override
        public String getQuery() {
            return getCommonQuery().getQuery();
        }
    }

    @JsonProperty("attribute")
    private Attribute attribute;

    @JsonProperty("bound query")
    private Query boundQuery;

    @JsonProperty("difference bound")
    private double differenceBound;

    @JsonProperty("difference tolerance")
    private double differenceTolerance;

    @JsonProperty("precision tolerance")
    private Number precisionTolerance;

    @Override
    public Object getResult() {
        Determination determination = new Determination();
        DeterminationQuery determinationQuery = new DeterminationQuery();
        determination.setDeterminableCommonQuery(determinationQuery);
        determinationQuery.setCondition(this.getCommonQuery().getCondition());
        determination.setMetric(this.getMetric());
        this.getMetric().setCommonQuery(determinationQuery);
        determination.setDifferenceBound(differenceBound);
        determination.setTolerance(differenceTolerance, precisionTolerance);
        return determination.determine(this.attribute.toString());
    }
}
