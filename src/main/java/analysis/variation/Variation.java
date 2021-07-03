package analysis.variation;

import analysis.Pair;
import analysis.metric.Metric;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import component.condition.*;
import db_connection.DbConnection;
import query.common.CommonQuery;
import java.sql.Connection;
import java.util.LinkedList;

/**
 * Variation general form
 *
 * @author Tung Doan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = NaiveVariation.class, name = "naive"),
        @JsonSubTypes.Type(value = AverageVariation.class, name = "average"),
        @JsonSubTypes.Type(value = AdaptiveVariation.class, name = "adaptive"),
        @JsonSubTypes.Type(value = SetVariation.class, name = "set")
})
public abstract class Variation {

    @JsonProperty("type")
    private VariationType type;

    private CommonQuery commonQuery;

    private Metric metric;

    private LinkedList<Condition> varyingConditions;

    public Variation(VariationType type) {
        this.type = type;
    }

    public VariationType getType() {
        return type;
    }

    public CommonQuery getCommonQuery() {
        return commonQuery;
    }

    public void setCommonQuery(CommonQuery commonQuery) {
        this.commonQuery = commonQuery;
        this.retrieveAllVaryingConditions();
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
        this.metric.setCommonQuery(this.commonQuery);
    }

    public Metric getMetric() {
        return this.metric;
    }

    private void retrieveAllVaryingConditions() {
        if(this.commonQuery != null) {
            this.varyingConditions = this.commonQuery.retrieveAllConditionsWithValue();
        }
    }

    public LinkedList<Condition> getVaryingConditions() {
        return this.varyingConditions;
    }

    protected Connection getDatabaseConnection() {
        return DbConnection.getConnection();
    }

    public abstract LinkedList<Pair<String, Integer, Pair<Number, LinkedList<Number>, LinkedList<Double>>>> vary(String condition);
}
