package main.benchmark;

/**
 * Simple implementation benchmark to count the runtime
 *
 * @author Tung Doan
 */
public class BenchMark {

    private Function function;

    public void setFunction(Function function) {
        this.function = function;
    }

    /**
     * run the "to-count" function iteratively and calculate the average runtime
     * @param numberOfIterations
     * @return average runtime
     */
    public long runCounter(int numberOfIterations) {
        if(this.function == null) {
            System.out.println("Provide the function!");
            return 0;
        }
        numberOfIterations = numberOfIterations < 1 ? 1 : numberOfIterations;

        long avgTime = 0L;

        for(int i=0; i<numberOfIterations; ++i) {
            long startTime = System.currentTimeMillis();
            this.function.run();
            long endTime = System.currentTimeMillis();

            avgTime += endTime - startTime;
        }

        return avgTime / numberOfIterations;
    }

    public interface Function {
        void run();
    }
}
