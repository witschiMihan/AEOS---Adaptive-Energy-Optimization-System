package model;

public class Machine {
    private String machineId;
    private String machineName;
    private String location;
    private boolean isActive;

    public Machine() {
    }

    public Machine(String machineId, String machineName, String location) {
        this.machineId = machineId;
        this.machineName = machineName;
        this.location = location;
        this.isActive = true;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "machineId='" + machineId + '\'' +
                ", machineName='" + machineName + '\'' +
                ", location='" + location + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
