package request;

import com.fasterxml.jackson.annotation.JsonProperty;
import request.action.Action;
import analysis.metric.Metric;
import request.query.Selection;

public class Request {

    public Selection query;

    public Action action;

    public Metric metric;

    public Request(@JsonProperty("query") Selection query, @JsonProperty("action") Action action,
                   @JsonProperty("metric") Metric metric) {
        this.query = query;
        this.action = action;
        this.metric = metric;
        this.action.setCommonQuery(query);
        this.action.setMetric(metric);
    }
}
