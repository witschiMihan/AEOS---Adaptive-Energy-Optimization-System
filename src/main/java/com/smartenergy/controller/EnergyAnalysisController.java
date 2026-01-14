package com.smartenergy.controller;

import com.smartenergy.model.EnergyRecord;
import com.smartenergy.model.Machine;
import com.smartenergy.service.EnergyAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin(origins = "*")
public class EnergyAnalysisController {

    @Autowired
    private EnergyAnalyzerService energyAnalyzerService;

    private List<EnergyRecord> records = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();

    /**
     * Upload and process CSV file with energy data
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        try {
            records.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String recordId = parts[0].trim();
                    String machineId = parts[1].trim();
                    double consumption = Double.parseDouble(parts[2].trim());
                    LocalDateTime timestamp = LocalDateTime.now();

                    records.add(new EnergyRecord(recordId, machineId, consumption, timestamp));
                }
            }

            return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully",
                "recordsCount", records.size(),
                "data", records
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all energy records
     */
    @GetMapping("/records")
    public ResponseEntity<?> getRecords() {
        return ResponseEntity.ok(Map.of(
            "count", records.size(),
            "data", records
        ));
    }

    /**
     * Add manual energy record
     */
    @PostMapping("/record")
    public ResponseEntity<?> addRecord(@RequestBody EnergyRecord record) {
        records.add(record);
        return ResponseEntity.ok(Map.of(
            "message", "Record added successfully",
            "data", record
        ));
    }

    /**
     * Calculate energy statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        if (records.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No data available"));
        }

        double average = energyAnalyzerService.calculateAverageConsumption(records);
        double max = energyAnalyzerService.findMaxConsumption(records);
        double min = energyAnalyzerService.findMinConsumption(records);
        double total = energyAnalyzerService.calculateTotalConsumption(records);
        double stdDev = energyAnalyzerService.calculateStandardDeviation(records);

        return ResponseEntity.ok(Map.of(
            "average", String.format("%.2f", average),
            "max", String.format("%.2f", max),
            "min", String.format("%.2f", min),
            "total", String.format("%.2f", total),
            "stdDeviation", String.format("%.2f", stdDev),
            "recordCount", records.size()
        ));
    }

    /**
     * Get statistics by machine
     */
    @GetMapping("/statistics/byMachine")
    public ResponseEntity<?> getStatisticsByMachine() {
        if (records.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No data available"));
        }

        Map<String, List<EnergyRecord>> groupedByMachine = new LinkedHashMap<>();
        for (EnergyRecord record : records) {
            groupedByMachine.computeIfAbsent(record.getMachineId(), k -> new ArrayList<>()).add(record);
        }

        Map<String, Map<String, Object>> statistics = new LinkedHashMap<>();
        for (Map.Entry<String, List<EnergyRecord>> entry : groupedByMachine.entrySet()) {
            List<EnergyRecord> machineRecords = entry.getValue();
            double average = energyAnalyzerService.calculateAverageConsumption(machineRecords);
            double total = energyAnalyzerService.calculateTotalConsumption(machineRecords);

            statistics.put(entry.getKey(), Map.of(
                "average", String.format("%.2f", average),
                "total", String.format("%.2f", total),
                "count", machineRecords.size()
            ));
        }

        return ResponseEntity.ok(statistics);
    }

    /**
     * Register a new machine
     */
    @PostMapping("/machine")
    public ResponseEntity<?> addMachine(@RequestBody Machine machine) {
        machines.add(machine);
        return ResponseEntity.ok(Map.of(
            "message", "Machine registered successfully",
            "data", machine
        ));
    }

    /**
     * Get all registered machines
     */
    @GetMapping("/machines")
    public ResponseEntity<?> getMachines() {
        return ResponseEntity.ok(Map.of(
            "count", machines.size(),
            "data", machines
        ));
    }

    /**
     * Clear all data
     */
    @PostMapping("/clear")
    public ResponseEntity<?> clearData() {
        records.clear();
        machines.clear();
        return ResponseEntity.ok(Map.of("message", "All data cleared"));
    }
}
