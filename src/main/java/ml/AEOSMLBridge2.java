package ml;

import java.io.*;
import java.net.Socket;
import ml.AEOSMLBridge.MLData;

/**
 * AEOS ML Bridge 2 - Alternative implementation
 * Uses MLData structure instead of JSONObject
 */
public class AEOSMLBridge2 {

    private Socket socket;
    private BufferedReader in;
    private boolean connected = false;

    public void connect() {
        try {
            socket = new Socket("127.0.0.1", 5050);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("[AEOS] Connected to ML Engine");
        } catch (Exception e) {
            connected = false;
            System.out.println("[AEOS] ML Engine not running!");
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
        } catch (Exception e) {
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
                            case "turbine_rpm":
                            case "turbineRpm":
                                data.turbineRpm = Double.parseDouble(value);
                                break;
                            case "solar_output":
                            case "solarOutput":
                                data.solarOutput = Double.parseDouble(value);
                                break;
                            case "grid_load":
                            case "gridLoad":
                                data.gridLoad = Double.parseDouble(value);
                                break;
                            case "rig_consumption":
                            case "rigConsumption":
                                data.rigConsumption = Double.parseDouble(value);
                                break;
                            case "data_center_load":
                            case "dataCenterLoad":
                                data.dataCenterLoad = Double.parseDouble(value);
                                break;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid numeric values
                    }
                }
            }
        } catch (Exception e) {
            // Return default MLData if parsing fails
        }
        
        return data;
    }

    /**
     * Check if connected to ML Engine
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Disconnect from ML Engine
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            // Ignore
        }
    }
}
