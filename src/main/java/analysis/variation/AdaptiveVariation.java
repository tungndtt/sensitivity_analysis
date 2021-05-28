package analysis.variation;

import condition.Condition;
import query.common.CommonQuery;

import java.sql.ResultSet;
import java.util.LinkedList;

public class AdaptiveVariation extends Variation {

    public AdaptiveVariation() {
        super(VariationType.ADAPTIVE);
    }

    public AdaptiveVariation(CommonQuery commonQuery) {
        super(VariationType.ADAPTIVE);
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

    @Override
    public LinkedList<Pair> vary(String condition) {
        return null;
    }
}
