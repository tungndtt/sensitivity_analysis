package query.analysis;

import query.common.CommonQuery;

/**
 *
 */
public class SpecificActivityTransitionQuery extends AnalysisQuery{

    public enum Mode {
        ORDER,
        NEIGHBOR
    }

    private String startActivity, endActivity;

    private Mode mode = Mode.NEIGHBOR;

    public SpecificActivityTransitionQuery() {
        super("Transition time between 2 specific activities in case query");
    }

    public SpecificActivityTransitionQuery(CommonQuery commonQuery) {
        super("Transition time between 2 specific activities in case query");
        this.setCommonQuery(commonQuery);
    }

    public void setActivities(String startActivity, String endActivity) {
        this.startActivity = startActivity;
        this.endActivity = endActivity;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String getQuery() {
        if(this.mode == Mode.NEIGHBOR) {
            String order = null;
            if(this.startActivity.compareTo(this.endActivity) > 0 ) {
                order = "asc";
            }
            else {
                order = "desc";
            }

            return String.format("select caseid, avg((extract(epoch from next_time_stamp) - extract(epoch from time_stamp))/60) as average_transition_time from ( "
                            + "select caseid, activity, time_stamp, (lead(activity, 1) over (partition by caseid order by time_stamp asc, activity %s)) as next_activity, (lead(time_stamp, 1) over (partition by caseid order by time_stamp asc, activity %s)) as next_time_stamp "
                            + "from %s as t where activity = '%s' or activity = '%s'"
                            + " ) as t group by caseid, activity, next_activity having activity = '%s' and next_activity = '%s'",
                    order, order, "( " + this.getCommonQuery().getQuery() + " )", this.startActivity, this.endActivity, this.startActivity, this.endActivity);
        }
        else {
            return String.format(
                    "with commonQuery as ( %s )"
                    + "select caseid, avg(transition_time) as average_transition_time from ( "
                    + "select t1.caseid, (extract(epoch from t2.time_stamp) - extract(epoch from t2.time_stamp))/60 as transition_time from ( "
                    + "select caseid, rank() over (partition by caseid order by time_stamp) as ranking, time_stamp from commonQuery as t where activity = '%s' ) as t1 "
                    + "inner join ( "
                    + "select caseid, rank() over (partition by caseid order by time_stamp) as ranking, time_stamp from commonQuery as t where activity = '%s' ) as t2 "
                    + "on t1.caseid = t2.caseid and t1.ranking=t2.ranking and t1.time_stamp <= t2.time_stamp ) as t group by caseid", this.getCommonQuery().getQuery() , this.startActivity, this.endActivity);
        }
    }
}
