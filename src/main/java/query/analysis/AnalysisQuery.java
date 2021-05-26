package query.analysis;

import query.common.CommonQuery;

public abstract class AnalysisQuery {

    private String queryName;

    private CommonQuery commonQuery;

    public AnalysisQuery(String queryName, CommonQuery commonQuery) {
        this.queryName = queryName;
        this.commonQuery = commonQuery;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public CommonQuery getCommonQuery() {
        return commonQuery;
    }

    public void setCommonQuery(CommonQuery commonQuery) {
        this.commonQuery = commonQuery;
    }

    public abstract String getQuery();
}
