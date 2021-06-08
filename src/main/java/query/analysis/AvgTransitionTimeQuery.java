package query.analysis;

import query.common.CommonQuery;

/**
 *
 */
public class AvgTransitionTimeQuery extends AnalysisQuery {

    public AvgTransitionTimeQuery(CommonQuery commonQuery) {
        super("Average transition time between activities within case query", commonQuery);
    }

    public AvgTransitionTimeQuery() {
        super("Average transition time between activities within case query");
    }

    @Override
    public String getQuery() {
        return String.format(
                "select caseid, avg(transition_time) as average_transition_time from ("
                + "select caseid, (extract(epoch from lead(time_stamp, 1) over(partition by caseid order by time_stamp)) - extract(epoch from time_stamp))/60 as transition_time from %s as t"
                + ") as t group by caseid", "(" + this.getCommonQuery().getQuery() + ")"
        );
    }
}
