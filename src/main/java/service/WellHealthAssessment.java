package service;

import model.EnergyRecord;
import java.util.*;

/**
 * Well Health assessment system
 * Calculates comprehensive health score for each machine based on multiple factors
 * Uses color-coded status (RED, YELLOW, GREEN)
 */
public class WellHealthAssessment {
    
    public enum HealthStatus {
        RED(0.0, 60.0, "Critical - Immediate Action Required"),
        YELLOW(60.0, 80.0, "Warning - Monitor Closely"),
        GREEN(80.0, 100.0, "Good - Operating Normally");
        
        public final double minScore;
        public final double maxScore;
        public final String description;
        
        HealthStatus(double min, double max, String desc) {
            this.minScore = min;
            this.maxScore = max;
            this.description = desc;
        }
    }
    
    public static class HealthScore {
        public final String machineId;
        public final double overallScore; // 0-100
        public final HealthStatus status;
        
        public final double reliabilityScore;    // 0-100
        public final double efficiencyScore;      // 0-100
        public final double stabilityScore;       // 0-100
        public final double trendScore;           // 0-100
        
        public final String summary;
        
        public HealthScore(String machineId, double overall, HealthStatus status,
                          double reliability, double efficiency, double stability, 
                          double trend, String summary) {
            this.machineId = machineId;
            this.overallScore = overall;
            this.status = status;
            this.reliabilityScore = reliability;
            this.efficiencyScore = efficiency;
            this.stabilityScore = stability;
            this.trendScore = trend;
            this.summary = summary;
        }
    }
    
    /**
     * Calculate well health for a specific machine
     */
    public static HealthScore calculateWellHealth(String machineId, List<EnergyRecord> records) {
        List<EnergyRecord> machineRecords = records.stream()
            .filter(r -> r.getMachineId().equals(machineId))
            .sorted(Comparator.comparing(EnergyRecord::getTimestamp))
            .toList();
        
        if (machineRecords.isEmpty()) {
            return new HealthScore(machineId, 0, HealthStatus.RED, 0, 0, 0, 0, "No data available");
        }
        
        // Calculate component scores
        double reliability = calculateReliability(machineRecords);
        double efficiency = calculateEfficiency(machineRecords);
        double stability = calculateStability(machineRecords);
        double trend = calculateTrend(machineRecords);
        
        // Calculate weighted overall score
        double overall = (reliability * 0.35) + (efficiency * 0.25) + (stability * 0.25) + (trend * 0.15);
        
        // Determine status
        HealthStatus status = getStatus(overall);
        
        // Generate summary
        String summary = generateSummary(machineId, overall, status, reliability, efficiency, stability, trend);
        
        return new HealthScore(machineId, overall, status, reliability, efficiency, stability, trend, summary);
    }
    
    /**
     * Reliability Score - Based on error bits and error rate
     * 100 = no errors, 0 = very high error rate
     */
    private static double calculateReliability(List<EnergyRecord> records) {
        double totalErrorBits = records.stream().mapToLong(EnergyRecord::getErrorBits).sum();
        double avgErrorBits = totalErrorBits / records.size();
        
        // Scale: 0 error bits = 100, 10+ error bits = 0
        return Math.max(0, Math.min(100, 100 - (avgErrorBits * 10)));
    }
    
    /**
     * Efficiency Score - Based on consumption consistency and utilization
     * 100 = steady consumption, 0 = highly variable
     */
    private static double calculateEfficiency(List<EnergyRecord> records) {
        double[] consumption = records.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .toArray();
        
        double mean = Arrays.stream(consumption).average().orElse(0);
        if (mean == 0) return 0;
        
        double variance = 0;
        for (double val : consumption) {
            variance += Math.pow(val - mean, 2);
        }
        variance /= consumption.length;
        
        double stdDev = Math.sqrt(variance);
        double coeffVar = stdDev / mean; // Coefficient of variation
        
        // Scale: CV of 0.1 (10%) = 100, CV > 0.5 (50%) = 0
        return Math.max(0, Math.min(100, 100 - (coeffVar * 200)));
    }
    
    /**
     * Stability Score - Based on consumption trend consistency
     * 100 = stable/predictable trend, 0 = erratic/unstable
     */
    private static double calculateStability(List<EnergyRecord> records) {
        if (records.size() < 3) return 50;
        
        double[] consumption = records.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .toArray();
        
        // Calculate period-over-period changes
        double[] changes = new double[consumption.length - 1];
        for (int i = 0; i < changes.length; i++) {
            changes[i] = Math.abs(consumption[i + 1] - consumption[i]);
        }
        
        double avgChange = Arrays.stream(changes).average().orElse(0);
        double maxChange = Arrays.stream(changes).max().orElse(0);
        double mean = Arrays.stream(consumption).average().orElse(0);
        
        if (mean == 0) return 50;
        
        // Calculate volatility ratio
        double volatilityRatio = maxChange / mean;
        
        // Scale: volatility ratio 0.1 (10%) = 100, > 0.5 (50%) = 0
        return Math.max(0, Math.min(100, 100 - (volatilityRatio * 200)));
    }
    
    /**
     * Trend Score - Based on consumption direction and rate of change
     * 100 = stable/improving, 0 = deteriorating rapidly
     */
    private static double calculateTrend(List<EnergyRecord> records) {
        if (records.size() < 3) return 50;
        
        double[] consumption = records.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .toArray();
        
        // Split data into first half and second half
        int mid = consumption.length / 2;
        double firstHalfAvg = 0, secondHalfAvg = 0;
        
        for (int i = 0; i < mid; i++) {
            firstHalfAvg += consumption[i];
        }
        firstHalfAvg /= mid;
        
        for (int i = mid; i < consumption.length; i++) {
            secondHalfAvg += consumption[i];
        }
        secondHalfAvg /= (consumption.length - mid);
        
        // Calculate trend
        double trendPercent = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100;
        
        // Scale: -10% decline = 100, +30% increase = 0
        double score = 100 - Math.abs(trendPercent) * 2;
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Get health status based on score
     */
    private static HealthStatus getStatus(double score) {
        if (score < 60) return HealthStatus.RED;
        if (score < 80) return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }
    
    /**
     * Generate health summary description
     */
    private static String generateSummary(String machineId, double overall, HealthStatus status,
                                         double reliability, double efficiency, double stability, double trend) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Machine %s Health: %s\n", machineId, status.description));
        sb.append(String.format("Overall Score: %.1f/100\n\n", overall));
        sb.append(String.format("Reliability:  %.1f/100\n", reliability));
        sb.append(String.format("Efficiency:   %.1f/100\n", efficiency));
        sb.append(String.format("Stability:    %.1f/100\n", stability));
        sb.append(String.format("Trend:        %.1f/100\n\n", trend));
        
        if (overall < 60) {
            sb.append("CRITICAL ISSUES:\n");
            if (reliability < 60) sb.append("  • High error rate detected\n");
            if (efficiency < 60) sb.append("  • Inconsistent power consumption\n");
            if (stability < 60) sb.append("  • Erratic behavior detected\n");
        } else if (overall < 80) {
            sb.append("ITEMS TO MONITOR:\n");
            if (reliability < 80) sb.append("  • Check for maintenance needs\n");
            if (efficiency < 80) sb.append("  • Consider optimization\n");
            if (stability < 80) sb.append("  • Monitor for degradation\n");
        } else {
            sb.append("SYSTEM OPERATING NORMALLY\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Get color code for UI display
     */
    public static String getStatusColor(HealthStatus status) {
        return switch (status) {
            case RED -> "#FF0000";
            case YELLOW -> "#FFD700";
            case GREEN -> "#00AA00";
        };
    }
}
