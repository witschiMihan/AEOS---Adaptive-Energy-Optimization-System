package service;

import model.EnergyRecord;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages live alert notifications for energy monitoring
 * Tracks threshold violations, anomalies, and system events
 */
public class AlertNotificationManager {
    
    public static class Alert {
        public enum Severity { INFO, WARNING, CRITICAL }
        
        public final String id;
        public final Severity severity;
        public final String message;
        public final LocalDateTime timestamp;
        public final String machineId;
        public final String alertType; // "THRESHOLD", "ANOMALY", "ERROR_RATE", "RELIABILITY"
        
        public Alert(String id, Severity severity, String message, LocalDateTime timestamp, 
                     String machineId, String alertType) {
            this.id = id;
            this.severity = severity;
            this.message = message;
            this.timestamp = timestamp;
            this.machineId = machineId;
            this.alertType = alertType;
        }
    }
    
    private List<Alert> alerts;
    private Map<String, Double> thresholds; // Machine ID -> consumption threshold
    private double globalErrorRateThreshold;
    private double globalReliabilityFloor;
    
    public AlertNotificationManager() {
        this.alerts = new ArrayList<>();
        this.thresholds = new HashMap<>();
        this.globalErrorRateThreshold = 5.0; // 5% error rate threshold
        this.globalReliabilityFloor = 85.0; // 85% reliability floor
    }
    
    /**
     * Monitor records and generate alerts
     */
    public List<Alert> analyzeAndAlert(List<EnergyRecord> records, 
                                       Map<String, Double> machineErrorRates,
                                       Map<String, Double> machineReliability) {
        alerts.clear();
        
        for (EnergyRecord record : records) {
            String machineId = record.getMachineId();
            double consumption = record.getEnergyConsumption();
            int errorBits = record.getErrorBits();
            
            // Check consumption threshold
            if (thresholds.containsKey(machineId)) {
                double threshold = thresholds.get(machineId);
                if (consumption > threshold * 1.2) {
                    alerts.add(new Alert(
                        UUID.randomUUID().toString(),
                        Alert.Severity.WARNING,
                        String.format("Machine %s exceeds threshold: %.2f kWh (threshold: %.2f kWh)",
                            machineId, consumption, threshold),
                        record.getTimestamp(),
                        machineId,
                        "THRESHOLD"
                    ));
                }
            }
            
            // Check error bits
            if (errorBits > 5) {
                alerts.add(new Alert(
                    UUID.randomUUID().toString(),
                    Alert.Severity.WARNING,
                    String.format("Machine %s high error bits: %d", machineId, errorBits),
                    record.getTimestamp(),
                    machineId,
                    "ERROR_BITS"
                ));
            }
        }
        
        // Check error rates
        for (String machineId : machineErrorRates.keySet()) {
            double errorRate = machineErrorRates.get(machineId);
            if (errorRate > globalErrorRateThreshold) {
                alerts.add(new Alert(
                    UUID.randomUUID().toString(),
                    Alert.Severity.CRITICAL,
                    String.format("Machine %s error rate critical: %.2f%%", machineId, errorRate * 100),
                    LocalDateTime.now(),
                    machineId,
                    "ERROR_RATE"
                ));
            }
        }
        
        // Check reliability
        for (String machineId : machineReliability.keySet()) {
            double reliability = machineReliability.get(machineId);
            if (reliability < globalReliabilityFloor) {
                alerts.add(new Alert(
                    UUID.randomUUID().toString(),
                    Alert.Severity.CRITICAL,
                    String.format("Machine %s reliability low: %.2f%%", machineId, reliability * 100),
                    LocalDateTime.now(),
                    machineId,
                    "RELIABILITY"
                ));
            }
        }
        
        return alerts;
    }
    
    /**
     * Get alerts by severity
     */
    public List<Alert> getAlertsBySeverity(Alert.Severity severity) {
        return alerts.stream()
            .filter(a -> a.severity == severity)
            .toList();
    }
    
    /**
     * Get alerts for specific machine
     */
    public List<Alert> getAlertsForMachine(String machineId) {
        return alerts.stream()
            .filter(a -> a.machineId.equals(machineId))
            .toList();
    }
    
    /**
     * Set consumption threshold for machine
     */
    public void setMachineThreshold(String machineId, double threshold) {
        thresholds.put(machineId, threshold);
    }
    
    /**
     * Get all active alerts
     */
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(alerts);
    }
    
    /**
     * Get alert summary
     */
    public String getAlertSummary() {
        long critical = alerts.stream().filter(a -> a.severity == Alert.Severity.CRITICAL).count();
        long warning = alerts.stream().filter(a -> a.severity == Alert.Severity.WARNING).count();
        long info = alerts.stream().filter(a -> a.severity == Alert.Severity.INFO).count();
        
        return String.format("Critical: %d | Warning: %d | Info: %d", critical, warning, info);
    }
    
    /**
     * Clear old alerts (older than hours)
     */
    public void clearOldAlerts(int hoursOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursOld);
        alerts.removeIf(a -> a.timestamp.isBefore(cutoff));
    }
}
