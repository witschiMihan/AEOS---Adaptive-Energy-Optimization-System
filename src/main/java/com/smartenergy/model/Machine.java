package com.smartenergy.model;

public class Machine {
    private String machineId;
    private String machineName;
    private String location;

    public Machine(String machineId, String machineName, String location) {
        this.machineId = machineId;
        this.machineName = machineName;
        this.location = location;
    }

    // Getters and Setters
    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
