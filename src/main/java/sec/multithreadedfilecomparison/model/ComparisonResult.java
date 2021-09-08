package sec.multithreadedfilecomparison.model;

public class ComparisonResult {
    private final ComparisonPair comparisonPair;
    private final double similarity;

    public ComparisonResult(ComparisonPair comparisonPair, double similarity) {
        this.comparisonPair = comparisonPair;
        this.similarity = similarity;
    }

    public ComparisonPair getComparisonPair() {
        return comparisonPair;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public String toString() {
        return comparisonPair.toString() + ": " + similarity;
    }
}
