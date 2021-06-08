package main.benchmark;

/**
 *
 */
public abstract class BenchMark {

    private Object[] args;

    public BenchMark(Object[] args) {
        this.args = args;
    }

    /**
     *
     * @param numberOfIterations
     * @return
     */
    public long count(int numberOfIterations) {

        numberOfIterations = numberOfIterations < 1 ? 1 : numberOfIterations;

        long avgTime = 0L;

        for(int i=0; i<numberOfIterations; ++i) {
            long startTime = System.currentTimeMillis();
            this.run(this.args);
            long endTime = System.currentTimeMillis();

            avgTime += endTime - startTime;
        }

        return avgTime / numberOfIterations;
    }

    /**
     *
     * @param args
     */
    public abstract void run(Object... args);
}
