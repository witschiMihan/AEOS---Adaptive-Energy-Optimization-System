package ml;

import java.io.*;
import java.util.*;
import java.nio.file.Files;

/**
 * Model Persistence Module
 * Save and load trained ML models to/from disk
 */
public class ModelPersistence {

    /**
     * Save trained Linear Regression model
     */
    public static boolean saveLinearModel(LinearRegressionModel model, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("type", "LinearRegression");
            modelData.put("slope", model.getSlope());
            modelData.put("intercept", model.getIntercept());
            modelData.put("rSquared", model.getRSquared());
            modelData.put("timestamp", System.currentTimeMillis());
            
            oos.writeObject(modelData);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load Linear Regression model
     */
    public static LinearRegressionModel loadLinearModel(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> modelData = (Map<String, Object>) ois.readObject();
            
            LinearRegressionModel model = new LinearRegressionModel();
            double slope = (double) modelData.get("slope");
            double intercept = (double) modelData.get("intercept");
            model.setSlope(slope);
            model.setIntercept(intercept);
            
            return model;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading model: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save Adaptive ML model
     */
    public static boolean saveAdaptiveModel(AdaptiveMLBitCorrection model, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            String json = model.exportModel();
            writer.write(json);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving adaptive model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save Ensemble model
     */
    public static boolean saveEnsembleModel(EnsembleMLModel model, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("type", "EnsembleML");
            modelData.put("timestamp", System.currentTimeMillis());
            modelData.put("modelData", model);
            
            oos.writeObject(modelData);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving ensemble model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load Ensemble model
     */
    public static EnsembleMLModel loadEnsembleModel(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> modelData = (Map<String, Object>) ois.readObject();
            return (EnsembleMLModel) modelData.get("modelData");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading ensemble model: " + e.getMessage());
            return null;
        }
    }

    /**
     * Export model metadata as JSON
     */
    public static String exportModelMetadata(String modelName, String modelType, 
            Map<String, Double> metrics) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append(String.format("  \"modelName\": \"%s\",\n", modelName));
        json.append(String.format("  \"modelType\": \"%s\",\n", modelType));
        json.append(String.format("  \"timestamp\": %d,\n", System.currentTimeMillis()));
        json.append("  \"metrics\": {\n");
        
        boolean first = true;
        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            if (!first) json.append(",\n");
            json.append(String.format("    \"%s\": %.6f", entry.getKey(), entry.getValue()));
            first = false;
        }
        
        json.append("\n  }\n");
        json.append("}\n");
        
        return json.toString();
    }

    /**
     * Save model metadata
     */
    public static boolean saveMetadata(String modelName, String modelType,
            Map<String, Double> metrics, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            String json = exportModelMetadata(modelName, modelType, metrics);
            writer.write(json);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving metadata: " + e.getMessage());
            return false;
        }
    }

    /**
     * List all saved models in a directory
     */
    public static List<String> listSavedModels(String directory) {
        List<String> models = new ArrayList<>();
        File dir = new File(directory);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".model"));
            if (files != null) {
                for (File file : files) {
                    models.add(file.getName());
                }
            }
        }
        
        return models;
    }

    /**
     * Get model creation timestamp
     */
    public static long getModelTimestamp(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) ois.readObject();
            return (long) data.getOrDefault("timestamp", 0L);
        } catch (IOException | ClassNotFoundException e) {
            return 0;
        }
    }

    /**
     * Delete model file
     */
    public static boolean deleteModel(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * Export all models as backup
     */
    public static boolean exportModelsBackup(String sourceDir, String backupPath) {
        try {
            File source = new File(sourceDir);
            File backup = new File(backupPath);
            
            if (!backup.exists()) {
                backup.mkdirs();
            }
            
            File[] files = source.listFiles((d, name) -> name.endsWith(".model"));
            if (files != null) {
                for (File file : files) {
                    Files.copy(file.toPath(), new File(backup, file.getName()).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error backing up models: " + e.getMessage());
            return false;
        }
    }
}

// Add to LinearRegressionModel
class LinearRegressionModelExtended extends LinearRegressionModel {
    private double slope;
    private double intercept;

    public void loadModel(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }
}
