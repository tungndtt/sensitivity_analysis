package query.analysis;

import query.common.CommonQuery;

/**
 *
 */
public class CasesPerVariantQuery extends AnalysisQuery {
    public CasesPerVariantQuery(CommonQuery commonQuery) {
        super("Number of cases per variant of activities query", commonQuery);
    }

    public CasesPerVariantQuery() {
        super("Number of cases per variant of activities query");
    }

    @Override
    public String getQuery() {
        return String.format(
                "select variant, count(*) as number_of_variant from ( "
                + "select caseId, string_agg(activity, ',') as variant from ( select * from %s as t order by time_stamp ) as t group by caseId"
                + " ) as t group by variant", "( " + this.getCommonQuery().getQuery() + " )"
        );
    }
}
