package query.common.abstract_custom;

import condition.CompareCondition;
import condition.ComparisionType;
import condition.value.DateValue;
import condition.value.NumericalValue;
import condition.value.Value;

import java.util.Date;

/**
 *
 */
public class CompareQuery extends CustomQuery {

    public CompareQuery(String queryName, String attribute, String selectFrom, Comparable value, ComparisionType comparisionType) {
        super(queryName, attribute, selectFrom);
        Value _value = null;
        if(value instanceof Number) {
            _value = new NumericalValue(value);
        }
        else if(value instanceof Date) {
            _value = new DateValue(value);
        }
        if(_value != null) {
            CompareCondition compareCondition = new CompareCondition(this.getAttribute(), _value, comparisionType);
            this.setCondition(compareCondition);
        }
    }
}
