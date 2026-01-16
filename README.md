# Adaptive Energy Optimization System (AEOS)
## Enhanced with Adaptive ML-Based Self-Learning Features

### 🎯 Quick Start

**No Installation - Just Download & Run!**

```
✅ Desktop Application:  Double-click AEOS.bat → Application starts
✅ Web Version:          mvn spring-boot:run  → Browser access
✅ Professional EXE:     Use bat2exe to create AEOS.exe
```

📖 **Quick Links:**
![Desktop App](https://img.shields.io/badge/Desktop-Java%20Swing-blue?style=for-the-badge)
![Web App](https://img.shields.io/badge/Web-Spring%20Boot-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Layered-important?style=for-the-badge)

---

### Project Overview

A comprehensive Java-based energy monitoring and error correction system featuring advanced ML capabilities, real-time analytics, and adaptive learning for intelligent bit error correction. Now available as both a **professional desktop application** and **web-based application**!

### System Architecture

```
Smart Energy System
├── Model Layer (model/)
│   ├── EnergyRecord.java
│   └── Machine.java
├── Service Layer (service/)
│   ├── EnergyAnalyzer.java
│   └── BitCorrectionEngine.java
├── Utility Layer (util/)
│   ├── DataLoader.java
│   ├── ChartGenerator.java
│   └── ReportGenerator.java
├── ML Layer (ml/)
│   ├── LinearRegressionModel.java
│   ├── WekaIntegration.java
│   └── AdaptiveMLBitCorrection.java ⭐ NEW
└── GUI Layer (gui/)
    ├── EnergyApp.java
    └── EnergyController.java
```

### Core Features

#### 1. **CSV File Upload & Data Management** ✅
- Load energy consumption data from CSV files
- Multi-machine data aggregation
- Data validation and error handling
- Real-time table updates

#### 2. **Energy Visualization** ✅
- Interactive line charts for energy trends
- Multi-machine comparison dashboards
- Time-series data analysis
- Zoom and pan capabilities

#### 3. **Advanced ML Analytics** ✅
- K-means clustering for machine grouping
- Anomaly detection with statistical methods
- Time-series forecasting (exponential smoothing)
- Feature importance analysis
- Energy usage categorization

#### 4. **Bit Error Correction** ✅
- Hamming code-based error detection
- Bit-level error correction
- Data integrity validation
- Parity bit calculation

#### 5. **Adaptive ML-Based Bit Correction** ⭐ NEW
- **Self-Learning System**: Learns from observed error patterns
- **Machine-Specific Profiles**: Unique correction factors per machine
- **Reliability Scoring**: Quantifies machine operational health
- **Predictive Analytics**: Forecasts error patterns
- **System Recommendations**: Maintenance suggestions based on error rates
- **Model Export**: JSON export for external analysis
- **Real-time Adaptation**: Correction factors improve with more data

#### 6. **Report Generation** ✅
- HTML reports with styled tables
- JSON exports for data interchange
- CSV summaries with statistics
- Machine-specific analytics

### NEW: Adaptive ML Features

#### Key Capabilities

**Error Pattern Analysis**
- Detects systematic errors vs. random bit flips
- Calculates per-machine error signatures
- Tracks error trends over time
- Provides early warning for degradation

**Adaptive Correction**
- Dynamic correction factors learned from data
- Machine-specific tuning
- Confidence-based recommendations
- Sanity checks to prevent overcorrection

**Reliability Metrics**
- Machine reliability scoring (0-100%)
- Model confidence levels
- Sample-based statistics
- Trend analysis

**System Recommendations**
- Automatic maintenance suggestions
- Risk-based prioritization
- Data-driven decision support
- Integration with maintenance records

**Analytics Dashboard**
- Machine Performance Tab: Statistical metrics
- Correction Patterns Tab: Learned behaviors
- Model Export Tab: JSON model export

#### Integration with Existing Features

**With Energy Analysis**
- Uses corrected values for calculations
- Improved statistical accuracy
- Better trend detection

**With ML Algorithms**
- Cleaner input data for clustering
- Better anomaly detection
- Improved forecasting

**With Charts & Reports**
- Reliability metrics in visualizations
- Correction information in exports
- Machine profiles in reports

### Usage Workflow

#### Basic Workflow
```
1. Load Data
   → Use "Load CSV" from menu
   → Select energy records file
   
2. Train Adaptive Model
   → Go to "Bit Correction" tab
   → Click "Train Adaptive ML"
   → Review recommendations
   
3. Apply Corrections
   → Click "Apply Corrections"
   → Corrections use learned factors
   
4. View Results
   → Check correction table
   → View ML Analytics dashboard
   → Export model if needed
```

#### Advanced Workflow
```
1. Load historical data
2. Train initial model
3. Apply corrections
4. Monitor reliability metrics
5. Retrain monthly with new data
6. Export model for backup
7. Use recommendations for maintenance
```

### Installation & Setup

#### Prerequisites
- Java 21 or higher
- Maven (for building)
- 4GB RAM minimum

#### Build
```bash
cd "Smart Energy Consumption & Bit Correction System"
mvn clean compile
```

#### Run
```bash
mvn exec:java -Dexec.mainClass="gui.EnergyApp"
# OR
java -cp bin gui.EnergyApp
```

### Configuration

#### System Thresholds (in AdaptiveMLBitCorrection.java)
```java
globalErrorThreshold = 0.05;        // 5% error threshold
minSamplesForAdaptation = 10;       // Min samples for model
```

#### Correction Levels
- High error (>30%): 0-15% correction
- Medium error (15-30%): 0-4.5% correction
- Low error (<15%): 0-1.5% correction

### Data Formats

#### Input: CSV Format
```csv
recordId,machineId,energyConsumption,timestamp
R001,M001,45.5,2025-12-31T10:00:00
R002,M001,52.3,2025-12-31T11:00:00
```

#### Output: Model Export (JSON)
```json
{
  "machineProfiles": {
    "M001": {
      "errorRate": 0.0450,
      "correctionFactor": 1.0235,
      "reliability": 0.9550
    }
  },
  "globalThreshold": 0.0500,
  "samplesProcessed": 100
}
```

### Key Classes

#### AdaptiveMLBitCorrection.java
- Core ML correction engine
- Machine profile management
- Recommendation engine
- Model export/import

#### EnergyController.java
- GUI event handling
- UI component management
- Feature integration
- Data flow orchestration

#### BitCorrectionEngine.java
- Hamming code implementation
- Error detection logic
- Data integrity validation

#### WekaIntegration.java
- K-means clustering
- Anomaly detection
- Time-series forecasting
- Feature analysis

### Performance Metrics

#### Training
- Time: O(n) where n = number of records
- Space: O(m) where m = number of machines
- Min samples per machine: 10
- Recommended: 100+ per machine

#### Runtime
- Correction time: O(1) per record
- Model update: Negligible
- UI response: < 100ms for typical datasets

### Troubleshooting

#### High Error Rates
- **Possible Cause**: Machine hardware degradation
- **Action**: Inspect physical components, check maintenance logs

#### Low Confidence
- **Possible Cause**: Insufficient training data
- **Action**: Load more records, retrain model

#### Inconsistent Corrections
- **Possible Cause**: Noisy data
- **Action**: Validate source data, check for duplicates

#### Missing Recommendations
- **Possible Cause**: Model not trained
- **Action**: Click "Train Adaptive ML" first

### Testing Data

Sample energy records are provided. Each record contains:
- Machine ID (M001, M002, M003)
- Energy consumption (kWh)
- Timestamp
- Error bits (detected)

### Development Notes

#### Recent Changes
- ✅ Added AdaptiveMLBitCorrection.java
- ✅ Enhanced EnergyController with ML UI
- ✅ Fixed Graphics2D rendering hints
- ✅ Fixed String format specifiers
- ✅ Added ML analytics dashboard

#### Known Limitations
- Single-threaded UI (no async training)
- In-memory model (no persistence)
- No clustering of correction factors
- Limited to 64-bit IEEE doubles

#### Future Enhancements
- Multi-threaded training
- Model persistence (database)
- Time-series decomposition
- Seasonal adjustment factors
- IoT sensor integration
- Real-time streaming support

### File Structure

```
Smart Energy Consumption & Bit Correction System/
├── src/
│   ├── app/
│   │   └── EnergyApp.java (main)
│   ├── gui/
│   │   └── EnergyController.java
│   ├── service/
│   │   ├── EnergyAnalyzer.java
│   │   └── BitCorrectionEngine.java
│   ├── ml/
│   │   ├── LinearRegressionModel.java
│   │   ├── WekaIntegration.java
│   │   └── AdaptiveMLBitCorrection.java ⭐
│   ├── model/
│   │   ├── EnergyRecord.java
│   │   └── Machine.java
│   └── util/
│       ├── DataLoader.java
│       ├── ChartGenerator.java
│       └── ReportGenerator.java
├── bin/ (compiled classes)
├── pom.xml (Maven config)
├── .classpath (Eclipse config)
├── .project (Eclipse config)
└── Documentation/
    ├── ADAPTIVE_ML_FEATURES.md
    ├── ADAPTIVE_ML_QUICK_START.md
    └── README.md (this file)
```

### Dependencies

![Java](https://img.shields.io/badge/Java-21%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Machine Learning](https://img.shields.io/badge/Machine%20Learning-Adaptive-blueviolet?style=for-the-badge&logo=tensorflow&logoColor=white)
![Energy Systems](https://img.shields.io/badge/Energy-Optimization-green?style=for-the-badge&logo=powerbi&logoColor=white)
![Production Ready](https://img.shields.io/badge/Status-Production%20Ready-success?style=for-the-badge)


### Compilation Status

✅ All components compile successfully
✅ No runtime errors
✅ All features integrated
✅ UI fully responsive

### Testing Checklist

- [x] CSV data loading
- [x] Energy analysis calculations
- [x] Chart generation
- [x] Bit correction
- [x] ML model training
- [x] Analytics dashboard
- [x] Report generation
- [x] Adaptive ML correction
- [x] Model export/import
- [x] Recommendation engine

### Support & Documentation

**Documentation Files:**
- `ADAPTIVE_ML_FEATURES.md` - Detailed feature guide
- `ADAPTIVE_ML_QUICK_START.md` - Quick reference
- `README.md` - This file

**Key Sections:**
- Architecture overview
- Feature descriptions
- Usage instructions
- Troubleshooting guide
- Development notes

### License

This project is provided as-is for educational and industrial use.

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Dec 2025 | Initial release with 5 core features |
| 1.1 | Dec 2025 | Added Adaptive ML bit correction ⭐ |

### Status

**Project Status**: ✅ **PRODUCTION READY**

All features implemented, tested, and deployed.

---

**Last Updated**: December 31, 2025  
**Maintained By**: RigVisionX Technology  
**Email**: mihanwitschi@gmail.com
**Mobile**: +60 1169491842
**LinkedIn**: RigVisionX Technology | https://www.linkedin.com/in/rigvisionx-technology-7085943a4/
**2n LinkedIn**: Witschi Mihan | https://www.linkedin.com/in/witschi-mihan-14a347312/
