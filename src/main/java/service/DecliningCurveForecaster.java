package service;

import model.EnergyRecord;
import java.util.*;

/**
 * Decline Curve Forecasting for energy consumption
 * Models production decline patterns commonly seen in energy systems
 * Includes: Exponential Decline, Hyperbolic Decline, Harmonic Decline
 */
public class DecliningCurveForecaster {
    
    public static class DeclineModel {
        public final String type; // "EXPONENTIAL", "HYPERBOLIC", "HARMONIC"
        public final double declineRate;
        public final double initialRate;
        public final double[] historicalData;
        public final double[] forecast;
        public final double r2Score; // Goodness of fit
        
        public DeclineModel(String type, double declineRate, double initialRate,
                           double[] historical, double[] forecast, double r2) {
            this.type = type;
            this.declineRate = declineRate;
            this.initialRate = initialRate;
            this.historicalData = historical;
            this.forecast = forecast;
            this.r2Score = r2;
        }
    }
    
    /**
     * Analyze declining curve pattern from energy records
     */
    public static DeclineModel analyzeDecline(List<EnergyRecord> records, String machineId, int forecastMonths) {
        List<EnergyRecord> machineRecords = records.stream()
            .filter(r -> r.getMachineId().equals(machineId))
            .sorted(Comparator.comparing(EnergyRecord::getTimestamp))
            .toList();
        
        if (machineRecords.size() < 3) {
            return null;
        }
        
        // Extract consumption values
        double[] data = machineRecords.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .toArray();
        
        // Calculate decline rate
        double initialRate = data[0];
        double finalRate = data[data.length - 1];
        int periods = data.length - 1;
        
        // Exponential decline: q = q_i * e^(-D*t)
        double exponentialDecline = Math.log(finalRate / initialRate) / periods;
        double exponentialRate = -exponentialDecline;
        
        // Hyperbolic decline: q = q_i / (1 + D*b*t)^(1/b)
        // Approximation: calculate average percentage decline per period
        double hyperbolicRate = (initialRate - finalRate) / (initialRate * periods);
        
        // Harmonic decline: q = q_i / (1 + D*t)
        double harmonicRate = (1 - finalRate / initialRate) / periods;
        
        // Generate forecast using exponential decline (most conservative)
        double[] forecast = new double[forecastMonths];
        for (int i = 1; i <= forecastMonths; i++) {
            forecast[i - 1] = initialRate * Math.exp(-exponentialRate * (periods + i));
        }
        
        // Calculate R² fit (simplified)
        double r2 = calculateFitQuality(data);
        
        return new DeclineModel(
            "EXPONENTIAL",
            exponentialRate,
            initialRate,
            data,
            forecast,
            r2
        );
    }
    
    /**
     * Calculate R² goodness of fit
     */
    private static double calculateFitQuality(double[] data) {
        double mean = Arrays.stream(data).average().orElse(0);
        double ssTotal = 0;
        double ssRes = 0;
        
        for (int i = 0; i < data.length; i++) {
            ssTotal += Math.pow(data[i] - mean, 2);
            // Simplified residual calculation
            ssRes += Math.pow(data[i] - (data[0] * Math.exp(-0.05 * i)), 2);
        }
        
        return ssTotal == 0 ? 0 : 1 - (ssRes / ssTotal);
    }
    
    /**
     * Compare actual vs forecasted decline
     */
    public static double compareWithActual(DeclineModel model, List<EnergyRecord> records, String machineId) {
        List<Double> actual = records.stream()
            .filter(r -> r.getMachineId().equals(machineId))
            .sorted(Comparator.comparing(EnergyRecord::getTimestamp))
            .skip(model.historicalData.length)
            .map(EnergyRecord::getEnergyConsumption)
            .toList();
        
        if (actual.isEmpty()) {
            return 0;
        }
        
        double error = 0;
        for (int i = 0; i < Math.min(actual.size(), model.forecast.length); i++) {
            error += Math.abs(actual.get(i) - model.forecast[i]) / model.forecast[i];
        }
        
        return error / Math.min(actual.size(), model.forecast.length);
    }
    
    /**
     * Get forecast description
     */
    public static String getDeclineDescription(DeclineModel model) {
        StringBuilder desc = new StringBuilder();
        desc.append("DECLINE CURVE ANALYSIS\n");
        desc.append("========================\n\n");
        desc.append(String.format("Type: %s Decline\n", model.type));
        desc.append(String.format("Initial Rate: %.2f kWh\n", model.initialRate));
        desc.append(String.format("Decline Rate: %.4f\n", model.declineRate));
        desc.append(String.format("Model Fit (R²): %.4f\n\n", model.r2Score));
        
        desc.append("FORECAST (Next 12 months):\n");
        for (int i = 0; i < model.forecast.length; i++) {
            desc.append(String.format("Month %2d: %8.2f kWh\n", i + 1, model.forecast[i]));
        }
        
        return desc.toString();
    }
}
