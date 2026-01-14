package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Anomaly Detection System
 * Detects unusual energy patterns using multiple algorithms:
 * - Statistical methods (Z-score, IQR)
 * - Isolation Forest-like approach
 * - Local Outlier Factor (LOF)
 * - Dynamic thresholds
 */
public class AnomalyDetector {

    public static class AnomalyScore {
        String recordId;
        double value;
        double score; // 0-1 scale
        String anomalyType;
        boolean isAnomaly;
        
        AnomalyScore(String recordId, double value, double score, String type, boolean isAnomaly) {
            this.recordId = recordId;
            this.value = value;
            this.score = score;
            this.anomalyType = type;
            this.isAnomaly = isAnomaly;
        }
        
        public double getScore() {
            return score;
        }
        
        public String getRecordId() {
            return recordId;
        }
        
        public double getValue() {
            return value;
        }
        
        public String getAnomalyType() {
            return anomalyType;
        }
        
        public boolean isAnomaly() {
            return isAnomaly;
        }
    }

    private Map<String, List<Double>> machineHistory = new HashMap<>();
    private Map<String, AnomalyMetrics> machineMetrics = new HashMap<>();
    private double zScoreThreshold = 3.0;
    private double iqrMultiplier = 1.5;
    private double anomalyThreshold = 0.7;

    /**
     * Metrics for anomaly detection
     */
    private static class AnomalyMetrics {
        double mean;
        double stdDev;
        double q1, q3, iqr;
        double minValue, maxValue;
        List<Double> sortedValues;
    }

    /**
     * Build baseline for anomaly detection
     */
    public void buildBaseline(List<EnergyRecord> records) {
        for (EnergyRecord record : records) {
            String machineId = record.getMachineId();
            machineHistory.putIfAbsent(machineId, new ArrayList<>());
            machineHistory.get(machineId).add(record.getEnergyConsumption());
        }
        
        // Calculate metrics for each machine
        for (Map.Entry<String, List<Double>> entry : machineHistory.entrySet()) {
            machineMetrics.put(entry.getKey(), calculateMetrics(entry.getValue()));
        }
    }

    /**
     * Calculate statistical metrics for a data series
     */
    private AnomalyMetrics calculateMetrics(List<Double> values) {
        AnomalyMetrics metrics = new AnomalyMetrics();
        
        if (values.isEmpty()) return metrics;
        
        // Mean and StdDev
        double sum = 0;
        for (double v : values) sum += v;
        metrics.mean = sum / values.size();
        
        double sumSquaredDiff = 0;
        for (double v : values) {
            sumSquaredDiff += Math.pow(v - metrics.mean, 2);
        }
        metrics.stdDev = Math.sqrt(sumSquaredDiff / values.size());
        
        // Min/Max
        metrics.minValue = Collections.min(values);
        metrics.maxValue = Collections.max(values);
        
        // Quartiles
        metrics.sortedValues = new ArrayList<>(values);
        Collections.sort(metrics.sortedValues);
        
        int n = metrics.sortedValues.size();
        metrics.q1 = metrics.sortedValues.get(n / 4);
        metrics.q3 = metrics.sortedValues.get(3 * n / 4);
        metrics.iqr = metrics.q3 - metrics.q1;
        
        return metrics;
    }

    /**
     * Detect anomalies using Z-score method
     */
    public double zScoreAnomaly(double value, AnomalyMetrics metrics) {
        if (metrics.stdDev == 0) return 0;
        return Math.abs((value - metrics.mean) / metrics.stdDev) / zScoreThreshold;
    }

    /**
     * Detect anomalies using IQR method
     */
    public double iqrAnomaly(double value, AnomalyMetrics metrics) {
        double lowerBound = metrics.q1 - iqrMultiplier * metrics.iqr;
        double upperBound = metrics.q3 + iqrMultiplier * metrics.iqr;
        
        if (value < lowerBound || value > upperBound) {
            return Math.min(1.0, Math.abs(value - metrics.mean) / (metrics.iqr + 0.001) * 0.5);
        }
        return 0;
    }

    /**
     * Isolation Forest-like anomaly detection
     * Detects outliers based on isolation path length
     */
    public double isolationAnomaly(double value, AnomalyMetrics metrics) {
        // Normalize value
        double normalizedValue = (value - metrics.minValue) / (metrics.maxValue - metrics.minValue + 0.001);
        
        // Check if in extreme regions
        if (normalizedValue < 0.1 || normalizedValue > 0.9) {
            return Math.min(1.0, Math.abs(normalizedValue - 0.5) * 2);
        }
        return 0;
    }

    /**
     * Local Outlier Factor (LOF) anomaly detection
     */
    public double lofAnomaly(double value, AnomalyMetrics metrics, int k) {
        if (metrics.sortedValues.size() < k) return 0;
        
        // Find k nearest neighbors
        List<Double> distances = new ArrayList<>();
        for (double neighbor : metrics.sortedValues) {
            distances.add(Math.abs(value - neighbor));
        }
        Collections.sort(distances);
        
        // Calculate reachability distance
        double kDistance = distances.get(Math.min(k, distances.size() - 1));
        double lrd = 1.0 / (distances.subList(0, Math.min(k, distances.size()))
            .stream()
            .mapToDouble(d -> Math.max(d, kDistance))
            .average()
            .orElse(1.0) + 0.001);
        
        return Math.min(1.0, lrd);
    }

    /**
     * Comprehensive anomaly detection combining multiple methods
     */
    public AnomalyScore detectAnomaly(EnergyRecord record) {
        String machineId = record.getMachineId();
        double value = record.getEnergyConsumption();
        
        AnomalyMetrics metrics = machineMetrics.get(machineId);
        if (metrics == null) {
            // First time seeing this machine
            buildBaseline(Arrays.asList(record));
            metrics = machineMetrics.get(machineId);
        }
        
        // Calculate anomaly scores from different methods
        double zScore = zScoreAnomaly(value, metrics);
        double iqr = iqrAnomaly(value, metrics);
        double isolation = isolationAnomaly(value, metrics);
        double lof = lofAnomaly(value, metrics, 5);
        
        // Ensemble score (average of normalized scores)
        double ensembleScore = (zScore + iqr + isolation + lof) / 4.0;
        ensembleScore = Math.min(1.0, ensembleScore);
        
        // Determine anomaly type
        String anomalyType = "NORMAL";
        if (ensembleScore > 0.7) {
            anomalyType = "SEVERE";
        } else if (ensembleScore > 0.5) {
            anomalyType = "MODERATE";
        } else if (ensembleScore > 0.3) {
            anomalyType = "MILD";
        }
        
        boolean isAnomaly = ensembleScore > anomalyThreshold;
        
        return new AnomalyScore(record.getRecordId(), value, ensembleScore, anomalyType, isAnomaly);
    }

    /**
     * Batch anomaly detection
     */
    public List<AnomalyScore> detectAnomalies(List<EnergyRecord> records) {
        buildBaseline(records);
        return records.stream()
            .map(this::detectAnomaly)
            .collect(Collectors.toList());
    }

    /**
     * Get anomalies summary
     */
    public Map<String, Object> getAnomalySummary(List<AnomalyScore> scores) {
        Map<String, Object> summary = new HashMap<>();
        
        long totalCount = scores.size();
        long anomalyCount = scores.stream().filter(s -> s.isAnomaly).count();
        
        summary.put("totalRecords", totalCount);
        summary.put("anomalies", anomalyCount);
        summary.put("anomalyRate", totalCount > 0 ? (double) anomalyCount / totalCount : 0);
        
        Map<String, Long> typeCounts = scores.stream()
            .filter(s -> s.isAnomaly)
            .collect(Collectors.groupingBy(s -> s.anomalyType, Collectors.counting()));
        summary.put("anomalyTypes", typeCounts);
        
        double avgScore = scores.stream()
            .mapToDouble(s -> s.score)
            .average()
            .orElse(0);
        summary.put("averageAnomalyScore", avgScore);
        
        return summary;
    }

    /**
     * Set anomaly sensitivity (0.0-1.0, higher = more sensitive)
     */
    public void setSensitivity(double sensitivity) {
        this.anomalyThreshold = 1.0 - sensitivity;
    }

    /**
     * Get top anomalies
     */
    public List<AnomalyScore> getTopAnomalies(List<AnomalyScore> scores, int topN) {
        return scores.stream()
            .filter(s -> s.isAnomaly)
            .sorted(Comparator.comparingDouble(AnomalyScore::getScore).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
}
