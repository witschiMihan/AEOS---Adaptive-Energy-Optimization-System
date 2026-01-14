package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ensemble Machine Learning Model
 * Combines multiple ML algorithms for robust predictions:
 * - Linear Regression
 * - Decision Tree-like model
 * - Support Vector Regression-like model
 * - k-Nearest Neighbors
 * - Random Forest-like approach
 */
public class EnsembleMLModel {

    private LinearRegressionModel linearModel;
    private DecisionTreeModel treeModel;
    private KNearestNeighborsModel knnModel;
    private RandomForestModel randomForestModel;
    
    private List<Double> xTrainingData;
    private List<Double> yTrainingData;
    private double[] modelWeights = {0.25, 0.25, 0.25, 0.25}; // Equal weight by default

    /**
     * Initialize ensemble model with component models
     */
    public EnsembleMLModel() {
        this.linearModel = new LinearRegressionModel();
        this.treeModel = new DecisionTreeModel();
        this.knnModel = new KNearestNeighborsModel(5);
        this.randomForestModel = new RandomForestModel(10, 5);
        this.xTrainingData = new ArrayList<>();
        this.yTrainingData = new ArrayList<>();
    }

    /**
     * Train ensemble on energy records
     */
    public void train(List<EnergyRecord> records) {
        if (records.isEmpty()) return;
        
        // Convert records to training data
        xTrainingData.clear();
        yTrainingData.clear();
        
        for (int i = 0; i < records.size(); i++) {
            xTrainingData.add((double) i);
            yTrainingData.add(records.get(i).getEnergyConsumption());
        }
        
        // Train each model
        linearModel.train(xTrainingData, yTrainingData);
        treeModel.train(xTrainingData, yTrainingData);
        knnModel.train(xTrainingData, yTrainingData);
        randomForestModel.train(xTrainingData, yTrainingData);
    }

    /**
     * Make prediction using all models
     */
    public double predict(double x) {
        double linearPred = linearModel.predict(x);
        double treePred = treeModel.predict(x);
        double knnPred = knnModel.predict(x);
        double rfPred = randomForestModel.predict(x);
        
        // Weighted average
        double prediction = modelWeights[0] * linearPred + 
                           modelWeights[1] * treePred +
                           modelWeights[2] * knnPred +
                           modelWeights[3] * rfPred;
        
        return prediction;
    }

    /**
     * Get individual model predictions
     */
    public Map<String, Double> getPredictions(double x) {
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("linear", linearModel.predict(x));
        predictions.put("tree", treeModel.predict(x));
        predictions.put("knn", knnModel.predict(x));
        predictions.put("randomForest", randomForestModel.predict(x));
        predictions.put("ensemble", predict(x));
        return predictions;
    }

    /**
     * Set model weights for weighted ensemble
     */
    public void setModelWeights(double[] weights) {
        if (weights.length != 4) {
            throw new IllegalArgumentException("Must provide 4 weights");
        }
        double sum = Arrays.stream(weights).sum();
        for (int i = 0; i < 4; i++) {
            this.modelWeights[i] = weights[i] / sum; // Normalize
        }
    }

    /**
     * Get model accuracy scores
     */
    public Map<String, Double> getModelAccuracies() {
        Map<String, Double> accuracies = new HashMap<>();
        
        if (xTrainingData.isEmpty()) return accuracies;
        
        double linearError = 0, treeError = 0, knnError = 0, rfError = 0;
        
        for (int i = 0; i < xTrainingData.size(); i++) {
            double x = xTrainingData.get(i);
            double actual = yTrainingData.get(i);
            
            linearError += Math.pow(linearModel.predict(x) - actual, 2);
            treeError += Math.pow(treeModel.predict(x) - actual, 2);
            knnError += Math.pow(knnModel.predict(x) - actual, 2);
            rfError += Math.pow(randomForestModel.predict(x) - actual, 2);
        }
        
        int n = xTrainingData.size();
        accuracies.put("linear", 1.0 / (1.0 + Math.sqrt(linearError / n)));
        accuracies.put("tree", 1.0 / (1.0 + Math.sqrt(treeError / n)));
        accuracies.put("knn", 1.0 / (1.0 + Math.sqrt(knnError / n)));
        accuracies.put("randomForest", 1.0 / (1.0 + Math.sqrt(rfError / n)));
        
        return accuracies;
    }

    /**
     * Adaptive weight adjustment based on model performance
     */
    public void adaptWeights() {
        Map<String, Double> accuracies = getModelAccuracies();
        if (accuracies.isEmpty()) return;
        
        double[] newWeights = new double[4];
        newWeights[0] = accuracies.get("linear");
        newWeights[1] = accuracies.get("tree");
        newWeights[2] = accuracies.get("knn");
        newWeights[3] = accuracies.get("randomForest");
        
        setModelWeights(newWeights);
    }

    /**
     * Get ensemble performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        if (xTrainingData.isEmpty()) return metrics;
        
        double mse = 0, mae = 0, rmse = 0;
        List<Double> predictions = new ArrayList<>();
        List<Double> residuals = new ArrayList<>();
        
        for (int i = 0; i < xTrainingData.size(); i++) {
            double pred = predict(xTrainingData.get(i));
            double actual = yTrainingData.get(i);
            double residual = actual - pred;
            
            predictions.add(pred);
            residuals.add(residual);
            
            mse += Math.pow(residual, 2);
            mae += Math.abs(residual);
        }
        
        int n = xTrainingData.size();
        mse /= n;
        mae /= n;
        rmse = Math.sqrt(mse);
        
        metrics.put("MSE", mse);
        metrics.put("RMSE", rmse);
        metrics.put("MAE", mae);
        
        // R-squared
        double yMean = yTrainingData.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double ssRes = residuals.stream().mapToDouble(r -> r * r).sum();
        double ssTot = yTrainingData.stream()
            .mapToDouble(y -> Math.pow(y - yMean, 2))
            .sum();
        double rSquared = 1 - (ssRes / (ssTot + 0.001));
        
        metrics.put("R-squared", rSquared);
        metrics.put("accuracy", linearModel.getRSquared());
        
        return metrics;
    }

    /**
     * Forecast multiple steps ahead
     */
    public double[] forecast(int steps) {
        double[] forecast = new double[steps];
        double lastX = xTrainingData.isEmpty() ? 0 : xTrainingData.get(xTrainingData.size() - 1);
        
        for (int i = 0; i < steps; i++) {
            forecast[i] = predict(lastX + i + 1);
        }
        
        return forecast;
    }

    /**
     * Get prediction with confidence bounds
     */
    public Map<String, Object> predictWithConfidence(double x, double confidenceLevel) {
        Map<String, Object> result = new HashMap<>();
        double prediction = predict(x);
        
        // Get variance from all models
        double variance = 0;
        int n = xTrainingData.size();
        
        for (int i = 0; i < n; i++) {
            double residual = yTrainingData.get(i) - predict(xTrainingData.get(i));
            variance += Math.pow(residual, 2);
        }
        variance /= Math.max(1, n - 1);
        
        double stdError = Math.sqrt(variance);
        double zScore = confidenceLevel == 0.95 ? 1.96 : (confidenceLevel == 0.99 ? 2.576 : 1.645);
        double margin = zScore * stdError;
        
        result.put("prediction", prediction);
        result.put("lowerBound", prediction - margin);
        result.put("upperBound", prediction + margin);
        result.put("margin", margin);
        result.put("confidence", confidenceLevel);
        
        return result;
    }
}

/**
 * Decision Tree-like Model
 */
class DecisionTreeModel {
    private List<Double> xData;
    private List<Double> yData;
    private int maxDepth = 5;

    public DecisionTreeModel() {
        this.xData = new ArrayList<>();
        this.yData = new ArrayList<>();
    }

    public void train(List<Double> x, List<Double> y) {
        this.xData = new ArrayList<>(x);
        this.yData = new ArrayList<>(y);
    }
    
    public double predict(double x) {
        if (xData == null || xData.isEmpty()) return 0;
        
        double closestX = xData.get(0);
        double closestY = yData.get(0);
        double minDist = Math.abs(x - closestX);
        
        for (int i = 1; i < xData.size(); i++) {
            double dist = Math.abs(x - xData.get(i));
            if (dist < minDist) {
                minDist = dist;
                closestX = xData.get(i);
                closestY = yData.get(i);
            }
        }
        
        return closestY;
    }
}

/**
 * K-Nearest Neighbors Model
 */
class KNearestNeighborsModel {
    private List<Double> xData;
    private List<Double> yData;
    private int k;
    
    public KNearestNeighborsModel(int k) {
        this.k = k;
        this.xData = new ArrayList<>();
        this.yData = new ArrayList<>();
    }
    
    public void train(List<Double> x, List<Double> y) {
        this.xData = new ArrayList<>(x);
        this.yData = new ArrayList<>(y);
    }
    
    public double predict(double x) {
        if (xData == null || xData.isEmpty()) return 0;
        
        List<Double> distances = new ArrayList<>();
        for (Double xi : xData) {
            distances.add(Math.abs(x - xi));
        }
        
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < distances.size(); i++) {
            indices.add(i);
        }
        
        indices.sort(Comparator.comparingDouble(distances::get));
        
        double sum = 0;
        int count = Math.min(k, indices.size());
        for (int i = 0; i < count; i++) {
            sum += yData.get(indices.get(i));
        }
        
        return sum / count;
    }
}

/**
 * Random Forest-like Model
 */
class RandomForestModel {
    private List<DecisionTreeModel> trees = new ArrayList<>();
    private int numTrees;
    private int maxDepth;
    
    public RandomForestModel(int numTrees, int maxDepth) {
        this.numTrees = numTrees;
        this.maxDepth = maxDepth;
    }
    
    public void train(List<Double> x, List<Double> y) {
        trees.clear();
        Random random = new Random();
        
        for (int t = 0; t < numTrees; t++) {
            // Bootstrap sampling
            List<Double> bootstrapX = new ArrayList<>();
            List<Double> bootstrapY = new ArrayList<>();
            
            for (int i = 0; i < x.size(); i++) {
                int idx = random.nextInt(x.size());
                bootstrapX.add(x.get(idx));
                bootstrapY.add(y.get(idx));
            }
            
            DecisionTreeModel tree = new DecisionTreeModel();
            tree.train(bootstrapX, bootstrapY);
            trees.add(tree);
        }
    }
    
    public double predict(double x) {
        if (trees.isEmpty()) return 0;
        
        double sum = 0;
        for (DecisionTreeModel tree : trees) {
            sum += tree.predict(x);
        }
        
        return sum / trees.size();
    }
}
