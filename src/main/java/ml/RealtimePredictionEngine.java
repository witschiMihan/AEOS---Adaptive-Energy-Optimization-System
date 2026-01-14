package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Real-Time Prediction Engine
 * Handles streaming data and makes online predictions with continuous learning
 */
public class RealtimePredictionEngine {

    /**
     * Prediction with confidence
     */
    public static class RealTimePrediction {
        public double value;
        public double confidence;
        public String status;
        public long timestamp;
        public Map<String, Object> metadata;
        
        public RealTimePrediction(double value, double confidence, String status) {
            this.value = value;
            this.confidence = confidence;
            this.status = status;
            this.timestamp = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
    }

    private final int BUFFER_SIZE = 1000;
    private final BlockingQueue<EnergyRecord> dataBuffer;
    private final LinearRegressionModel onlineModel;
    private final SimpleNeuralNetwork neuralNet;
    private final EnsembleMLModel ensembleModel;
    private final TimeSeriesAnalyzer timeSeriesAnalyzer;
    
    private volatile boolean isRunning = false;
    private ExecutorService executorService;
    private List<EnergyRecord> recentHistory;
    private double anomalyThreshold = 0.7;
    private int updateFrequency = 100; // Update model every 100 records
    private int recordCounter = 0;

    /**
     * Initialize real-time engine
     */
    public RealtimePredictionEngine() {
        this.dataBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
        this.onlineModel = new LinearRegressionModel();
        this.neuralNet = new SimpleNeuralNetwork(1, 64, 32, 16, 1);
        this.ensembleModel = new EnsembleMLModel();
        this.timeSeriesAnalyzer = new TimeSeriesAnalyzer();
        this.recentHistory = Collections.synchronizedList(new ArrayList<>());
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * Start the real-time prediction engine
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        
        // Start data processing thread
        executorService.submit(this::processDataStream);
        
        // Start model update thread
        executorService.submit(this::updateModelOnline);
    }

    /**
     * Stop the engine
     */
    public void stop() {
        isRunning = false;
        executorService.shutdown();
    }

    /**
     * Add data point to stream
     */
    public boolean addDataPoint(EnergyRecord record) {
        try {
            return dataBuffer.offer(record, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Make real-time prediction
     */
    public RealTimePrediction predict(double energyValue) {
        double linearPred = onlineModel.predict(energyValue);
        double neuralPred = neuralNet.predict(new double[]{energyValue});
        double ensemblePred = ensembleModel.predict(recentHistory.size());
        
        // Weighted ensemble
        double prediction = 0.33 * linearPred + 0.33 * neuralPred + 0.34 * ensemblePred;
        
        // Calculate confidence
        double confidence = calculateConfidence(energyValue, prediction);
        
        String status = "NORMAL";
        if (confidence < 0.5) {
            status = "LOW_CONFIDENCE";
        } else if (confidence > 0.9) {
            status = "HIGH_CONFIDENCE";
        }
        
        RealTimePrediction result = new RealTimePrediction(prediction, confidence, status);
        result.metadata.put("linearPrediction", linearPred);
        result.metadata.put("neuralPrediction", neuralPred);
        result.metadata.put("ensemblePrediction", ensemblePred);
        
        return result;
    }

    /**
     * Process data stream
     */
    private void processDataStream() {
        while (isRunning) {
            try {
                EnergyRecord record = dataBuffer.poll(1, TimeUnit.SECONDS);
                
                if (record != null) {
                    recentHistory.add(record);
                    
                    // Keep history within size limit
                    if (recentHistory.size() > BUFFER_SIZE) {
                        recentHistory.remove(0);
                    }
                    
                    recordCounter++;
                    
                    // Update time series analyzer
                    if (recentHistory.size() >= 10) {
                        timeSeriesAnalyzer.buildTimeSeries(recentHistory);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Online model update with incremental learning
     */
    private void updateModelOnline() {
        while (isRunning) {
            try {
                Thread.sleep(5000); // Update every 5 seconds
                
                if (recordCounter >= updateFrequency && !recentHistory.isEmpty()) {
                    // Incremental update to linear model
                    List<Double> xData = new ArrayList<>();
                    List<Double> yData = new ArrayList<>();
                    
                    for (int i = 0; i < recentHistory.size(); i++) {
                        xData.add((double) i);
                        yData.add(recentHistory.get(i).getEnergyConsumption());
                    }
                    
                    // Online update (simple)
                    if (xData.size() >= 2) {
                        onlineModel.train(xData, yData);
                    }
                    
                    // Train neural network on recent batch
                    neuralNet.train(recentHistory, 10);
                    
                    // Update ensemble model
                    ensembleModel.train(recentHistory);
                    
                    recordCounter = 0;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Calculate prediction confidence
     */
    private double calculateConfidence(double input, double prediction) {
        if (recentHistory.isEmpty()) return 0.5;
        
        // Calculate prediction error on recent history
        double mse = 0;
        for (EnergyRecord record : recentHistory) {
            double pred = onlineModel.predict(record.getEnergyConsumption());
            mse += Math.pow(record.getEnergyConsumption() - pred, 2);
        }
        mse /= recentHistory.size();
        
        // Convert MSE to confidence (lower error = higher confidence)
        double rmse = Math.sqrt(mse);
        double avgValue = recentHistory.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .average()
            .orElse(1.0);
        
        double mape = rmse / avgValue;
        return Math.max(0, Math.min(1.0, 1 - mape));
    }

    /**
     * Detect anomalies in real-time
     */
    public boolean isAnomaly(EnergyRecord record) {
        if (recentHistory.size() < 10) return false;
        
        double mean = recentHistory.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .average()
            .orElse(0);
        
        double variance = recentHistory.stream()
            .mapToDouble(r -> Math.pow(r.getEnergyConsumption() - mean, 2))
            .average()
            .orElse(0);
        
        double stdDev = Math.sqrt(variance);
        double zScore = Math.abs((record.getEnergyConsumption() - mean) / (stdDev + 0.001));
        
        return zScore > 3.0; // 3-sigma rule
    }

    /**
     * Get real-time statistics
     */
    public Map<String, Object> getRealtimeStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (recentHistory.isEmpty()) {
            return stats;
        }
        
        double[] values = recentHistory.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .toArray();
        
        double mean = Arrays.stream(values).average().orElse(0);
        double min = Arrays.stream(values).min().orElse(0);
        double max = Arrays.stream(values).max().orElse(0);
        
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        stats.put("mean", mean);
        stats.put("min", min);
        stats.put("max", max);
        stats.put("stdDev", stdDev);
        stats.put("bufferSize", recentHistory.size());
        stats.put("recordsProcessed", recordCounter);
        
        // Trend
        if (recentHistory.size() >= 2) {
            double recent = recentHistory.get(recentHistory.size() - 1).getEnergyConsumption();
            double old = recentHistory.get(Math.max(0, recentHistory.size() - 11)).getEnergyConsumption();
            stats.put("trend", (recent - old) / old);
        }
        
        return stats;
    }

    /**
     * Forecast next N values
     */
    public double[] forecastNext(int steps) {
        double[] forecast = new double[steps];
        double lastValue = recentHistory.isEmpty() ? 0 
            : recentHistory.get(recentHistory.size() - 1).getEnergyConsumption();
        
        for (int i = 0; i < steps; i++) {
            forecast[i] = onlineModel.predict(lastValue + i);
        }
        
        return forecast;
    }

    /**
     * Batch prediction with confidence
     */
    public List<RealTimePrediction> predictBatch(double[] values) {
        return Arrays.stream(values)
            .mapToObj(this::predict)
            .collect(Collectors.toList());
    }

    /**
     * Get model metrics
     */
    public Map<String, Object> getModelMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        if (!recentHistory.isEmpty()) {
            metrics.put("modelAccuracies", ensembleModel.getModelAccuracies());
            metrics.put("performanceMetrics", ensembleModel.getPerformanceMetrics());
        }
        
        return metrics;
    }

    /**
     * Reset engine
     */
    public void reset() {
        recentHistory.clear();
        recordCounter = 0;
        dataBuffer.clear();
    }

    /**
     * Get buffer size
     */
    public int getBufferSize() {
        return dataBuffer.size();
    }

    /**
     * Check if engine is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
