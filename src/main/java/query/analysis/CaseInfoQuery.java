package query.analysis;

import query.common.CommonQuery;

public class CaseInfoQuery extends AnalysisQuery {
    public CaseInfoQuery(CommonQuery commonQuery) {
        super("Case overview information query", commonQuery);
    }

    @Override
    public String getQuery() {
        AvgTransitionTimeQuery avgTransitionTimeQuery = new AvgTransitionTimeQuery(this.getCommonQuery());
        return String.format(
                "select t1.caseId, number_of_activities, number_of_resources, duration, t2.average_transition_time from ( "
                + "select caseId, count(activity) as number_of_activities, count(resource) as number_of_resources, timestampdiff(minute, min(time_stamp), max(time_stamp)) as duration from %s as t group by caseId"
                + " ) as t1, %s as t2 where t1.caseId = t2.caseId", "( " + this.getCommonQuery().getQuery() + " )", "( " + avgTransitionTimeQuery.getQuery() + " )"
        );
    }
}
