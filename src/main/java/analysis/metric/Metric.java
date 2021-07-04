package analysis.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import db_connection.DbConnection;
import query.analysis.AnalysisQuery;
import query.common.CommonQuery;
import java.sql.Connection;

/**
 * Metric general form
 *
 * @author Tung Doan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = CasePerVariantMetric.class, name = "CPVM"),
        @JsonSubTypes.Type(value = CaseVarianceMetric.class, name = "CVM"),
        @JsonSubTypes.Type(value = SpecificActivityTransitionPerCaseMetric.class, name = "SATPCM")
})
public abstract class Metric {

    protected AnalysisQuery analysisQuery;

    @JsonProperty("type")
    private MetricType metricType;

    public Metric(MetricType metricType) {
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

    public MetricType getMetricType() {
        return metricType;
    }

    protected Connection getDatabaseConnection() {
        return DbConnection.getConnection();
    }

    public abstract Object analyze();

    public abstract double calculateDiff(Object obj1, Object obj2);
}
