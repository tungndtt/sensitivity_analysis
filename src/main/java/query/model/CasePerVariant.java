package query.model;

import java.sql.ResultSet;

public class CasePerVariant {

    private String variant;

    private int numberOfCases;

    private CasePerVariant(String variant, int numberOfCases) {
        this.variant = variant;
        this.numberOfCases = numberOfCases;
    }


    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public int getNumberOfCases() {
        return numberOfCases;
    }

    public void setNumberOfCases(int numberOfCases) {
        this.numberOfCases = numberOfCases;
    }

    public static CasePerVariant CasePerVariant(ResultSet resultSet) {
        try {
            String variant = resultSet.getString("variant");
            int numberOfCases = resultSet.getInt("number_of_variant");

            return new CasePerVariant(variant, numberOfCases);
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
