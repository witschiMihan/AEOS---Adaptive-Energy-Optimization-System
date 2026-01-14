package app;

import java.util.ArrayList;
import java.util.List;
import model.EnergyRecord;
import model.Machine;
import service.EnergyAnalyzer;

public class MainApp {

    public static void main(String[] args) {

        System.out.println("=== SMART ENERGY CONSUMPTION & BIT CORRECTION SYSTEM ===");

        // Create sample data
        List<EnergyRecord> records = new ArrayList<>();
        records.add(new EnergyRecord("R1", "M-001", 45.5, java.time.LocalDateTime.now()));
        records.add(new EnergyRecord("R2", "M-001", 52.3, java.time.LocalDateTime.now().plusHours(1)));
        records.add(new EnergyRecord("R3", "M-002", 38.7, java.time.LocalDateTime.now().plusHours(2)));
        records.add(new EnergyRecord("R4", "M-002", 41.2, java.time.LocalDateTime.now().plusHours(3)));
        records.add(new EnergyRecord("R5", "M-003", 55.0, java.time.LocalDateTime.now().plusHours(4)));

        System.out.println("Sample data created with " + records.size() + " records");

        // Create analyzer
        EnergyAnalyzer analyzer = new EnergyAnalyzer();

        // Analyze data
        System.out.println("\n--- ANALYSIS RESULTS ---");
        System.out.println("Average Consumption: " + String.format("%.2f", analyzer.calculateAverageConsumption(records)) + " kWh");
        System.out.println("Max Consumption: " + String.format("%.2f", analyzer.findMaxConsumption(records)) + " kWh");
        System.out.println("Min Consumption: " + String.format("%.2f", analyzer.findMinConsumption(records)) + " kWh");
        System.out.println("Total Consumption: " + String.format("%.2f", analyzer.calculateTotalConsumption(records)) + " kWh");
        System.out.println("Std Deviation: " + String.format("%.2f", analyzer.calculateStandardDeviation(records)) + " kWh");

        // Create machines
        List<Machine> machines = new ArrayList<>();
        machines.add(new Machine("M-001", "Pump A", "Warehouse 1"));
        machines.add(new Machine("M-002", "Pump B", "Warehouse 2"));
        machines.add(new Machine("M-003", "Compressor", "Building 3"));

        System.out.println("\n--- REGISTERED MACHINES ---");
        for (Machine machine : machines) {
            System.out.println(machine.getMachineId() + ": " + machine.getMachineName() + " at " + machine.getLocation());
        }

        System.out.println("\n=== SYSTEM EXECUTION COMPLETED ===");
    }
}
