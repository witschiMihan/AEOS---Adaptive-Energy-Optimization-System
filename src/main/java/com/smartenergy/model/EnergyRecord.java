package com.smartenergy.model;

import java.time.LocalDateTime;

public class EnergyRecord {
    private String recordId;
    private String machineId;
    private double consumption;
    private LocalDateTime timestamp;

    public EnergyRecord(String recordId, String machineId, double consumption, LocalDateTime timestamp) {
        this.recordId = recordId;
        this.machineId = machineId;
        this.consumption = consumption;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public double getConsumption() { return consumption; }
    public void setConsumption(double consumption) { this.consumption = consumption; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
