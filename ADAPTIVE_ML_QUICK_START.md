# Adaptive ML Features - Quick Reference

## New UI Elements in Bit Correction Tab

### Buttons Added:
1. **"Apply Corrections"** - Apply standard + adaptive ML corrections
2. **"Train Adaptive ML"** - Train model on current data
3. **"View ML Analytics"** - Open analytics dashboard

## How to Use

### Step 1: Load Energy Data
- Use "Load CSV" to import energy records
- Ensure data contains multiple machines for better results
- Minimum 10 samples per machine recommended

### Step 2: Train the Model
```
1. Click "Train Adaptive ML" button
2. Wait for analysis to complete
3. Review machine profiles and recommendations
4. Click OK to close results dialog
```

### Step 3: Apply Adaptive Corrections
```
1. Click "Apply Corrections" button
2. System applies personalized corrections per machine
3. Correction table updates with results
4. Corrections now include ML-learned factors
```

### Step 4: View Analytics
```
1. Click "View ML Analytics" button
2. Three tabs available:
   - Machine Performance: See metrics
   - Correction Patterns: Understand behaviors
   - Model Export: Get JSON export
```

## Key Metrics Explained

| Metric | Range | Meaning |
|--------|-------|---------|
| **Error Rate** | 0-1 | Fraction of errors detected (0% = perfect, 100% = all bad) |
| **Correction Factor** | 0.8-1.2 | Multiplier applied to energy values (1.0 = no correction) |
| **Reliability** | 0%-100% | Machine operational quality (100% = perfect) |
| **Confidence** | 0%-100% | Model certainty based on training samples |

## Machine Profiles

Each machine gets a unique profile including:
- **Error Behavior**: Systematic or random error patterns
- **Correction Strategy**: Adaptive factor based on machine type
- **Reliability Score**: Overall machine health indicator
- **Maintenance Recommendation**: Action items if needed

## Interpretation Guide

### Error Rates:
- **< 2.5%**: Excellent - No maintenance needed
- **2.5% - 5%**: Good - Monitor for trends
- **> 5%**: Poor - Consider maintenance

### Reliability Scores:
- **> 95%**: Excellent machine condition
- **90-95%**: Good condition, monitor
- **80-90%**: Fair condition, plan maintenance
- **< 80%**: Poor condition, urgent maintenance needed

### Correction Factors:
- **1.0000**: No systematic bias
- **1.0001-1.0050**: Minor correction (< 0.5%)
- **1.0050-1.0150**: Moderate correction (0.5%-1.5%)
- **> 1.0150**: Major correction (> 1.5%)

## Training Recommendations

| Scenario | Training Frequency |
|----------|-------------------|
| Initial setup | After 100+ records per machine |
| Stable operation | Monthly |
| Post-maintenance | Immediately after repair |
| Suspected issues | As needed |

## Export & Integration

### Export Format: JSON
```json
{
  "machineProfiles": {
    "MACHINE_ID": {
      "errorRate": 0.0000,
      "correctionFactor": 1.0000,
      "reliability": 1.0000
    }
  },
  "globalThreshold": 0.0500,
  "samplesProcessed": 100
}
```

### Use Cases:
- **Backup**: Save models for version control
- **Sharing**: Export for analysis team
- **Integration**: Use in external analytics
- **Compliance**: Documentation for audits

## Keyboard Shortcuts

- **None currently** - Use buttons in UI

## Tips & Tricks

1. **Better Results**: Train with diverse data (different times, machines)
2. **Confidence Boost**: Process more records before trusting model
3. **Machine Comparison**: Use Performance tab to compare all machines
4. **Pattern Spotting**: Correction Patterns tab shows which machines need attention
5. **Model Verification**: Export and review JSON to validate profiles

## Common Issues

### "No energy records to train on!"
- **Fix**: Load CSV data first using main menu

### Low confidence percentages
- **Fix**: Train model with more data samples (minimum 10 per machine)

### Recommendations seem unchanged
- **Fix**: Ensure data is actually different from previous training

### Model not improving
- **Fix**: Check if machines need physical maintenance

## Advanced Features

- **Reset Machine**: Use `adaptiveMLCorrection.resetMachineAdaptation(machineId)`
- **Export Model**: JSON available in Model Export tab
- **Custom Thresholds**: Modify in AdaptiveMLBitCorrection source code
- **Machine Profiles**: View in Machine Performance tab

## Integration Points

### With Other Features:
- **Charts**: Use corrected energy values
- **Analysis**: Include ML insights
- **Reports**: Add reliability metrics
- **Weka ML**: Better clustering with clean data

## Support Workflow

1. **Data Loading** → CSV menu → Select file
2. **Model Training** → Train Adaptive ML button
3. **Apply & Review** → Apply Corrections button
4. **Analytics** → View ML Analytics button
5. **Export** → Model Export tab → Save

## Summary

The Adaptive ML system:
- ✅ Learns machine-specific error patterns
- ✅ Provides personalized correction factors
- ✅ Calculates reliability metrics
- ✅ Generates maintenance recommendations
- ✅ Exports models for analysis
- ✅ Improves accuracy over time

**Status**: All systems operational and integrated!

---
**Last Updated**: December 2025
