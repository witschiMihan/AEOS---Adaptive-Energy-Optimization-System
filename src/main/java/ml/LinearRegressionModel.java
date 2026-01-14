package ml;

import java.util.List;

public class LinearRegressionModel {

    private double slope;
    private double intercept;
    private double rSquared;

    public void train(double[] x, double[] y) {
        double xMean = mean(x);
        double yMean = mean(y);

        double numerator = 0;
        double denominator = 0;
        double ssRes = 0;
        double ssTot = 0;

        for (int i = 0; i < x.length; i++) {
            numerator += (x[i] - xMean) * (y[i] - yMean);
            denominator += Math.pow(x[i] - xMean, 2);
        }

        slope = numerator / denominator;
        intercept = yMean - slope * xMean;

        // Calculate R-squared
        for (int i = 0; i < x.length; i++) {
            double predicted = predict(x[i]);
            ssRes += Math.pow(y[i] - predicted, 2);
            ssTot += Math.pow(y[i] - yMean, 2);
        }
        this.rSquared = 1 - (ssRes / ssTot);
    }

    public void train(List<Double> xValues, List<Double> yValues) {
        if (xValues.size() != yValues.size()) {
            throw new IllegalArgumentException("X and Y values must have the same length");
        }

        double[] x = new double[xValues.size()];
        double[] y = new double[yValues.size()];

        for (int i = 0; i < xValues.size(); i++) {
            x[i] = xValues.get(i);
            y[i] = yValues.get(i);
        }

        train(x, y);
    }

    public double predict(double x) {
        return slope * x + intercept;
    }

    private double mean(double[] data) {
        double sum = 0;
        for (double d : data) {
            sum += d;
        }
        return sum / data.length;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    public double getRSquared() {
        return rSquared;
    }

    public void setRSquared(double rSquared) {
        this.rSquared = rSquared;
    }
}
