package ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * AEOS ML Data Bridge
 * Connects Java AEOS Dashboard to Python ML Engine via Socket
 * Streams real-time ML predictions and optimizations
 */
public class AEOSMLBridge {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;
    private String host = "127.0.0.1";
    private int port = 5050;
    
    // ML Data structure - Enhanced with model insights
    public static class MLData {
        // Real-time operational data
        public double load;
        public double pressure;
        public double efficiency;
        public int anomaly;
        public long timestamp;
        public double turbineRpm;
        public double solarOutput;
        public double gridLoad;
        public double rigConsumption;
        public double dataCenterLoad;
        
        // ML Model insights (from trained model)
        public String machineId;
        public double errorRate;
        public double correctionFactor;
        public double reliability;
        public double predictedLoadNextHour;
        public boolean maintenanceRecommended;
        
        public MLData() {
            // Default values
            load = 0.0;
            pressure = 0.0;
            efficiency = 0.0;
            anomaly = 0;
            timestamp = System.currentTimeMillis();
            turbineRpm = 0.0;
            solarOutput = 0.0;
            gridLoad = 0.0;
            rigConsumption = 0.0;
            dataCenterLoad = 0.0;
            
            // ML model defaults
            machineId = "UNKNOWN";
            errorRate = 0.0;
            correctionFactor = 1.0;
            reliability = 1.0;
            predictedLoadNextHour = 0.0;
            maintenanceRecommended = false;
        }
    }
    
    /**
     * Connect to Python ML Engine
     * @return true if connection successful
     */
    public boolean connect() {
        return connect(host, port);
    }
    
    /**
     * Connect to Python ML Engine with custom host/port
     * @param host ML server host
     * @param port ML server port
     * @return true if connection successful
     */
    public boolean connect(String host, int port) {
        this.host = host;
        this.port = port;
        
        try {
            socket = new Socket(host, port);
            // Set socket timeout to prevent indefinite blocking (5 seconds)
            socket.setSoTimeout(5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("✓ Connected to AEOS ML Engine at " + host + ":" + port);
            return true;
        } catch (Exception e) {
            connected = false;
            System.err.println("✗ Failed to connect to ML Engine: " + e.getMessage());
            System.err.println("  Make sure Python ML server is running: python aeos_ml_server.py");
            return false;
        }
    }
    
    /**
     * Disconnect from ML Engine
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from ML Engine");
        } catch (Exception e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Get live ML data from Python engine
     * @return MLData object or null if error/not connected
     */
    public MLData getLiveData() {
        if (!connected || in == null) {
            return null;
        }
        
        try {
            String line = in.readLine();
            if (line == null || line.trim().isEmpty()) {
                return null;
            }
            
            return parseJSON(line);
        } catch (java.net.SocketTimeoutException e) {
            // Timeout is normal - server sends data every second, so we might timeout between sends
            // Don't disconnect, just return null and try again
            return null;
        } catch (IOException e) {
            System.err.println("Error reading ML data: " + e.getMessage());
            connected = false;
            return null;
        }
    }
    
    /**
     * Simple JSON parser for ML data format
     * Parses: {"load": 75.5, "pressure": 150.2, ...}
     */
    private MLData parseJSON(String json) {
        MLData data = new MLData();
        
        try {
            // Remove whitespace and braces
            json = json.trim();
            if (json.startsWith("{")) json = json.substring(1);
            if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
            
            // Split by comma
            String[] pairs = json.split(",");
            
            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.contains(":")) {
                    String[] keyValue = pair.split(":", 2);
                    String key = keyValue[0].trim().replace("\"", "").replace("'", "");
                    String value = keyValue[1].trim().replace("\"", "").replace("'", "");
                    
                    try {
                        switch (key) {
                            case "load":
                                data.load = Double.parseDouble(value);
                                break;
                            case "pressure":
                                data.pressure = Double.parseDouble(value);
                                break;
                            case "efficiency":
                                data.efficiency = Double.parseDouble(value);
                                break;
                            case "anomaly":
                                data.anomaly = Integer.parseInt(value);
                                break;
                            case "timestamp":
                                data.timestamp = Long.parseLong(value);
                                break;
                            case "turbine_rpm":
                                data.turbineRpm = Double.parseDouble(value);
                                break;
                            case "solar_output":
                                data.solarOutput = Double.parseDouble(value);
                                break;
                            case "grid_load":
                                data.gridLoad = Double.parseDouble(value);
                                break;
                            case "rig_consumption":
                                data.rigConsumption = Double.parseDouble(value);
                                break;
                            case "data_center_load":
                                data.dataCenterLoad = Double.parseDouble(value);
                                break;
                            case "machine_id":
                                data.machineId = value;
                                break;
                            case "error_rate":
                                data.errorRate = Double.parseDouble(value);
                                break;
                            case "correction_factor":
                                data.correctionFactor = Double.parseDouble(value);
                                break;
                            case "reliability":
                                data.reliability = Double.parseDouble(value);
                                break;
                            case "predicted_load_next_hour":
                                data.predictedLoadNextHour = Double.parseDouble(value);
                                break;
                            case "maintenance_recommended":
                                data.maintenanceRecommended = Boolean.parseBoolean(value) || 
                                                             value.equalsIgnoreCase("true") || 
                                                             value.equals("1");
                                break;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
        
        return data;
    }
    
    /**
     * Check if connected to ML Engine
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    /**
     * Reconnect to ML Engine
     * @return true if reconnection successful
     */
    public boolean reconnect() {
        disconnect();
        try {
            Thread.sleep(1000); // Wait 1 second before reconnecting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return connect();
    }
}

