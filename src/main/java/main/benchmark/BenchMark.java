package main.benchmark;

/**
 * Simple implementation benchmark to count the runtime
 *
 * @author Tung Doan
 */
public abstract class BenchMark {

    /**
     * run the "to-count" function iteratively and calculate the average runtime
     * @param numberOfIterations
     * @return average runtime
     */
    public long runCounter(int numberOfIterations) {

        numberOfIterations = numberOfIterations < 1 ? 1 : numberOfIterations;

        long avgTime = 0L;

        for(int i=0; i<numberOfIterations; ++i) {
            long startTime = System.currentTimeMillis();
            this.run();
            long endTime = System.currentTimeMillis();

            avgTime += endTime - startTime;
        }

        return avgTime / numberOfIterations;
    }

    /**
     * run the function to count the runtime
     */
    public abstract void run();
}
