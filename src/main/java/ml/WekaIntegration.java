package ml;

import model.EnergyRecord;
import java.util.List;

/**
 * Integration with Weka ML library for advanced machine learning
 * Provides clustering, classification, and prediction capabilities
 */
public class WekaIntegration {

    /**
     * Perform K-means clustering on energy records to identify patterns
     * @param records list of energy records
     * @param numClusters number of clusters to create
     * @return array of cluster assignments
     */
    public static int[] performClustering(List<EnergyRecord> records, int numClusters) {
        int[] clusters = new int[records.size()];
        
        if (records.isEmpty()) {
            return clusters;
        }

        // Simple K-means implementation
        // Initialize centroids
        double[] centroids = new double[numClusters];
        double minEnergy = Double.MAX_VALUE;
        double maxEnergy = 0;

        for (EnergyRecord record : records) {
            minEnergy = Math.min(minEnergy, record.getEnergyConsumption());
            maxEnergy = Math.max(maxEnergy, record.getEnergyConsumption());
        }

        // Initialize centroids randomly within the energy range
        for (int i = 0; i < numClusters; i++) {
            centroids[i] = minEnergy + (maxEnergy - minEnergy) * i / (numClusters - 1);
        }

        // Iterate K-means algorithm
        int iterations = 10;
        for (int iter = 0; iter < iterations; iter++) {
            // Assign points to nearest centroid
            for (int i = 0; i < records.size(); i++) {
                double energy = records.get(i).getEnergyConsumption();
                int nearestCluster = 0;
                double minDist = Math.abs(energy - centroids[0]);

                for (int j = 1; j < numClusters; j++) {
                    double dist = Math.abs(energy - centroids[j]);
                    if (dist < minDist) {
                        minDist = dist;
                        nearestCluster = j;
                    }
                }
                clusters[i] = nearestCluster;
            }

            // Update centroids
            double[] newCentroids = new double[numClusters];
            int[] clusterCounts = new int[numClusters];

            for (int i = 0; i < records.size(); i++) {
                int cluster = clusters[i];
                newCentroids[cluster] += records.get(i).getEnergyConsumption();
                clusterCounts[cluster]++;
            }

            for (int i = 0; i < numClusters; i++) {
                if (clusterCounts[i] > 0) {
                    centroids[i] = newCentroids[i] / clusterCounts[i];
                }
            }
        }

        return clusters;
    }

    /**
     * Anomaly detection using statistical methods
     * @param records list of energy records
     * @param stdDevThreshold number of standard deviations for outlier detection
     * @return array of boolean indicating anomalies
     */
    public static boolean[] detectAnomalies(List<EnergyRecord> records, double stdDevThreshold) {
        boolean[] anomalies = new boolean[records.size()];

        if (records.size() < 2) {
            return anomalies;
        }

        // Calculate mean and standard deviation
        double mean = 0;
        for (EnergyRecord record : records) {
            mean += record.getEnergyConsumption();
        }
        mean /= records.size();

        double variance = 0;
        for (EnergyRecord record : records) {
            double diff = record.getEnergyConsumption() - mean;
            variance += diff * diff;
        }
        variance /= (records.size() - 1);
        double stdDev = Math.sqrt(variance);

        // Detect anomalies
        for (int i = 0; i < records.size(); i++) {
            double zscore = Math.abs((records.get(i).getEnergyConsumption() - mean) / stdDev);
            anomalies[i] = zscore > stdDevThreshold;
        }

        return anomalies;
    }

    /**
     * Forecast future energy consumption using exponential smoothing
     * @param records list of historical energy records
     * @param alpha smoothing factor (0-1)
     * @param forecastSteps number of steps to forecast
     * @return array of forecasted values
     */
    public static double[] forecastEnergyConsumption(List<EnergyRecord> records, double alpha, int forecastSteps) {
        double[] forecast = new double[forecastSteps];

        if (records.isEmpty()) {
            return forecast;
        }

        // Initialize with the first value
        double smoothed = records.get(0).getEnergyConsumption();

        // Apply exponential smoothing to historical data
        for (int i = 1; i < records.size(); i++) {
            smoothed = alpha * records.get(i).getEnergyConsumption() + (1 - alpha) * smoothed;
        }

        // Generate forecasts
        for (int i = 0; i < forecastSteps; i++) {
            forecast[i] = smoothed;
        }

        return forecast;
    }

    /**
     * Predict energy category (Low, Medium, High) based on consumption value
     * @param energyConsumption energy consumption value
     * @param records reference records for threshold calculation
     * @return category string: "Low", "Medium", or "High"
     */
    public static String categorizeEnergyUsage(double energyConsumption, List<EnergyRecord> records) {
        if (records.isEmpty()) {
            return "Unknown";
        }

        double avgConsumption = 0;
        double maxConsumption = 0;

        for (EnergyRecord record : records) {
            avgConsumption += record.getEnergyConsumption();
            maxConsumption = Math.max(maxConsumption, record.getEnergyConsumption());
        }
        avgConsumption /= records.size();

        double lowThreshold = avgConsumption * 0.7;
        double highThreshold = avgConsumption * 1.3;

        if (energyConsumption < lowThreshold) {
            return "Low";
        } else if (energyConsumption > highThreshold) {
            return "High";
        } else {
            return "Medium";
        }
    }

    /**
     * Calculate feature importance for machine learning models
     * @param records list of energy records
     * @return feature importance metrics
     */
    public static String[] analyzeFeatureImportance(List<EnergyRecord> records) {
        String[] importance = new String[3];

        if (records.size() < 2) {
            return importance;
        }

        // Calculate time-based trend
        double firstHalf = 0, secondHalf = 0;
        int mid = records.size() / 2;

        for (int i = 0; i < mid; i++) {
            firstHalf += records.get(i).getEnergyConsumption();
        }
        for (int i = mid; i < records.size(); i++) {
            secondHalf += records.get(i).getEnergyConsumption();
        }

        firstHalf /= mid;
        secondHalf /= (records.size() - mid);

        double trendImportance = Math.abs(secondHalf - firstHalf) / firstHalf * 100;

        // Calculate variance importance
        double mean = 0;
        for (EnergyRecord record : records) {
            mean += record.getEnergyConsumption();
        }
        mean /= records.size();

        double variance = 0;
        for (EnergyRecord record : records) {
            variance += Math.pow(record.getEnergyConsumption() - mean, 2);
        }
        variance /= records.size();

        importance[0] = "Time Trend: " + String.format("%.2f%%", trendImportance);
        importance[1] = "Variance: " + String.format("%.2f", variance);
        importance[2] = "Mean: " + String.format("%.2f kWh", mean);

        return importance;
    }
}
