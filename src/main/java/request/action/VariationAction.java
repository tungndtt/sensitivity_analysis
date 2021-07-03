package request.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import component.attribute.Attribute;
import analysis.variation.Variation;

public class VariationAction extends Action {

    @JsonProperty("variation")
    private Variation variation;

    @JsonProperty("attribute")
    private Attribute attribute;

    @Override
    public Object getResult() {
        this.variation.setCommonQuery(this.getCommonQuery());
        this.variation.setMetric(this.getMetric());
        return this.variation.vary(this.attribute.toString());
    }
}
