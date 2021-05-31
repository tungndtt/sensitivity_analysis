package query.analysis;

import query.common.CommonQuery;

public class CaseInfoQuery extends AnalysisQuery {
    public CaseInfoQuery(CommonQuery commonQuery) {
        super("Case overview information query", commonQuery);
    }

    public CaseInfoQuery() {
        super("Case overview information query");
    }

    @Override
    public String getQuery() {
        AvgTransitionTimeQuery avgTransitionTimeQuery = new AvgTransitionTimeQuery(this.getCommonQuery());
        return String.format(
                "select t1.caseid, number_of_activities, number_of_resources, duration, t2.average_transition_time from ( "
                + "select caseid, count(activity) as number_of_activities, count(resource) as number_of_resources, (extract(epoch from max(time_stamp)) -  extract(epoch from min(time_stamp)))/60 as duration from %s as t group by caseid"
                + " ) as t1, %s as t2 where t1.caseid = t2.caseid", "( " + this.getCommonQuery().getQuery() + " )", "( " + avgTransitionTimeQuery.getQuery() + " )"
        );
    }
}
