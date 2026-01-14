package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feature Engineering Module
 * Extracts meaningful features from raw energy data for ML models
 */
public class FeatureEngineer {

    /**
     * Feature set for ML models
     */
    public static class FeatureSet {
        public double baseConsumption;
        public double consumptionTrend;
        public double consumptionVolatility;
        public double errorBitDensity;
        public double errorBitTrend;
        public double hourOfDay;
        public double dayOfWeek;
        public double seasonFactor;
        public double peakDeviation;
        public double anomalyScore;
        
        public double[] toArray() {
            return new double[] {
                baseConsumption, consumptionTrend, consumptionVolatility,
                errorBitDensity, errorBitTrend, hourOfDay, dayOfWeek,
                seasonFactor, peakDeviation, anomalyScore
            };
        }
    }

    /**
     * Extract features from a single energy record and historical context
     */
    public static FeatureSet extractFeatures(EnergyRecord record, List<EnergyRecord> history) {
        FeatureSet features = new FeatureSet();
        
        // 1. Base consumption (normalized)
        double avgHistoricalConsumption = history.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .average()
            .orElse(1.0);
        features.baseConsumption = record.getEnergyConsumption() / avgHistoricalConsumption;
        
        // 2. Consumption trend (momentum)
        if (history.size() >= 2) {
            double recent = history.get(history.size() - 1).getEnergyConsumption();
            double older = history.get(Math.max(0, history.size() - 25)).getEnergyConsumption();
            features.consumptionTrend = (recent - older) / (older + 0.001);
        }
        
        // 3. Consumption volatility (standard deviation)
        double stdDev = calculateStandardDeviation(history);
        features.consumptionVolatility = stdDev / (avgHistoricalConsumption + 0.001);
        
        // 4. Error bit density (proportion of bits that are errors)
        long totalErrorBits = history.stream().mapToLong(EnergyRecord::getErrorBits).sum();
        long totalBits = history.size() * 64; // 64-bit numbers
        features.errorBitDensity = totalErrorBits / (double) totalBits;
        
        // 5. Error bit trend
        if (history.size() >= 2) {
            long recentErrors = history.subList(Math.max(0, history.size() - 10), history.size())
                .stream().mapToLong(EnergyRecord::getErrorBits).sum();
            long olderErrors = history.subList(Math.max(0, history.size() - 20), 
                Math.max(0, history.size() - 10))
                .stream().mapToLong(EnergyRecord::getErrorBits).sum();
            features.errorBitTrend = (recentErrors - olderErrors) / (double) (olderErrors + 1);
        }
        
        // 6. Hour of day feature (0-23)
        String timestamp = record.getTimestamp().toString();
        try {
            int hour = Integer.parseInt(timestamp.substring(11, 13));
            features.hourOfDay = hour / 24.0; // Normalize to 0-1
        } catch (Exception e) {
            features.hourOfDay = 0;
        }
        
        // 7. Day of week feature (0-6)
        long timeMillis = System.currentTimeMillis();
        features.dayOfWeek = (timeMillis / (86400000L * 7)) % 7 / 7.0; // Normalize
        
        // 8. Season factor
        int dayOfYear = (int) ((timeMillis / 86400000L) % 365);
        features.seasonFactor = Math.sin(2 * Math.PI * dayOfYear / 365);
        
        // 9. Peak deviation (how far from peak)
        double maxConsumption = history.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .max()
            .orElse(1.0);
        features.peakDeviation = (maxConsumption - record.getEnergyConsumption()) / maxConsumption;
        
        // 10. Anomaly score (statistical outlier detection)
        double zScore = Math.abs((record.getEnergyConsumption() - avgHistoricalConsumption) 
            / (stdDev + 0.001));
        features.anomalyScore = Math.min(1.0, zScore / 3.0);
        
        return features;
    }

    /**
     * Extract features for all records
     */
    public static List<FeatureSet> extractFeaturesForAll(List<EnergyRecord> records) {
        List<FeatureSet> features = new ArrayList<>();
        
        for (int i = 0; i < records.size(); i++) {
            List<EnergyRecord> history = records.subList(0, Math.min(i + 1, records.size()));
            features.add(extractFeatures(records.get(i), history));
        }
        
        return features;
    }

    /**
     * Polynomial feature expansion
     */
    public static double[] expandPolynomial(FeatureSet features, int degree) {
        double[] baseFeatures = features.toArray();
        List<Double> expanded = new ArrayList<>();
        
        // Add base features
        for (double f : baseFeatures) {
            expanded.add(f);
        }
        
        // Add polynomial terms
        for (int d = 2; d <= degree; d++) {
            for (double f : baseFeatures) {
                expanded.add(Math.pow(f, d));
            }
        }
        
        return expanded.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Interaction feature generation
     */
    public static double[] generateInteractions(FeatureSet features) {
        double[] baseFeatures = features.toArray();
        List<Double> withInteractions = new ArrayList<>();
        
        // Add base features
        for (double f : baseFeatures) {
            withInteractions.add(f);
        }
        
        // Add interaction terms
        for (int i = 0; i < baseFeatures.length; i++) {
            for (int j = i + 1; j < baseFeatures.length; j++) {
                withInteractions.add(baseFeatures[i] * baseFeatures[j]);
            }
        }
        
        return withInteractions.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Feature normalization (zero mean, unit variance)
     */
    public static List<double[]> normalizeFeatures(List<double[]> featureMatrix) {
        if (featureMatrix.isEmpty()) return featureMatrix;
        
        int numFeatures = featureMatrix.get(0).length;
        double[] means = new double[numFeatures];
        double[] stdDevs = new double[numFeatures];
        
        // Calculate means
        for (double[] features : featureMatrix) {
            for (int j = 0; j < numFeatures; j++) {
                means[j] += features[j];
            }
        }
        
        int n = featureMatrix.size();
        for (int j = 0; j < numFeatures; j++) {
            means[j] /= n;
        }
        
        // Calculate standard deviations
        for (double[] features : featureMatrix) {
            for (int j = 0; j < numFeatures; j++) {
                stdDevs[j] += Math.pow(features[j] - means[j], 2);
            }
        }
        
        for (int j = 0; j < numFeatures; j++) {
            stdDevs[j] = Math.sqrt(stdDevs[j] / n);
        }
        
        // Normalize
        List<double[]> normalized = new ArrayList<>();
        for (double[] features : featureMatrix) {
            double[] norm = new double[numFeatures];
            for (int j = 0; j < numFeatures; j++) {
                norm[j] = (features[j] - means[j]) / (stdDevs[j] + 0.001);
            }
            normalized.add(norm);
        }
        
        return normalized;
    }

    /**
     * Feature importance calculation using correlation
     */
    public static Map<Integer, Double> calculateFeatureImportance(
            List<double[]> features, double[] targets) {
        Map<Integer, Double> importance = new HashMap<>();
        
        if (features.isEmpty() || features.get(0).length == 0) {
            return importance;
        }
        
        int numFeatures = features.get(0).length;
        
        for (int j = 0; j < numFeatures; j++) {
            double correlation = calculateCorrelation(
                extractColumn(features, j), targets);
            importance.put(j, Math.abs(correlation));
        }
        
        return importance;
    }

    /**
     * Feature selection (keep top N features)
     */
    public static List<double[]> selectTopFeatures(List<double[]> features,
            double[] targets, int topN) {
        Map<Integer, Double> importance = calculateFeatureImportance(features, targets);
        
        List<Integer> topIndices = importance.entrySet().stream()
            .sorted(Comparator.comparingDouble((Map.Entry<Integer, Double> entry) -> entry.getValue()).reversed())
            .limit(topN)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        List<double[]> selected = new ArrayList<>();
        for (double[] featureVector : features) {
            double[] subset = new double[topN];
            for (int i = 0; i < topN; i++) {
                subset[i] = featureVector[topIndices.get(i)];
            }
            selected.add(subset);
        }
        
        return selected;
    }

    // Helper methods
    private static double calculateStandardDeviation(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        
        double mean = records.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .average()
            .orElse(0);
        
        double variance = records.stream()
            .mapToDouble(r -> Math.pow(r.getEnergyConsumption() - mean, 2))
            .average()
            .orElse(0);
        
        return Math.sqrt(variance);
    }

    private static double calculateCorrelation(double[] x, double[] y) {
        if (x.length != y.length || x.length == 0) return 0;
        
        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);
        
        double covariance = 0;
        double varX = 0, varY = 0;
        
        for (int i = 0; i < x.length; i++) {
            covariance += (x[i] - meanX) * (y[i] - meanY);
            varX += Math.pow(x[i] - meanX, 2);
            varY += Math.pow(y[i] - meanY, 2);
        }
        
        return covariance / Math.sqrt(varX * varY + 0.001);
    }

    private static double[] extractColumn(List<double[]> matrix, int col) {
        double[] column = new double[matrix.size()];
        for (int i = 0; i < matrix.size(); i++) {
            column[i] = matrix.get(i)[col];
        }
        return column;
    }
}
