package analysis.metric;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MetricType {
    CPVM("CPVM"),
    CVM("CVM"),
    SATPCM("SATPCM");

    private String type;

    MetricType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static MetricType forValue(String type) {
        if(type.equals(CPVM)) {
            return CPVM;
        }
        else if(type.equals(CVM)) {
            return CVM;
        }
        else if(type.equals(SATPCM)) {
            return SATPCM;
        }
        else {
            return null;
        }
    }
}
