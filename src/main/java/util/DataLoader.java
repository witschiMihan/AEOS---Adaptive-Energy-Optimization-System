package util;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.EnergyRecord;
import model.Machine;

public class DataLoader {

    /**
     * Load energy records from a CSV file
     * @param filePath path to the CSV file
     * @return list of energy records
     */
    public List<EnergyRecord> loadEnergyRecordsFromCSV(String filePath) {
        List<EnergyRecord> records = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    EnergyRecord record = new EnergyRecord(
                        parts[0].trim(),                           // recordId
                        parts[1].trim(),                           // machineId
                        Double.parseDouble(parts[2].trim()),       // energyConsumption
                        LocalDateTime.parse(parts[3].trim())       // timestamp
                    );
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        
        return records;
    }

    /**
     * Load machines from a CSV file
     * @param filePath path to the CSV file
     * @return list of machines
     */
    public List<Machine> loadMachinesFromCSV(String filePath) {
        List<Machine> machines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Machine machine = new Machine(
                        parts[0].trim(),  // machineId
                        parts[1].trim(),  // machineName
                        parts[2].trim()   // location
                    );
                    machines.add(machine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        
        return machines;
    }

    /**
     * Save energy records to a CSV file
     * @param filePath path to save the CSV file
     * @param records list of energy records
     */
    public void saveEnergyRecordsToCSV(String filePath, List<EnergyRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("RecordId,MachineId,EnergyConsumption,Timestamp,ErrorBits");
            
            // Write data
            for (EnergyRecord record : records) {
                writer.println(record.getRecordId() + "," +
                             record.getMachineId() + "," +
                             record.getEnergyConsumption() + "," +
                             record.getTimestamp() + "," +
                             record.getErrorBits());
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * Save machines to a CSV file
     * @param filePath path to save the CSV file
     * @param machines list of machines
     */
    public void saveMachinestoCSV(String filePath, List<Machine> machines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("MachineId,MachineName,Location,IsActive");
            
            // Write data
            for (Machine machine : machines) {
                writer.println(machine.getMachineId() + "," +
                             machine.getMachineName() + "," +
                             machine.getLocation() + "," +
                             machine.isActive());
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
}
