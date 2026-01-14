package service;

import java.util.List;
import model.EnergyRecord;

public class EnergyAnalyzer {

    /**
     * Calculate the average energy consumption from a list of records
     * @param records list of energy records
     * @return average energy consumption
     */
    public double calculateAverageConsumption(List<EnergyRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (EnergyRecord record : records) {
            sum += record.getEnergyConsumption();
        }
        return sum / records.size();
    }

    /**
     * Find the maximum energy consumption from a list of records
     * @param records list of energy records
     * @return maximum energy consumption value
     */
    public double findMaxConsumption(List<EnergyRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        double max = records.get(0).getEnergyConsumption();
        for (EnergyRecord record : records) {
            if (record.getEnergyConsumption() > max) {
                max = record.getEnergyConsumption();
            }
        }
        return max;
    }

    /**
     * Find the minimum energy consumption from a list of records
     * @param records list of energy records
     * @return minimum energy consumption value
     */
    public double findMinConsumption(List<EnergyRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        double min = records.get(0).getEnergyConsumption();
        for (EnergyRecord record : records) {
            if (record.getEnergyConsumption() < min) {
                min = record.getEnergyConsumption();
            }
        }
        return min;
    }

    /**
     * Calculate the total energy consumption from a list of records
     * @param records list of energy records
     * @return total energy consumption
     */
    public double calculateTotalConsumption(List<EnergyRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (EnergyRecord record : records) {
            total += record.getEnergyConsumption();
        }
        return total;
    }

    /**
     * Calculate the standard deviation of energy consumption
     * @param records list of energy records
     * @return standard deviation
     */
    public double calculateStandardDeviation(List<EnergyRecord> records) {
        if (records == null || records.size() < 2) {
            return 0;
        }
        double average = calculateAverageConsumption(records);
        double sumSquaredDiff = 0;
        for (EnergyRecord record : records) {
            double diff = record.getEnergyConsumption() - average;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / (records.size() - 1));
    }
}
