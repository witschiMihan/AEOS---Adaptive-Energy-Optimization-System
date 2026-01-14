package ml;

import model.EnergyRecord;
import java.util.*;

/**
 * Adaptive ML-Based Bit Correction System
 * Self-learning system that adapts correction strategies based on observed error patterns
 */
public class AdaptiveMLBitCorrection {

    private static class ErrorPattern {
        String machineId;
        double energyValue;
        int errorBits;
        double correctionFactor;
        long timestamp;
        
        ErrorPattern(String machineId, double energyValue, int errorBits, double correctionFactor) {
            this.machineId = machineId;
            this.energyValue = energyValue;
            this.errorBits = errorBits;
            this.correctionFactor = correctionFactor;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private List<ErrorPattern> trainingData = new ArrayList<>();
    private Map<String, Double> machineErrorRates = new HashMap<>();
    private Map<String, Double> machineCorrectionFactors = new HashMap<>();
    private double globalErrorThreshold = 0.05; // 5% error rate threshold
    private int minSamplesForAdaptation = 10;

    /**
     * Train the adaptive correction model on observed error patterns
     * @param errorPatterns list of energy records with known corrections
     */
    public void trainModel(List<EnergyRecord> errorPatterns) {
        trainingData.clear();
        machineErrorRates.clear();
        
        for (EnergyRecord record : errorPatterns) {
            double detectedError = detectErrorPattern(record);
            double correctionFactor = calculateCorrectionFactor(record.getEnergyConsumption(), detectedError);
            
            ErrorPattern pattern = new ErrorPattern(
                record.getMachineId(),
                record.getEnergyConsumption(),
                record.getErrorBits(),
                correctionFactor
            );
            trainingData.add(pattern);
            
            updateMachineMetrics(record.getMachineId(), detectedError, correctionFactor);
        }
    }

    /**
     * Detect error pattern in energy record using statistical analysis
     * @param record energy record to analyze
     * @return detected error magnitude
     */
    private double detectErrorPattern(EnergyRecord record) {
        // Analyze bit pattern for anomalies
        long bits = Double.doubleToLongBits(record.getEnergyConsumption());
        int bitCount = Long.bitCount(bits);
        
        // Calculate deviation from expected distribution
        double expectedBitCount = 32; // 50% of 64 bits should be 1
        return Math.abs(bitCount - expectedBitCount) / expectedBitCount;
    }

    /**
     * Calculate adaptive correction factor based on machine history
     * @param energyValue energy consumption value
     * @param detectedError detected error magnitude
     * @return correction factor
     */
    private double calculateCorrectionFactor(double energyValue, double detectedError) {
        // Adaptive correction: higher for high-error machines
        if (detectedError > 0.3) {
            return 1.0 + (detectedError * 0.5); // Up to 15% correction
        } else if (detectedError > 0.15) {
            return 1.0 + (detectedError * 0.3); // Up to 4.5% correction
        } else {
            return 1.0 + (detectedError * 0.1); // Up to 1.5% correction
        }
    }

    /**
     * Update machine-specific correction metrics
     * @param machineId machine identifier
     * @param errorRate detected error rate
     * @param correctionFactor applied correction factor
     */
    private void updateMachineMetrics(String machineId, double errorRate, double correctionFactor) {
        // Exponential moving average for error rate
        double currentRate = machineErrorRates.getOrDefault(machineId, 0.0);
        double alpha = 0.3; // Learning rate
        double newRate = (alpha * errorRate) + ((1 - alpha) * currentRate);
        machineErrorRates.put(machineId, newRate);
        
        // Track correction factor
        machineCorrectionFactors.put(machineId, correctionFactor);
    }

    /**
     * Apply adaptive ML correction to energy record
     * @param record energy record to correct
     * @return corrected energy value
     */
    public double adaptiveCorrect(EnergyRecord record) {
        String machineId = record.getMachineId();
        double originalValue = record.getEnergyConsumption();
        
        // Detect error pattern
        double detectedError = detectErrorPattern(record);
        
        // Get machine-specific correction factor
        double correctionFactor = machineCorrectionFactors.getOrDefault(machineId, 1.0);
        
        // Apply adaptive correction
        double correctedValue = originalValue * correctionFactor;
        
        // Validate correction (sanity check)
        if (Math.abs(correctedValue - originalValue) > originalValue * 0.5) {
            // Correction too large, use conservative approach
            correctedValue = originalValue * (1.0 + detectedError * 0.1);
        }
        
        return correctedValue;
    }

    /**
     * Get predictive correction for future values based on machine history
     * @param machineId machine identifier
     * @param energyValue predicted energy value
     * @return corrected prediction
     */
    public double predictiveCorrect(String machineId, double energyValue) {
        double correctionFactor = machineCorrectionFactors.getOrDefault(machineId, 1.0);
        return energyValue * correctionFactor;
    }

    /**
     * Analyze machine reliability based on learned patterns
     * @param machineId machine identifier
     * @return reliability score (0.0 to 1.0, higher is better)
     */
    public double getMachineReliability(String machineId) {
        double errorRate = machineErrorRates.getOrDefault(machineId, 0.0);
        // Convert error rate to reliability score
        return Math.max(0.0, 1.0 - errorRate);
    }

    /**
     * Get confidence level for corrections
     * @param machineId machine identifier
     * @return confidence score (0.0 to 1.0)
     */
    public double getCorrectionConfidence(String machineId) {
        // Based on number of samples trained on
        long sampleCount = trainingData.stream()
            .filter(p -> p.machineId.equals(machineId))
            .count();
        
        return Math.min(1.0, sampleCount / (double) minSamplesForAdaptation);
    }

    /**
     * Get adaptive correction recommendations
     * @return map of recommendations for system improvement
     */
    public Map<String, String> getRecommendations() {
        Map<String, String> recommendations = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : machineErrorRates.entrySet()) {
            String machineId = entry.getKey();
            double errorRate = entry.getValue();
            
            if (errorRate > globalErrorThreshold) {
                recommendations.put(machineId, 
                    String.format("High error rate (%.1f%%). Recommend maintenance inspection.", errorRate * 100));
            } else if (errorRate > globalErrorThreshold * 0.5) {
                recommendations.put(machineId, 
                    String.format("Elevated error rate (%.1f%%). Monitor closely.", errorRate * 100));
            } else {
                recommendations.put(machineId, "Nominal operation. No action required.");
            }
        }
        
        return recommendations;
    }

    /**
     * Get machine statistics for analytics dashboard
     * @return detailed statistics
     */
    public Map<String, Object> getMachineStatistics(String machineId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("machineId", machineId);
        stats.put("errorRate", machineErrorRates.getOrDefault(machineId, 0.0));
        stats.put("correctionFactor", machineCorrectionFactors.getOrDefault(machineId, 1.0));
        stats.put("reliability", getMachineReliability(machineId));
        stats.put("confidence", getCorrectionConfidence(machineId));
        
        long sampleCount = trainingData.stream()
            .filter(p -> p.machineId.equals(machineId))
            .count();
        stats.put("samplesProcessed", sampleCount);
        
        return stats;
    }

    /**
     * Reset adaptive learning for a specific machine
     * @param machineId machine identifier
     */
    public void resetMachineAdaptation(String machineId) {
        trainingData.removeIf(p -> p.machineId.equals(machineId));
        machineErrorRates.remove(machineId);
        machineCorrectionFactors.remove(machineId);
    }

    /**
     * Export trained model data
     * @return JSON string representation of model
     */
    public String exportModel() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"machineProfiles\": {\n");
        
        boolean first = true;
        for (Map.Entry<String, Double> entry : machineErrorRates.entrySet()) {
            if (!first) json.append(",\n");
            json.append(String.format("    \"%s\": {\n", entry.getKey()));
            json.append(String.format("      \"errorRate\": %.4f,\n", entry.getValue()));
            json.append(String.format("      \"correctionFactor\": %.4f,\n", 
                machineCorrectionFactors.getOrDefault(entry.getKey(), 1.0)));
            json.append(String.format("      \"reliability\": %.4f\n", getMachineReliability(entry.getKey())));
            json.append("    }");
            first = false;
        }
        
        json.append("\n  },\n");
        json.append(String.format("  \"globalThreshold\": %.4f,\n", globalErrorThreshold));
        json.append(String.format("  \"samplesProcessed\": %d\n", trainingData.size()));
        json.append("}");
        
        return json.toString();
    }
}
