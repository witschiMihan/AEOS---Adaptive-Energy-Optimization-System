package model;

import java.time.LocalDateTime;

public class EnergyRecord {
    private String recordId;
    private String machineId;
    private double energyConsumption;
    private LocalDateTime timestamp;
    private int errorBits;

    public EnergyRecord() {
    }

    public EnergyRecord(String recordId, String machineId, double energyConsumption, LocalDateTime timestamp) {
        this.recordId = recordId;
        this.machineId = machineId;
        this.energyConsumption = energyConsumption;
        this.timestamp = timestamp;
        this.errorBits = 0;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getErrorBits() {
        return errorBits;
    }

    public void setErrorBits(int errorBits) {
        this.errorBits = errorBits;
    }

    @Override
    public String toString() {
        return "EnergyRecord{" +
                "recordId='" + recordId + '\'' +
                ", machineId='" + machineId + '\'' +
                ", energyConsumption=" + energyConsumption +
                ", timestamp=" + timestamp +
                ", errorBits=" + errorBits +
                '}';
    }
}
