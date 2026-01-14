# AEOS ML Server

A FastAPI-based machine learning service for energy prediction and anomaly detection in the AEOS system.

## 🚀 Quick Start

### Local Development

```bash
# Install dependencies
pip install -r requirements.txt

# Run the server
python app.py

# API Documentation (interactive)
# Open: http://localhost:8001/docs
```

### Docker

```bash
# Build image
docker build -t aeos-ml-server .

# Run container
docker run -p 8001:8001 aeos-ml-server

# API Documentation
# Open: http://localhost:8001/docs
```

## 📊 API Endpoints

### 1. Health Check
```bash
GET /health
```
Response: Server status and model status

### 2. Energy Prediction
```bash
POST /predict
{
  "machine_id": "M-001",
  "historical_data": [45.5, 52.3, 48.9, 41.2],
  "future_periods": 24
}
```
Response: Predicted consumption values for next periods

### 3. Anomaly Detection
```bash
POST /anomalies
{
  "machine_id": "M-001",
  "data": [45.5, 52.3, 48.9, 150.2, 41.2],
  "threshold": 2.0
}
```
Response: Indices and scores of anomalous data points

### 4. Statistics
```bash
POST /statistics
[45.5, 52.3, 48.9, 41.2, 55.0]
```
Response: Mean, median, std, percentiles, etc.

### 5. Model Information
```bash
GET /model-info
```
Response: Current model status and capabilities

## 🔧 Configuration

### Port
Default: `8001`
Change in `app.py`:
```python
uvicorn.run(app, host="0.0.0.0", port=8001)
```

### Model File
Place your trained `model.pkl` in the same directory
If not found, server uses fallback statistical methods

## 🤖 Machine Learning Features

### Prediction
- Exponential smoothing for forecasting
- Support for custom trained models via `model.pkl`
- Configurable future periods

### Anomaly Detection
- Z-score based statistical detection
- Customizable threshold
- Returns anomaly indices and scores

### Statistics
- Mean, median, standard deviation
- Min, max, quartiles
- Percentile calculations

## 📦 Model Training

To use a custom ML model:

```python
import pickle
from sklearn.ensemble import RandomForestRegressor

# Train your model
model = RandomForestRegressor()
model.fit(X_train, y_train)

# Save it
with open('model.pkl', 'wb') as f:
    pickle.dump(model, f)
```

Then place `model.pkl` in the `ml-server/` directory.

## 🔌 Integration with Java Backend

The ML server can be called from the main AEOS application:

```java
// Example Java code
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(
    "http://localhost:8001/predict",
    predictionRequest,
    PredictionResponse.class
);
```

## 📚 Documentation

Auto-generated interactive API docs available at:
```
http://localhost:8001/docs
```

## 🆘 Troubleshooting

### Port already in use
```bash
# Linux/Mac
lsof -i :8001
kill -9 <PID>

# Windows
netstat -ano | findstr :8001
taskkill /PID <PID> /F
```

### Module not found
```bash
pip install -r requirements.txt --upgrade
```

### Model loading error
Remove `model.pkl` and server will use fallback methods:
```bash
rm model.pkl
python app.py
```

## 📊 Docker Compose Integration

Add to main `docker-compose.yml`:

```yaml
ml-server:
  build: ./ml-server
  container_name: aeos-ml-server
  ports:
    - "8001:8001"
  environment:
    - PYTHONUNBUFFERED=1
  restart: unless-stopped
  depends_on:
    - aeos
```

Then run:
```bash
docker-compose up -d
```

## 🎯 Use Cases

1. **Energy Forecasting** - Predict next 24-48 hours consumption
2. **Anomaly Detection** - Identify unusual consumption patterns
3. **Statistical Analysis** - Compute metrics for reporting
4. **Real-time Predictions** - Integrate with dashboard

## 📈 Performance

- Startup: < 5 seconds
- Prediction: < 100ms
- Anomaly Detection: < 50ms
- Statistics: < 10ms

## 🔐 Security

For production:
- Add API key authentication
- Enable HTTPS
- Add rate limiting
- Validate input data
- Use environment variables for secrets

## 📝 Version

- Version: 1.0.0
- Python: 3.11+
- FastAPI: 0.104+

## 🤝 Support

For issues or questions, refer to main project documentation.
