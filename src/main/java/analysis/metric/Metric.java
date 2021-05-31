package analysis.metric;

import main.Application;
import query.analysis.AnalysisQuery;
import query.common.CommonQuery;

import java.sql.Connection;

public abstract class Metric {

    protected AnalysisQuery analysisQuery;

    private String metricType;

    public Metric(String metricType) {
        this.metricType = metricType;
    }

    public void setCommonQuery(CommonQuery commonQuery) {
        if(this.analysisQuery != null) {
            this.analysisQuery.setCommonQuery(commonQuery);
        }
    }

    public AnalysisQuery getAnalysisQuery() {
        return this.analysisQuery;
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
