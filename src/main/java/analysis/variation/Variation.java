package analysis.variation;

import analysis.Pair;
import analysis.metric.Metric;
import condition.Condition;
import db_connection.DbConnection;
import query.common.CommonQuery;
import java.sql.Connection;
import java.util.LinkedList;

/**
 * Variation general form
 *
 * @author Tung Doan
 */
public abstract class Variation {

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
