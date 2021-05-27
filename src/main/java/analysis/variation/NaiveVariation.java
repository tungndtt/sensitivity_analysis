package analysis.variation;

import query.common.CommonQuery;
import java.sql.ResultSet;

public class NaiveVariation extends Variation{

    public NaiveVariation() {
        super(VariationType.NAIVE);
    }

    public NaiveVariation(CommonQuery commonQuery) {
        super(VariationType.NAIVE);
        this.setCommonQuery(commonQuery);
    }

    public Object[] getMinMaxRange(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());
        if(query == null) {
            return null;
        }
        try {
            System.out.println(query);
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            Object[] range = new Object[2];

            while (resultSet.next()) {
                range[0] = resultSet.getObject("minimum");
                range[1] = resultSet.getObject("maximum");
            }

            return range;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
