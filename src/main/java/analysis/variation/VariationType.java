package analysis.variation;

public enum VariationType {
    NAIVE, // varying fixed unit
    AVERAGE, // varying unit based on average distance in value set
    ADAPTIVE, // varying unit based on changing rate from previous variation
    SET, // varying elements in set
}
