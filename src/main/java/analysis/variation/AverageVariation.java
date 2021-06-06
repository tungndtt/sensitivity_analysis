package analysis.variation;

import query.common.CommonQuery;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class AverageVariation extends Variation{

    private double gamma;

    private int numberOfIterations;

    private Number differenceBound;

    public AverageVariation() {
        super(VariationType.AVERAGE);
    }

    public AverageVariation(CommonQuery commonQuery) {
        super(VariationType.AVERAGE);
        this.setCommonQuery(commonQuery);
    }

    private LinkedList<Comparable> getAllElements(String attribute) {
        String query = this.getCommonQuery().getQueryForVariation(attribute, this.getType());
        System.out.println(query);
        if(query == null) {
            return null;
        }

        try {
            ResultSet resultSet = this.getDatabaseConnection().prepareStatement(query).executeQuery();
            LinkedList<Comparable> elements = new LinkedList<>();

            while (resultSet.next()) {
                elements.add((Comparable) resultSet.getObject("element"));
            }

            return elements;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void setGammaAndIterations(double gamma, int numberOfIterations) {
        this.gamma = gamma;
        this.numberOfIterations = numberOfIterations;
    }

    public void setDifferenceBound(Number differenceBound) {
        this.differenceBound = differenceBound;
    }

    @Override
    public LinkedList<Pair> vary(String attribute) {
        LinkedList<Comparable> elements = this.getAllElements(attribute);
        if(elements.size() < 2) {
            return null;
        }
        Collections.sort(elements);
        Iterator<Comparable> iterator = elements.iterator();
        Comparable current = iterator.next();

        Number n = this.differenceBound;
        if(current instanceof Integer) {
            n = n.intValue();
        }
        else if(current instanceof Double) {
            n = n.doubleValue();
        }
        else if(current instanceof Long || current instanceof Date) {
            n = n.longValue();
        }
        else {
            System.out.println("Unsupported type for average variation!");
            return null;
        }

        Number sum = 0;
        int number = 0;
        while(iterator.hasNext()) {
            Comparable next = iterator.next();
            Number distance = this.distance(current, next);

            if(distance == null) {
                return null;
            }
            if(((Comparable)distance).compareTo(n) <= 0) {
                if(distance instanceof Integer) {
                    sum = distance.intValue() + sum.intValue();
                }
                else if(distance instanceof Double) {
                    sum = distance.doubleValue() + sum.doubleValue();
                }
                else if(distance instanceof Long) {
                    sum = distance.longValue() + sum.longValue();
                }
                ++number;
            }

            current = next;
        }

        Number unit = null;

        if(sum instanceof Integer) {
            unit = sum.intValue()*this.gamma / number;
        }
        else if(sum instanceof Double) {
            unit = sum.doubleValue()*this.gamma / number;
        }
        else if(sum instanceof Long) {
            unit = sum.longValue()*this.gamma / number;
        }

        NaiveVariation naiveVariation = new NaiveVariation();
        naiveVariation.setCommonQuery(this.getCommonQuery());
        naiveVariation.setMetric(this.getMetric());
        naiveVariation.setUnitAndNumberOfIterations(unit, this.numberOfIterations);

        return naiveVariation.vary(attribute);
    }

    private Number distance(Object o1, Object o2) {
        if(o1 instanceof Integer) {
            return (Integer) o2 - (Integer) o1;
        }
        else if(o1 instanceof Double) {
            return (Double) o2 - (Double) o1;
        }
        else if(o1 instanceof Long) {
            return (Long) o2 - (Long) o1;
        }
        else if(o1 instanceof Date) {
            return (((Date) o2).getTime() - ((Date) o1).getTime())/60000;
        }
        else {
            System.out.println("Unsupported type for average variation!");
            return null;
        }
    }
}
