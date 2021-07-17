package request.action;

import analysis.determination.Determinator;
import com.fasterxml.jackson.annotation.JsonProperty;
import query.common.DeterminableCommonQuery;
import request.query.Selection;

import java.util.HashMap;

public class DeterminationAction extends Action {

    private class DeterminationQuery extends DeterminableCommonQuery {

        @Override
        public HashMap<String, String> getBoundQueries() {
            return new HashMap<>(){{
                put(getAttribute().toString(), boundQuery.getQuery());
            }};
        }

        @Override
        public String getQuery() {
            return getCommonQuery().getQuery();
        }
    }

    @JsonProperty("bound query")
    private Selection boundQuery;

    @JsonProperty("difference bound")
    private double differenceBound;

    @JsonProperty("difference tolerance")
    private double differenceTolerance;

    @JsonProperty("precision tolerance")
    private Number precisionTolerance;

    @Override
    public Object getResult() {
        Determinator determinator = new Determinator();
        DeterminationQuery determinationQuery = new DeterminationQuery();
        determinator.setDeterminableCommonQuery(determinationQuery);
        determinationQuery.setCondition(this.getCommonQuery().getCondition());
        determinator.setMetric(this.getMetric());
        this.getMetric().setCommonQuery(determinationQuery);
        determinator.setDifferenceBound(differenceBound);
        determinator.setTolerance(differenceTolerance, precisionTolerance);
        return determinator.determine(this.getAttribute().toString());
    }
}
