package com.smartenergy.service;

import com.smartenergy.model.EnergyRecord;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EnergyAnalyzerService {

    public double calculateAverageConsumption(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        return records.stream()
                .mapToDouble(EnergyRecord::getConsumption)
                .average()
                .orElse(0);
    }

    public double findMaxConsumption(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        return records.stream()
                .mapToDouble(EnergyRecord::getConsumption)
                .max()
                .orElse(0);
    }

    public double findMinConsumption(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        return records.stream()
                .mapToDouble(EnergyRecord::getConsumption)
                .min()
                .orElse(0);
    }

    public double calculateTotalConsumption(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        return records.stream()
                .mapToDouble(EnergyRecord::getConsumption)
                .sum();
    }

    public double calculateStandardDeviation(List<EnergyRecord> records) {
        if (records.isEmpty()) return 0;
        double average = calculateAverageConsumption(records);
        double variance = records.stream()
                .mapToDouble(r -> Math.pow(r.getConsumption() - average, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }
}
