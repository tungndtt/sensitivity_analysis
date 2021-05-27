package analysis.metric;

import main.Application;
import query.common.CommonQuery;

import java.sql.Connection;

public abstract class Metric {

    private CommonQuery commonQuery;

    private String metricType;

    public Metric(String metricType) {
        this.metricType = metricType;
    }

    public CommonQuery getCommonQuery() {
        return commonQuery;
    }

    public void setCommonQuery(CommonQuery commonQuery) {
        this.commonQuery = commonQuery;
    }

    public String getMetricType() {
        return metricType;
    }

    protected Connection getDatabaseConnection() {
        return Application.getConnection();
    }

    public abstract Object analyze();

    public abstract double calculateDiff(Object obj1, Object obj2);
}
