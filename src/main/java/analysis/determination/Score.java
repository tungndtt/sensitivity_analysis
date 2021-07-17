package analysis.determination;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Score {

    public static void main(String[] args) {
        List<Number[]> variations = new LinkedList<>();
        variations.add(new Double[]{-200.0, 0.78});
        variations.add(new Double[]{-160.0, 0.7});
        variations.add(new Double[]{-50.0, 0.42});
        variations.add(new Double[]{-20.0, 0.2});
        variations.add(new Double[]{15.0, 0.18});
        variations.add(new Double[]{50.0, 0.24});
        variations.add(new Double[]{100.0, 0.64});
        variations.add(new Double[]{180.0, 0.7});

        System.out.println(calculateScore(variations, 0.5));
    }

    public static double calculateScore(List<Number[]> variations, double bias) {

        double negative = 0.0, positive = 0.0, sNegative = 0.0, sPositive = 0.0;

        double left = 0.0, right = 0.0, l = 0, r = 0;

        int count = 0;

        for(Number[] _variation : variations) {
            if(_variation[0].doubleValue() < 0) {
                double c = 1.0 / Math.log(-_variation[0].doubleValue() + 1);
                negative += _variation[1].doubleValue() * c;
                sNegative += c;

                left += _variation[1].doubleValue();
                l += 1;

                count += _variation[1].doubleValue() >= 0.19 ? 1 : 0;
            }
            else if(_variation[0].doubleValue() > 0) {
                double c = 1.0 / Math.log(_variation[0].doubleValue() + 1);
                positive += _variation[1].doubleValue() * c;
                sPositive += c;

                right += _variation[1].doubleValue();
                r += 1;
            }
        }

        double result = 0.0, res = 0.0;
        if(sNegative > 0) {
            result += negative * (1 - bias) / sNegative;
            res += left * (1 - bias) / l;
            System.out.println("Average changing rate on the negative side = " + left / l);
        }
        if(sPositive > 0) {
            result += positive * bias / sPositive;
            res += right * bias / r;
            System.out.println("Average changing rate on the positive side = " + right / r);
        }

        return 1.0 - result;
    }

    public static double calculateScore(List<Number[]> variations, double theta, double bias) {
        double lastCR = 0.0, lastCDF = 0.0, left = 0.0, right = 0.0;
        Iterator<Number[]> iterator = variations.iterator();
        while(iterator.hasNext()) {
            Number[] numbers = iterator.next();
            if(numbers[0].doubleValue() < 0) {
                double cdf = Score.CNDF(numbers[0].doubleValue(), 0, theta);
                left += numbers[1].doubleValue() * (cdf - lastCDF);
                lastCDF = cdf;
                lastCR = numbers[1].doubleValue();
            }
            else {
                left += lastCR * (0.5 - lastCDF);
                double cdf = Score.CNDF(numbers[0].doubleValue(), 0, theta);
                right += numbers[1].doubleValue() * (cdf - 0.5);
                lastCDF = cdf;
                lastCR = numbers[1].doubleValue();
                break;
            }
        }
        while(iterator.hasNext()) {
            Number[] numbers = iterator.next();
            double cdf = Score.CNDF(numbers[0].doubleValue(), 0, theta);
            right += numbers[1].doubleValue() * (cdf - lastCDF);
            lastCDF = cdf;
            lastCR = numbers[1].doubleValue();
        }
        right += lastCR * (1.0 - lastCDF);

        return left * (1 - bias) + right * bias;
    }

    /**
     * Returns the cumulative normal distribution function (CNDF) with Mean u and Variance theta
     * @param x
     * @param u Mean
     * @param theta Variance
     * @return
     */
    private static double CNDF(double x, double u, double theta) {
        x = (x - u) / theta;
        int neg = (x < 0d) ? 1 : 0;
        if (neg == 1) {
            x *= -1d;
        }

        double k = 1d / ( 1d + 0.2316419 * x);
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) * k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return (1d - neg) * y + neg * (1d - y);
    }
}
