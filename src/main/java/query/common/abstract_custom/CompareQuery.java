package query.common.abstract_custom;

import component.attribute.Attribute;
import component.condition.CompareCondition;
import component.condition.ComparisionType;
import component.value.DateValue;
import component.value.NumericalValue;
import component.value.Value;

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
            CompareCondition compareCondition = new CompareCondition(new Attribute(this.getAttribute(), null), _value, comparisionType);
            this.setCondition(compareCondition);
        }
    }
}
