package request;

import com.fasterxml.jackson.annotation.JsonProperty;
import request.action.Action;
import analysis.metric.Metric;

public class Request {

    @JsonProperty("query")
    private Query query;

    @JsonProperty("action")
    private Action action;

    @JsonProperty("metric")
    private Metric metric;

    public Request(@JsonProperty("query") Query query, @JsonProperty("action") Action action, @JsonProperty("metric") Metric metric) {
        this.query = query;
        this.action = action;
        this.metric = metric;
        this.action.setCommonQuery(query);
        this.action.setMetric(metric);
    }
}
