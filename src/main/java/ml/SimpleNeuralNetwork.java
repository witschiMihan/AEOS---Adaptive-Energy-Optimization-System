package ml;

import model.EnergyRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple Neural Network for Energy Data
 * Implements a feedforward neural network with backpropagation
 */
public class SimpleNeuralNetwork {

    private static class Neuron {
        double[] weights;
        double bias;
        double output;
        double delta;
        
        Neuron(int inputSize) {
            weights = new double[inputSize];
            Random random = new Random();
            for (int i = 0; i < inputSize; i++) {
                weights[i] = random.nextGaussian() * 0.01; // Xavier initialization
            }
            bias = random.nextGaussian() * 0.01;
        }
    }

    private List<Neuron>[] layers;
    private int[] layerSizes;
    private double learningRate = 0.01;
    private double momentum = 0.9;
    private double[][][] prevWeightDeltas;

    /**
     * Initialize neural network with specified architecture
     */
    @SuppressWarnings("unchecked")
    public SimpleNeuralNetwork(int... layerSizes) {
        this.layerSizes = layerSizes;
        this.layers = new List[layerSizes.length];
        this.prevWeightDeltas = new double[layerSizes.length][][];

        // Initialize layers
        for (int l = 0; l < layerSizes.length; l++) {
            layers[l] = new ArrayList<>();
            int inputSize = (l == 0) ? layerSizes[0] : layerSizes[l - 1];

            for (int n = 0; n < layerSizes[l]; n++) {
                layers[l].add(new Neuron(inputSize));
            }

            prevWeightDeltas[l] = new double[layerSizes[l]][];
            for (int n = 0; n < layerSizes[l]; n++) {
                prevWeightDeltas[l][n] = new double[inputSize];
            }
        }
    }

    /**
     * Activation function (ReLU)
     */
    private double activate(double x) {
        return Math.max(0, x); // ReLU
    }

    /**
     * Activation derivative (ReLU)
     */
    private double activateDerivative(double x) {
        return x > 0 ? 1 : 0;
    }

    /**
     * Forward pass
     */
    public double predict(double[] input) {
        double[] current = input;
        
        // Propagate through all layers except output
        for (int l = 1; l < layers.length; l++) {
            double[] next = new double[layers[l].size()];
            
            for (int n = 0; n < layers[l].size(); n++) {
                Neuron neuron = layers[l].get(n);
                double sum = neuron.bias;
                
                for (int i = 0; i < current.length; i++) {
                    sum += current[i] * neuron.weights[i];
                }
                
                neuron.output = activate(sum);
                next[n] = neuron.output;
            }
            
            current = next;
        }
        
        // Output layer (linear)
        if (layers.length > 0 && layers[layers.length - 1].size() > 0) {
            Neuron outputNeuron = layers[layers.length - 1].get(0);
            double sum = outputNeuron.bias;
            for (int i = 0; i < current.length; i++) {
                sum += current[i] * outputNeuron.weights[i];
            }
            return sum;
        }
        
        return 0;
    }

    /**
     * Train on energy records
     */
    public void train(List<EnergyRecord> records, int epochs) {
        if (records.isEmpty()) return;
        
        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalError = 0;
            
            for (EnergyRecord record : records) {
                double input = record.getEnergyConsumption();
                double target = record.getEnergyConsumption();
                
                // Forward pass
                double prediction = predict(new double[]{input});
                double error = target - prediction;
                totalError += error * error;
                
                // Backward pass
                backpropagate(new double[]{input}, error);
            }
            
            if (epoch % 100 == 0) {
                System.out.println("Epoch " + epoch + " - MSE: " + (totalError / records.size()));
            }
        }
    }

    /**
     * Backpropagation
     */
    private void backpropagate(double[] input, double error) {
        // Calculate output layer delta
        List<Neuron> outputLayer = layers[layers.length - 1];
        if (!outputLayer.isEmpty()) {
            Neuron outputNeuron = outputLayer.get(0);
            outputNeuron.delta = error; // Linear output
            
            // Update output neuron weights
            updateNeuronWeights(outputNeuron, input, 0);
        }
        
        // Backpropagate through hidden layers
        double[] nextDeltas = new double[input.length];
        
        for (int l = layers.length - 1; l > 0; l--) {
            double[] prevDeltas = new double[layers[l - 1].size()];
            
            for (int n = 0; n < layers[l].size(); n++) {
                Neuron neuron = layers[l].get(n);
                
                for (int pn = 0; pn < layers[l - 1].size(); pn++) {
                    Neuron prevNeuron = layers[l - 1].get(pn);
                    prevDeltas[pn] += neuron.delta * neuron.weights[pn] 
                        * activateDerivative(prevNeuron.output);
                }
            }
            
            // Update weights in previous layer
            for (int n = 0; n < layers[l - 1].size(); n++) {
                Neuron neuron = layers[l - 1].get(n);
                neuron.delta = prevDeltas[n];
                updateNeuronWeights(neuron, input, l - 1);
            }
        }
    }

    /**
     * Update neuron weights using gradient descent with momentum
     */
    private void updateNeuronWeights(Neuron neuron, double[] input, int layerIndex) {
        // Find neuron index in layer
        int neuronIndex = -1;
        for (int n = 0; n < layers[layerIndex].size(); n++) {
            if (layers[layerIndex].get(n) == neuron) {
                neuronIndex = n;
                break;
            }
        }

        if (neuronIndex == -1) return;

        for (int i = 0; i < neuron.weights.length; i++) {
            double input_i = (layerIndex == 0) ? input[i] : layers[layerIndex - 1].get(i).output;
            double weightDelta = learningRate * neuron.delta * input_i;

            // Add momentum
            weightDelta += momentum * prevWeightDeltas[layerIndex][neuronIndex][i];

            neuron.weights[i] += weightDelta;
            prevWeightDeltas[layerIndex][neuronIndex][i] = weightDelta;
        }

        neuron.bias += learningRate * neuron.delta;
    }

    /**
     * Set learning rate
     */
    public void setLearningRate(double rate) {
        this.learningRate = rate;
    }

    /**
     * Set momentum
     */
    public void setMomentum(double m) {
        this.momentum = m;
    }

    /**
     * Get network weights (for persistence)
     */
    public Map<String, Object> getWeights() {
        Map<String, Object> weights = new HashMap<>();
        
        for (int l = 0; l < layers.length; l++) {
            List<Map<String, Object>> layerWeights = new ArrayList<>();
            
            for (Neuron neuron : layers[l]) {
                Map<String, Object> neuronWeights = new HashMap<>();
                neuronWeights.put("weights", neuron.weights);
                neuronWeights.put("bias", neuron.bias);
                layerWeights.add(neuronWeights);
            }
            
            weights.put("layer_" + l, layerWeights);
        }
        
        return weights;
    }

    /**
     * Load network weights
     */
    public void loadWeights(Map<String, Object> weights) {
        for (int l = 0; l < layers.length; l++) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> layerWeights = 
                (List<Map<String, Object>>) weights.get("layer_" + l);
            
            for (int n = 0; n < layers[l].size(); n++) {
                Map<String, Object> neuronWeights = layerWeights.get(n);
                Neuron neuron = layers[l].get(n);
                
                neuron.weights = (double[]) neuronWeights.get("weights");
                neuron.bias = (double) neuronWeights.get("bias");
            }
        }
    }

    /**
     * Predict multiple values
     */
    public double[] predictBatch(double[] inputs) {
        double[] predictions = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            predictions[i] = predict(new double[]{inputs[i]});
        }
        return predictions;
    }
}
