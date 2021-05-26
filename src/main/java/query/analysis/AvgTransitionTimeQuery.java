package query.analysis;

import query.common.CommonQuery;

public class AvgTransitionTimeQuery extends AnalysisQuery {

    public AvgTransitionTimeQuery(CommonQuery commonQuery) {
        super("Average transition time between activities within case query", commonQuery);
    }

    @Override
    public String getQuery() {
        return String.format(
                "select caseId, avg(transition_time) as average_transition from ("
                + "select caseId, timestampdiff(minute, time_stamp, lead(time_stamp, 1) over(partition by caseId order by time_stamp)) as transition_time) from %s as t"
                + ") as t group by caseId", "(" + this.getCommonQuery().getQuery() + ")"
        );
    }
}
