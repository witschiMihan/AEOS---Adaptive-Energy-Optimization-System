from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import numpy as np
import json
import pickle
import os
from datetime import datetime

app = FastAPI(
    title="AEOS ML Server",
    description="Machine Learning service for AEOS energy monitoring",
    version="1.0.0"
)

# Load model if exists
MODEL_PATH = "model.pkl"
model = None

if os.path.exists(MODEL_PATH):
    try:
        with open(MODEL_PATH, 'rb') as f:
            model = pickle.load(f)
        print("✅ Model loaded successfully")
    except Exception as e:
        print(f"⚠️ Could not load model: {e}")
else:
    print("⚠️ model.pkl not found - using dummy predictions")

# Request/Response Models
class PredictionRequest(BaseModel):
    """Request for energy consumption prediction"""
    machine_id: str
    historical_data: List[float]
    future_periods: int = 24

class PredictionResponse(BaseModel):
    """Response with predictions"""
    machine_id: str
    predictions: List[float]
    confidence: float
    timestamp: str

class AnomalyRequest(BaseModel):
    """Request for anomaly detection"""
    machine_id: str
    data: List[float]
    threshold: float = 2.0

class AnomalyResponse(BaseModel):
    """Response with anomaly detection results"""
    machine_id: str
    anomalies: List[int]
    anomaly_scores: List[float]
    timestamp: str

# Health Check
@app.get("/health")
async def health_check():
    """Check if ML server is healthy"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "model_loaded": model is not None
    }

# Predictions
@app.post("/predict", response_model=PredictionResponse)
async def predict_consumption(request: PredictionRequest):
    """
    Predict future energy consumption
    """
    try:
        # Convert to numpy array
        data = np.array(request.historical_data)
        
        if len(data) < 2:
            raise HTTPException(status_code=400, detail="Need at least 2 historical data points")
        
        # Simple exponential smoothing prediction (if model not available)
        if model is None:
            # Fallback: exponential smoothing
            alpha = 0.3
            predictions = []
            last_value = data[-1]
            
            for i in range(request.future_periods):
                predicted = alpha * last_value + (1 - alpha) * np.mean(data)
                predictions.append(float(predicted))
                last_value = predicted
            
            confidence = 0.6  # Lower confidence for fallback
        else:
            # Use loaded model for predictions
            try:
                predictions = model.predict(data.reshape(1, -1)).flatten().tolist()[:request.future_periods]
                confidence = 0.9
            except:
                # Fallback if model prediction fails
                predictions = [float(np.mean(data))] * request.future_periods
                confidence = 0.5
        
        return PredictionResponse(
            machine_id=request.machine_id,
            predictions=predictions,
            confidence=confidence,
            timestamp=datetime.now().isoformat()
        )
    
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

# Anomaly Detection
@app.post("/anomalies", response_model=AnomalyResponse)
async def detect_anomalies(request: AnomalyRequest):
    """
    Detect anomalies in energy data using statistical methods
    """
    try:
        data = np.array(request.data)
        
        if len(data) < 3:
            raise HTTPException(status_code=400, detail="Need at least 3 data points")
        
        # Z-score anomaly detection
        mean = np.mean(data)
        std = np.std(data)
        
        if std == 0:
            z_scores = np.zeros_like(data)
        else:
            z_scores = np.abs((data - mean) / std)
        
        # Identify anomalies
        anomaly_indices = np.where(z_scores > request.threshold)[0].tolist()
        anomaly_scores = z_scores.tolist()
        
        return AnomalyResponse(
            machine_id=request.machine_id,
            anomalies=anomaly_indices,
            anomaly_scores=anomaly_scores,
            timestamp=datetime.now().isoformat()
        )
    
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Anomaly detection error: {str(e)}")

# Statistics
@app.post("/statistics")
async def calculate_statistics(data: List[float]):
    """
    Calculate statistical metrics for energy data
    """
    try:
        arr = np.array(data)
        
        return {
            "count": len(arr),
            "mean": float(np.mean(arr)),
            "median": float(np.median(arr)),
            "std": float(np.std(arr)),
            "min": float(np.min(arr)),
            "max": float(np.max(arr)),
            "quartile_25": float(np.percentile(arr, 25)),
            "quartile_75": float(np.percentile(arr, 75)),
            "timestamp": datetime.now().isoformat()
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Statistics error: {str(e)}")

# Model Info
@app.get("/model-info")
async def get_model_info():
    """
    Get information about the current ML model
    """
    return {
        "model_loaded": model is not None,
        "model_path": MODEL_PATH,
        "version": "1.0.0",
        "timestamp": datetime.now().isoformat(),
        "capabilities": [
            "energy_prediction",
            "anomaly_detection",
            "statistics",
            "health_check"
        ]
    }

# Root endpoint
@app.get("/")
async def root():
    """
    Root endpoint with API information
    """
    return {
        "service": "AEOS ML Server",
        "version": "1.0.0",
        "status": "active",
        "endpoints": {
            "health": "/health",
            "predict": "/predict",
            "anomalies": "/anomalies",
            "statistics": "/statistics",
            "model_info": "/model-info",
            "docs": "/docs"
        },
        "docs": "http://localhost:8001/docs"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
