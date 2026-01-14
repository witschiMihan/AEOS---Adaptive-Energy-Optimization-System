package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Time-Series Analysis and Forecasting
 * Provides trend detection, seasonal decomposition, and forecasting capabilities
 */
public class TimeSeriesAnalyzer {

    private static class TimeSeriesPoint {
        double timestamp;
        double value;
        
        TimeSeriesPoint(double timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private List<TimeSeriesPoint> timeSeries = new ArrayList<>();
    private double[] trend;
    private double[] seasonal;
    private double[] residual;
    private double alpha = 0.3; // Exponential smoothing parameter
    private double beta = 0.2;  // Trend smoothing parameter
    private int seasonalPeriod = 24; // Hourly data with daily pattern

    /**
     * Build time series from energy records
     */
    public void buildTimeSeries(List<EnergyRecord> records) {
        timeSeries.clear();
        
        List<EnergyRecord> sorted = records.stream()
            .sorted(Comparator.comparing(r -> r.getTimestamp().toString()))
            .collect(Collectors.toList());
        
        for (int i = 0; i < sorted.size(); i++) {
            EnergyRecord record = sorted.get(i);
            timeSeries.add(new TimeSeriesPoint(i, record.getEnergyConsumption()));
        }
        
        decomposeTimeSeries();
    }

    /**
     * Decompose time series into trend, seasonal, and residual components
     */
    private void decomposeTimeSeries() {
        if (timeSeries.isEmpty()) return;
        
        int n = timeSeries.size();
        trend = new double[n];
        seasonal = new double[n];
        residual = new double[n];
        
        // Calculate trend using moving average
        int windowSize = Math.min(7, n / 4);
        for (int i = 0; i < n; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - windowSize); j <= Math.min(n - 1, i + windowSize); j++) {
                sum += timeSeries.get(j).value;
                count++;
            }
            trend[i] = sum / count;
        }
        
        // Calculate seasonal component
        for (int s = 0; s < seasonalPeriod && s < n; s++) {
            double sumSeasonal = 0;
            int count = 0;
            for (int i = s; i < n; i += seasonalPeriod) {
                sumSeasonal += timeSeries.get(i).value - trend[i];
                count++;
            }
            double avgSeasonal = count > 0 ? sumSeasonal / count : 0;
            
            for (int i = s; i < n; i += seasonalPeriod) {
                seasonal[i] = avgSeasonal;
            }
        }
        
        // Calculate residual
        for (int i = 0; i < n; i++) {
            residual[i] = timeSeries.get(i).value - trend[i] - seasonal[i];
        }
    }

    /**
     * Exponential smoothing with trend (Holt's method)
     */
    public double[] exponentialSmoothing(int forecastSteps) {
        if (timeSeries.isEmpty()) return new double[0];
        
        int n = timeSeries.size();
        double[] level = new double[n + forecastSteps];
        double[] trendCoeff = new double[n + forecastSteps];
        double[] forecast = new double[n + forecastSteps];
        
        // Initialize
        level[0] = timeSeries.get(0).value;
        trendCoeff[0] = 0;
        
        // Fit to historical data
        for (int i = 1; i < n; i++) {
            double prevLevel = level[i - 1];
            level[i] = alpha * timeSeries.get(i).value + (1 - alpha) * (prevLevel + trendCoeff[i - 1]);
            trendCoeff[i] = beta * (level[i] - prevLevel) + (1 - beta) * trendCoeff[i - 1];
            forecast[i] = level[i] + trendCoeff[i];
        }
        
        // Forecast future values
        for (int h = 1; h <= forecastSteps; h++) {
            level[n + h - 1] = level[n - 1] + h * trendCoeff[n - 1];
            forecast[n + h - 1] = level[n + h - 1];
        }
        
        double[] result = new double[forecastSteps];
        System.arraycopy(forecast, n, result, 0, forecastSteps);
        return result;
    }

    /**
     * ARIMA-style differencing for stationarity
     */
    public double[] differencing(int order) {
        if (timeSeries.size() < order + 1) return new double[0];
        
        double[] diff = new double[timeSeries.size()];
        List<Double> values = timeSeries.stream().map(p -> p.value).collect(Collectors.toList());
        
        for (int d = 0; d < order; d++) {
            List<Double> newDiff = new ArrayList<>();
            for (int i = 1; i < values.size(); i++) {
                newDiff.add(values.get(i) - values.get(i - 1));
            }
            values = newDiff;
        }
        
        for (int i = 0; i < values.size(); i++) {
            diff[i] = values.get(i);
        }
        
        return diff;
    }

    /**
     * Autocorrelation analysis
     */
    public double[] calculateAutocorrelation(int maxLag) {
        if (timeSeries.isEmpty()) return new double[0];
        
        List<Double> values = timeSeries.stream().map(p -> p.value).collect(Collectors.toList());
        int n = values.size();
        double mean = values.stream().mapToDouble(v -> v).average().orElse(0);
        
        double[] acf = new double[maxLag + 1];
        
        double c0 = 0;
        for (int i = 0; i < n; i++) {
            c0 += Math.pow(values.get(i) - mean, 2);
        }
        c0 /= n;
        
        for (int lag = 0; lag <= maxLag; lag++) {
            double c = 0;
            for (int i = 0; i < n - lag; i++) {
                c += (values.get(i) - mean) * (values.get(i + lag) - mean);
            }
            c /= n;
            acf[lag] = c / c0;
        }
        
        return acf;
    }

    /**
     * Trend detection (increasing, decreasing, or stable)
     */
    public String detectTrend() {
        if (trend == null || trend.length < 2) return "INSUFFICIENT_DATA";
        
        int n = trend.length;
        double startTrend = trend[0];
        double endTrend = trend[n - 1];
        double change = (endTrend - startTrend) / Math.abs(startTrend + 0.001);
        
        if (change > 0.05) return "INCREASING";
        if (change < -0.05) return "DECREASING";
        return "STABLE";
    }

    /**
     * Calculate trend strength
     */
    public double getTrendStrength() {
        if (residual == null || residual.length == 0) return 0;
        
        double residualVar = Arrays.stream(residual).map(r -> r * r).average().orElse(0);
        double trendVar = Arrays.stream(trend).map(t -> t * t).average().orElse(0);
        
        return 1.0 - (residualVar / (trendVar + residualVar + 0.001));
    }

    /**
     * Calculate seasonality strength
     */
    public double getSeasonalityStrength() {
        if (residual == null || residual.length == 0) return 0;
        
        double residualVar = Arrays.stream(residual).map(r -> r * r).average().orElse(0);
        double seasonalVar = Arrays.stream(seasonal).map(s -> s * s).average().orElse(0);
        
        return 1.0 - (residualVar / (seasonalVar + residualVar + 0.001));
    }

    /**
     * Get trend component
     */
    public double[] getTrend() {
        return trend;
    }

    /**
     * Get seasonal component
     */
    public double[] getSeasonal() {
        return seasonal;
    }

    /**
     * Forecast with confidence intervals
     */
    public Map<String, Object> forecastWithCI(int steps, double confidenceLevel) {
        double[] forecast = exponentialSmoothing(steps);
        
        // Calculate confidence interval width
        OptionalDouble avg = Arrays.stream(residual).map(r -> r * r).average();
        double residualStdDev = avg.isPresent() ? Math.sqrt(avg.getAsDouble()) : 1.0;
        
        double zScore = confidenceLevel == 0.95 ? 1.96 : (confidenceLevel == 0.99 ? 2.576 : 1.645);
        
        Map<String, Object> result = new HashMap<>();
        result.put("forecast", forecast);
        result.put("confidence", confidenceLevel);
        result.put("intervalWidth", zScore * residualStdDev);
        result.put("lowerBound", Arrays.stream(forecast)
            .map(f -> f - zScore * residualStdDev)
            .toArray());
        result.put("upperBound", Arrays.stream(forecast)
            .map(f -> f + zScore * residualStdDev)
            .toArray());
        
        return result;
    }
}
