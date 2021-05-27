package analysis.variation;

import query.common.CommonQuery;
import java.sql.ResultSet;
import java.util.LinkedList;

public class AverageVariation extends Variation{

    public AverageVariation() {
        super(VariationType.AVERAGE);
    }

    public AverageVariation(CommonQuery commonQuery) {
        super(VariationType.AVERAGE);
        this.setCommonQuery(commonQuery);
    }

    public LinkedList<Object> getAllElements(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());

        if(query == null) {
            return null;
        }

        try {
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            LinkedList<Object> elements = new LinkedList<>();

            while (resultSet.next()) {
                elements.add(resultSet.getObject("element"));
            }

            return elements;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
