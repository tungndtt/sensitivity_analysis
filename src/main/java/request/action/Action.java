package request.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import query.common.CommonQuery;
import analysis.metric.Metric;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = DeterminationAction.class, name = "determination"),
        @JsonSubTypes.Type(value = VariationAction.class, name = "variation"),
})
public abstract class Action {

    @JsonProperty("type")
    private ActionType type;

    private CommonQuery commonQuery;

    private Metric metric;

    public void setCommonQuery(CommonQuery commonQuery) {
        this.commonQuery = commonQuery;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public CommonQuery getCommonQuery() {
        return this.commonQuery;
    }

    public Metric getMetric() {
        return this.metric;
    }

    public abstract Object getResult();
}
