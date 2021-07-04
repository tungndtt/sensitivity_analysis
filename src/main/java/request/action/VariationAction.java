package request.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import analysis.variation.Variation;
import request.query.Selection;

import java.util.HashMap;

public class VariationAction extends Action {

    @JsonProperty("variation")
    private Variation variation;

    @JsonProperty("get-range query")
    private Selection query;

    @Override
    public Object getResult() {
        this.variation.setCommonQuery(this.getCommonQuery());
        this.variation.setMetric(this.getMetric());

        this.getCommonQuery().setAttributeValueSetQueries(new HashMap<>(){{
            put(getAttribute().toString(), new HashMap<>(){{
                put(variation.getType(), query.getQuery());
            }});
        }});

        return this.variation.vary(this.getAttribute().toString());
    }
}
