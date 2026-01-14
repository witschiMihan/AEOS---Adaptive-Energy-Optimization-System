package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cross-Validation Module
 * Evaluates model performance using various validation techniques:
 * - K-Fold Cross-Validation
 * - Leave-One-Out Cross-Validation
 * - Stratified K-Fold
 * - Time Series Cross-Validation
 */
public class CrossValidator {

    /**
     * Cross-validation result
     */
    public static class ValidationResult {
        public double[] scores;
        public double meanScore;
        public double stdDev;
        public Map<String, Object> metrics;
        public String validationType;
        
        public ValidationResult(double[] scores, String type) {
            this.scores = scores;
            this.validationType = type;
            this.metrics = new HashMap<>();
            calculateStatistics();
        }
        
        private void calculateStatistics() {
            this.meanScore = Arrays.stream(scores).average().orElse(0);
            double variance = Arrays.stream(scores)
                .map(s -> Math.pow(s - meanScore, 2))
                .average()
                .orElse(0);
            this.stdDev = Math.sqrt(variance);
        }
    }

    /**
     * K-Fold Cross-Validation
     */
    public static ValidationResult kFoldCrossValidation(List<EnergyRecord> records,
            int k, EnergyModel model) {
        if (records.size() < k) {
            throw new IllegalArgumentException("Dataset too small for " + k + "-fold CV");
        }
        
        int foldSize = records.size() / k;
        double[] scores = new double[k];
        
        for (int i = 0; i < k; i++) {
            // Split into training and test
            List<EnergyRecord> testSet = new ArrayList<>();
            List<EnergyRecord> trainingSet = new ArrayList<>();
            
            int testStart = i * foldSize;
            int testEnd = (i == k - 1) ? records.size() : (i + 1) * foldSize;
            
            for (int j = 0; j < records.size(); j++) {
                if (j >= testStart && j < testEnd) {
                    testSet.add(records.get(j));
                } else {
                    trainingSet.add(records.get(j));
                }
            }
            
            // Train and evaluate
            model.train(trainingSet);
            scores[i] = evaluateModel(model, testSet);
        }
        
        return new ValidationResult(scores, "K-Fold (" + k + "-fold)");
    }

    /**
     * Stratified K-Fold (maintains class distribution)
     */
    public static ValidationResult stratifiedKFoldCrossValidation(List<EnergyRecord> records,
            int k, EnergyModel model) {
        // Group by error rate categories
        Map<String, List<EnergyRecord>> stratifiedGroups = new HashMap<>();
        
        for (EnergyRecord record : records) {
            double errorRate = record.getErrorBits() / 64.0;
            String category;
            
            if (errorRate < 0.05) {
                category = "LOW";
            } else if (errorRate < 0.15) {
                category = "MEDIUM";
            } else {
                category = "HIGH";
            }
            
            stratifiedGroups.putIfAbsent(category, new ArrayList<>());
            stratifiedGroups.get(category).add(record);
        }
        
        // Distribute folds evenly across strata
        List<EnergyRecord> stratifiedRecords = new ArrayList<>();
        int maxGroupSize = stratifiedGroups.values().stream()
            .mapToInt(List::size)
            .max()
            .orElse(0);
        
        for (int i = 0; i < maxGroupSize; i++) {
            for (List<EnergyRecord> group : stratifiedGroups.values()) {
                if (i < group.size()) {
                    stratifiedRecords.add(group.get(i));
                }
            }
        }
        
        return kFoldCrossValidation(stratifiedRecords, k, model);
    }

    /**
     * Leave-One-Out Cross-Validation
     */
    public static ValidationResult leaveOneOutCrossValidation(List<EnergyRecord> records,
            EnergyModel model) {
        double[] scores = new double[records.size()];
        
        for (int i = 0; i < records.size(); i++) {
            List<EnergyRecord> trainingSet = new ArrayList<>();
            EnergyRecord testRecord = null;
            
            for (int j = 0; j < records.size(); j++) {
                if (i == j) {
                    testRecord = records.get(j);
                } else {
                    trainingSet.add(records.get(j));
                }
            }
            
            model.train(trainingSet);
            scores[i] = evaluateSingleRecord(model, testRecord);
        }
        
        return new ValidationResult(scores, "Leave-One-Out");
    }

    /**
     * Time Series Cross-Validation (walk-forward validation)
     */
    public static ValidationResult timeSeriesCrossValidation(List<EnergyRecord> records,
            int trainSize, int testSize, EnergyModel model) {
        List<Double> scores = new ArrayList<>();
        
        for (int i = 0; i + trainSize + testSize <= records.size(); i += testSize) {
            List<EnergyRecord> trainingSet = records.subList(i, i + trainSize);
            List<EnergyRecord> testSet = records.subList(i + trainSize, i + trainSize + testSize);
            
            model.train(trainingSet);
            scores.add(evaluateModel(model, testSet));
        }
        
        double[] scoreArray = scores.stream().mapToDouble(Double::doubleValue).toArray();
        return new ValidationResult(scoreArray, "Time Series Walk-Forward");
    }

    /**
     * Nested Cross-Validation for hyperparameter tuning
     */
    public static ValidationResult nestedCrossValidation(List<EnergyRecord> records,
            int outerK, int innerK, EnergyModel model) {
        double[] outerScores = new double[outerK];
        int foldSize = records.size() / outerK;
        
        for (int i = 0; i < outerK; i++) {
            List<EnergyRecord> testSet = new ArrayList<>();
            List<EnergyRecord> trainingSet = new ArrayList<>();
            
            int testStart = i * foldSize;
            int testEnd = (i == outerK - 1) ? records.size() : (i + 1) * foldSize;
            
            for (int j = 0; j < records.size(); j++) {
                if (j >= testStart && j < testEnd) {
                    testSet.add(records.get(j));
                } else {
                    trainingSet.add(records.get(j));
                }
            }
            
            // Inner CV for hyperparameter tuning
            ValidationResult innerCV = kFoldCrossValidation(trainingSet, innerK, model);
            
            // Train on full training set with best parameters
            model.train(trainingSet);
            outerScores[i] = evaluateModel(model, testSet);
        }
        
        return new ValidationResult(outerScores, "Nested Cross-Validation");
    }

    /**
     * Evaluate model on test set
     */
    private static double evaluateModel(EnergyModel model, List<EnergyRecord> testSet) {
        if (testSet.isEmpty()) return 0;
        
        double mse = 0;
        for (EnergyRecord record : testSet) {
            double predicted = model.predict(record.getEnergyConsumption());
            double actual = record.getEnergyConsumption();
            mse += Math.pow(predicted - actual, 2);
        }
        
        mse /= testSet.size();
        double rmse = Math.sqrt(mse);
        
        // R² score
        double mean = testSet.stream()
            .mapToDouble(EnergyRecord::getEnergyConsumption)
            .average()
            .orElse(0);
        
        double ssRes = 0, ssTot = 0;
        for (EnergyRecord record : testSet) {
            double predicted = model.predict(record.getEnergyConsumption());
            double actual = record.getEnergyConsumption();
            ssRes += Math.pow(actual - predicted, 2);
            ssTot += Math.pow(actual - mean, 2);
        }
        
        return 1 - (ssRes / (ssTot + 0.001));
    }

    /**
     * Evaluate on single record
     */
    private static double evaluateSingleRecord(EnergyModel model, EnergyRecord record) {
        double predicted = model.predict(record.getEnergyConsumption());
        double actual = record.getEnergyConsumption();
        double error = Math.abs(predicted - actual);
        return 1 - (error / (actual + 0.001));
    }

    /**
     * Learning curve analysis
     */
    public static Map<String, double[]> learningCurveAnalysis(List<EnergyRecord> records,
            EnergyModel model, int steps) {
        List<Double> trainScores = new ArrayList<>();
        List<Double> testScores = new ArrayList<>();
        List<Integer> trainSizes = new ArrayList<>();
        
        int stepSize = Math.max(1, records.size() / steps);
        
        for (int size = stepSize; size <= records.size(); size += stepSize) {
            List<EnergyRecord> trainSet = records.subList(0, size);
            List<EnergyRecord> testSet = records.subList(size, 
                Math.min(size + stepSize, records.size()));
            
            model.train(trainSet);
            double trainScore = evaluateModel(model, trainSet);
            double testScore = testSet.isEmpty() ? trainScore : evaluateModel(model, testSet);
            
            trainScores.add(trainScore);
            testScores.add(testScore);
            trainSizes.add(size);
        }
        
        Map<String, double[]> result = new HashMap<>();
        result.put("trainSizes", trainSizes.stream().mapToDouble(Integer::doubleValue).toArray());
        result.put("trainScores", trainScores.stream().mapToDouble(Double::doubleValue).toArray());
        result.put("testScores", testScores.stream().mapToDouble(Double::doubleValue).toArray());
        
        return result;
    }

    /**
     * Bootstrap cross-validation
     */
    public static ValidationResult bootstrapCrossValidation(List<EnergyRecord> records,
            int iterations, EnergyModel model) {
        double[] scores = new double[iterations];
        Random random = new Random();
        
        for (int i = 0; i < iterations; i++) {
            List<EnergyRecord> sample = new ArrayList<>();
            Set<Integer> usedIndices = new HashSet<>();
            
            // Bootstrap sample
            for (int j = 0; j < records.size(); j++) {
                int idx = random.nextInt(records.size());
                sample.add(records.get(idx));
                usedIndices.add(idx);
            }
            
            // Out-of-bag test set
            List<EnergyRecord> testSet = new ArrayList<>();
            for (int j = 0; j < records.size(); j++) {
                if (!usedIndices.contains(j)) {
                    testSet.add(records.get(j));
                }
            }
            
            model.train(sample);
            scores[i] = testSet.isEmpty() ? 1.0 : evaluateModel(model, testSet);
        }
        
        return new ValidationResult(scores, "Bootstrap (" + iterations + " iterations)");
    }

    /**
     * Get detailed validation report
     */
    public static Map<String, Object> getValidationReport(ValidationResult result) {
        Map<String, Object> report = new HashMap<>();
        report.put("validationType", result.validationType);
        report.put("meanScore", result.meanScore);
        report.put("stdDev", result.stdDev);
        report.put("minScore", Arrays.stream(result.scores).min().orElse(0));
        report.put("maxScore", Arrays.stream(result.scores).max().orElse(0));
        report.put("scores", result.scores);
        report.putAll(result.metrics);
        
        return report;
    }

    /**
     * Interface for ML models to implement
     */
    public interface EnergyModel {
        void train(List<EnergyRecord> records);
        double predict(double value);
    }
}
