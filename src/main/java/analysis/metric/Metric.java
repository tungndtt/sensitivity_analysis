package analysis.metric;

import db_connection.DbConnection;
import query.analysis.AnalysisQuery;
import query.common.CommonQuery;
import java.sql.Connection;

/**
 * Metric general form
 *
 * @author Tung Doan
 */
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
        return DbConnection.getConnection();
    }

    public abstract Object analyze();

    public abstract double calculateDiff(Object obj1, Object obj2);
}
