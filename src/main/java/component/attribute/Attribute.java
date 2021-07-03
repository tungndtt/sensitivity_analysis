package component.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attribute {

    private String attribute;

    private String alias;

    public Attribute(@JsonProperty("attribute") String attribute, @JsonProperty("alias") String alias) {
        this.attribute = attribute;
        this.alias = alias;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String toString() {
        String result = this.attribute;
        if(this.alias != null && this.alias.length() > 0) {
            result += " as " + this.alias;
        }
        return result;
    }
}

