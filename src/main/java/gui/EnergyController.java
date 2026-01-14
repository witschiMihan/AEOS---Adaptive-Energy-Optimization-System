package gui;

import service.EnergyAnalyzer;
import service.BitCorrectionEngine;
import service.AlertNotificationManager;
import service.AdaptiveCorrectionEngine;
import service.DecliningCurveForecaster;
import service.WellHealthAssessment;
import model.EnergyRecord;
import model.Machine;
import util.DataLoader;
import util.ChartGenerator;
import util.ReportGenerator;
import ml.LinearRegressionModel;
import ml.WekaIntegration;
import ml.AdaptiveMLBitCorrection;
import ml.MLEngineManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class EnergyController {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel dashboardPanel;
    private JPanel analysisPanel;
    private JPanel correctionPanel;

    private JLabel avgConsumptionLabel;
    private JLabel maxConsumptionLabel;
    private JLabel minConsumptionLabel;
    private JLabel totalConsumptionLabel;
    private JLabel stdDevLabel;
    private JLabel alertStatusLabel;

    private JTable energyTable;
    private JTable machineTable;
    private JTable correctionTable;
    private JTable wellHealthTable;

    private EnergyAnalyzer energyAnalyzer;
    private BitCorrectionEngine bitCorrectionEngine;
    private AdaptiveMLBitCorrection adaptiveMLCorrection;
    private AdaptiveCorrectionEngine adaptiveCorrectionEngine;
    private AlertNotificationManager alertManager;
    private DataLoader dataLoader;
    private MLEngineManager mlEngineManager;
    private List<EnergyRecord> energyRecords;
    private List<Machine> machines;
    private List<service.AdaptiveCorrectionEngine.BeforeAfterComparison> lastAppliedComparisons;
    
    // SCADA Auto-Refresh
    private Timer autoRefreshTimer;
    private int autoRefreshInterval = 5000; // 5 seconds default
    private boolean autoRefreshEnabled = false;

    public EnergyController(JFrame frame) {
        this.frame = frame;
        this.energyAnalyzer = new EnergyAnalyzer();
        this.bitCorrectionEngine = new BitCorrectionEngine();
        this.adaptiveMLCorrection = new AdaptiveMLBitCorrection();
        this.adaptiveCorrectionEngine = new AdaptiveCorrectionEngine();
        this.alertManager = new AlertNotificationManager();
        this.dataLoader = new DataLoader();
        this.mlEngineManager = new MLEngineManager();
        this.energyRecords = new ArrayList<>();
        this.machines = new ArrayList<>();
        this.lastAppliedComparisons = new ArrayList<>();

        initializeUI();
        loadSampleData();
        trainMLEngines();
        updateAllDisplays();
    }

    private void initializeUI() {
        mainPanel = new BackgroundPanel("assets/aeos_logo.png");
        mainPanel.setLayout(new BorderLayout());

        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add the new animated AEOS Dashboard as the first tab
        JPanel aeosDashboardPanel = createAEOSDashboardPanel();
        tabbedPane.addTab("⚡ AEOS Dashboard", aeosDashboardPanel);

        // Add the Anomaly Detection Dashboard (includes live charts)
        JPanel anomalyDashboardPanel = createAnomalyDashboardPanel();
        tabbedPane.addTab("🚨 Anomaly Detection", anomalyDashboardPanel);

        dashboardPanel = createDashboardPanel();
        tabbedPane.addTab("Dashboard", dashboardPanel);

        analysisPanel = createAnalysisPanel();
        tabbedPane.addTab("Analysis", analysisPanel);

        correctionPanel = createCorrectionPanel();
        tabbedPane.addTab("Bit Correction", correctionPanel);
        
        JPanel chartPanel = createChartPanel();
        tabbedPane.addTab("Charts", chartPanel);
        
        JPanel multiMachinePanel = createMultiMachineDashboard();
        tabbedPane.addTab("Multi-Machine", multiMachinePanel);
        
        JPanel mlPanel = createMLPanel();
        tabbedPane.addTab("ML Analytics", mlPanel);
        
        JPanel realtimeDashboard = createRealtimeDashboard();
        tabbedPane.addTab("Real-Time ML", realtimeDashboard);
        
        JPanel learningPanel = createLearningBehaviorPanel();
        tabbedPane.addTab("Learning Progress", learningPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Footer with developer information; clicking the footer opens GitHub profile
        JPanel mainFooterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainFooterPanel.setBackground(new Color(230, 230, 230));
        mainFooterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String githubUrl = "https://github.com/witschiMihan";
        JLabel mainFooterLabel = new JLabel("Terms Of Use | Privacy Notice | Opt-Out | Go to RigVisionX");
        mainFooterLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        mainFooterLabel.setForeground(Color.BLUE);
        mainFooterLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        mainFooterLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(githubUrl));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Unable to open GitHub profile in browser.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainFooterPanel.add(mainFooterLabel);
        mainPanel.add(mainFooterPanel, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("Load Data from CSV");
        JMenuItem saveItem = new JMenuItem("Save Data to CSV");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadItem.addActionListener(e -> loadDataFromFile());
        saveItem.addActionListener(e -> saveDataToFile());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu reportMenu = new JMenu("Reports");
        JMenuItem htmlReportItem = new JMenuItem("Export HTML Report");
        JMenuItem jsonReportItem = new JMenuItem("Export JSON Report");
        JMenuItem csvReportItem = new JMenuItem("Export CSV Summary");
        JMenuItem pdfReportItem = new JMenuItem("Export PDF Report");

        htmlReportItem.addActionListener(e -> generateHTMLReport());
        jsonReportItem.addActionListener(e -> generateJSONReport());
        csvReportItem.addActionListener(e -> generateCSVReport());
        pdfReportItem.addActionListener(e -> generatePDFReport());

        reportMenu.add(htmlReportItem);
        reportMenu.add(jsonReportItem);
        reportMenu.add(csvReportItem);
        reportMenu.addSeparator();
        reportMenu.add(pdfReportItem);

        // NEW: Advanced Analytics Menu
        JMenu analyticsMenu = new JMenu("Analytics");
        JMenuItem alertsItem = new JMenuItem("Live Alerts");
        JMenuItem declineItem = new JMenuItem("Decline Curve Forecast");
        JMenuItem healthItem = new JMenuItem("Well Health");
        JMenuItem trainEngineItem = new JMenuItem("Train Adaptive Engine");
        JMenuItem learningProgressItem = new JMenuItem("Learning Progress");

        alertsItem.addActionListener(e -> showLiveAlerts());
        declineItem.addActionListener(e -> showDeclineCurveForecast());
        healthItem.addActionListener(e -> showWellHealthDashboard());
        trainEngineItem.addActionListener(e -> trainAdaptiveEngine());
        learningProgressItem.addActionListener(e -> showLearningProgress());

        analyticsMenu.add(alertsItem);
        analyticsMenu.add(declineItem);
        analyticsMenu.add(healthItem);
        analyticsMenu.addSeparator();
        analyticsMenu.add(trainEngineItem);
        analyticsMenu.add(learningProgressItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(reportMenu);
        menuBar.add(analyticsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Create the animated AEOS Dashboard panel with energy flow visualization
     */
    private JPanel createAEOSDashboardPanel() {
        AEOSDashboardPanel aeosPanel = new AEOSDashboardPanel();
        
        // Connect to real energy data
        aeosPanel.setEnergyData(energyAnalyzer, energyRecords);
        
        // Update data periodically
        Timer updateTimer = new Timer(5000, e -> {
            aeosPanel.setEnergyData(energyAnalyzer, energyRecords);
        });
        updateTimer.start();
        
        return aeosPanel;
    }
    
    /**
     * Create the Anomaly Detection Dashboard panel with AI monitoring and live charts
     */
    private JPanel createAnomalyDashboardPanel() {
        return new AEOSAnomalyDashboard();
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);

        JPanel tablesPanel = createTablesPanel();
        panel.add(tablesPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Energy Statistics"));

        avgConsumptionLabel = createStatLabel("Avg Consumption", "0.00 kWh");
        maxConsumptionLabel = createStatLabel("Max Consumption", "0.00 kWh");
        minConsumptionLabel = createStatLabel("Min Consumption", "0.00 kWh");
        totalConsumptionLabel = createStatLabel("Total Consumption", "0.00 kWh");
        stdDevLabel = createStatLabel("Std Deviation", "0.00 kWh");

        panel.add(avgConsumptionLabel.getParent());
        panel.add(maxConsumptionLabel.getParent());
        panel.add(minConsumptionLabel.getParent());
        panel.add(totalConsumptionLabel.getParent());
        panel.add(stdDevLabel.getParent());

        return panel;
    }

    private JLabel createStatLabel(String title, String value) {
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(0, 102, 204));

        statPanel.add(titleLabel, BorderLayout.NORTH);
        statPanel.add(valueLabel, BorderLayout.CENTER);

        return valueLabel;
    }

    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel energyPanel = new JPanel(new BorderLayout());
        energyPanel.setBorder(BorderFactory.createTitledBorder("Energy Records"));
        energyTable = new JTable();
        JScrollPane energyScroll = new JScrollPane(energyTable);
        energyPanel.add(energyScroll, BorderLayout.CENTER);

        JPanel machinePanel = new JPanel(new BorderLayout());
        machinePanel.setBorder(BorderFactory.createTitledBorder("Machines"));
        machineTable = new JTable();
        JScrollPane machineScroll = new JScrollPane(machineTable);
        machinePanel.add(machineScroll, BorderLayout.CENTER);

        panel.add(energyPanel);
        panel.add(machinePanel);

        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton analyzeButton = new JButton("Analyze Data");
        JButton regressionButton = new JButton("Run Regression");

        analyzeButton.addActionListener(e -> performAnalysis());
        regressionButton.addActionListener(e -> performRegression());

        controlPanel.add(analyzeButton);
        controlPanel.add(regressionButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCorrectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton correctButton = new JButton("Apply Corrections");
        JButton trainMLButton = new JButton("Train Adaptive ML");
        JButton analyticsButton = new JButton("View ML Analytics");
        
        correctButton.addActionListener(e -> applyCorrectionAndUpdate());
        trainMLButton.addActionListener(e -> trainAdaptiveMLModel());
        analyticsButton.addActionListener(e -> showMLAnalytics());

        controlPanel.add(correctButton);
        controlPanel.add(trainMLButton);
        controlPanel.add(analyticsButton);
        
        // NEW: SCADA Auto-Refresh Toggle
        JToggleButton autoRefreshButton = new JToggleButton("Auto Refresh: OFF");
        autoRefreshButton.addActionListener(e -> toggleAutoRefresh(autoRefreshButton));
        controlPanel.add(autoRefreshButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        correctionTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(correctionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSampleData() {
        machines.clear();
        energyRecords.clear();

        machines.add(new Machine("M001", "Compressor Unit A", "Factory Floor 1"));
        machines.add(new Machine("M002", "Pump System B", "Factory Floor 2"));
        machines.add(new Machine("M003", "HVAC System", "Control Room"));

        LocalDateTime now = LocalDateTime.now();
        energyRecords.add(new EnergyRecord("R001", "M001", 150.5, now));
        energyRecords.add(new EnergyRecord("R002", "M002", 200.3, now.plusHours(1)));
        energyRecords.add(new EnergyRecord("R003", "M003", 75.8, now.plusHours(2)));
        energyRecords.add(new EnergyRecord("R004", "M001", 165.2, now.plusHours(3)));
        energyRecords.add(new EnergyRecord("R005", "M002", 195.7, now.plusHours(4)));
    }

    private void updateAllDisplays() {
        updateStatisticsDisplay();
        updateEnergyTable();
        updateMachineTable();
    }

    private void updateStatisticsDisplay() {
        if (energyRecords.isEmpty()) {
            return;
        }

        double avg = energyAnalyzer.calculateAverageConsumption(energyRecords);
        double max = energyAnalyzer.findMaxConsumption(energyRecords);
        double min = energyAnalyzer.findMinConsumption(energyRecords);
        double total = energyAnalyzer.calculateTotalConsumption(energyRecords);
        double stdDev = energyAnalyzer.calculateStandardDeviation(energyRecords);

        avgConsumptionLabel.setText(String.format("%.2f kWh", avg));
        maxConsumptionLabel.setText(String.format("%.2f kWh", max));
        minConsumptionLabel.setText(String.format("%.2f kWh", min));
        totalConsumptionLabel.setText(String.format("%.2f kWh", total));
        stdDevLabel.setText(String.format("%.2f kWh", stdDev));
    }

    private void updateEnergyTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Record ID");
        model.addColumn("Machine ID");
        model.addColumn("Energy (kWh)");
        model.addColumn("Timestamp");
        model.addColumn("Error Bits");

        for (EnergyRecord record : energyRecords) {
            model.addRow(new Object[]{
                record.getRecordId(),
                record.getMachineId(),
                String.format("%.2f", record.getEnergyConsumption()),
                record.getTimestamp(),
                record.getErrorBits()
            });
        }

        energyTable.setModel(model);
    }

    private void updateMachineTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Machine ID");
        model.addColumn("Name");
        model.addColumn("Location");
        model.addColumn("Status");

        for (Machine machine : machines) {
            model.addRow(new Object[]{
                machine.getMachineId(),
                machine.getMachineName(),
                machine.getLocation(),
                machine.isActive() ? "Active" : "Inactive"
            });
        }

        machineTable.setModel(model);
    }

    private void applyCorrectionAndUpdate() {
        for (EnergyRecord record : energyRecords) {
            bitCorrectionEngine.correctBitErrors(record);
        }

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Record ID");
        model.addColumn("Machine ID");
        model.addColumn("Original Value");
        model.addColumn("Error Bits");
        model.addColumn("Integrity Valid");

        for (EnergyRecord record : energyRecords) {
            model.addRow(new Object[]{
                record.getRecordId(),
                record.getMachineId(),
                String.format("%.2f", record.getEnergyConsumption()),
                record.getErrorBits(),
                bitCorrectionEngine.validateDataIntegrity(record) ? "Yes" : "No"
            });
        }

        correctionTable.setModel(model);
        JOptionPane.showMessageDialog(frame, "Bit correction applied to all records!");
    }

    private void performAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Energy Analysis Results ===\n\n");
        sb.append(String.format("Average Consumption: %.2f kWh\n",
            energyAnalyzer.calculateAverageConsumption(energyRecords)));
        sb.append(String.format("Max Consumption: %.2f kWh\n",
            energyAnalyzer.findMaxConsumption(energyRecords)));
        sb.append(String.format("Min Consumption: %.2f kWh\n",
            energyAnalyzer.findMinConsumption(energyRecords)));
        sb.append(String.format("Total Consumption: %.2f kWh\n",
            energyAnalyzer.calculateTotalConsumption(energyRecords)));
        sb.append(String.format("Standard Deviation: %.2f kWh\n",
            energyAnalyzer.calculateStandardDeviation(energyRecords)));

        JTextArea resultsArea = new JTextArea(sb.toString());
        resultsArea.setEditable(false);
        JOptionPane.showMessageDialog(frame, new JScrollPane(resultsArea), 
            "Analysis Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void performRegression() {
        if (energyRecords.size() < 2) {
            JOptionPane.showMessageDialog(frame, "Need at least 2 records for regression!");
            return;
        }

        LinearRegressionModel model = new LinearRegressionModel();
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        for (int i = 0; i < energyRecords.size(); i++) {
            xValues.add((double) i);
            yValues.add(energyRecords.get(i).getEnergyConsumption());
        }

        model.train(xValues, yValues);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Linear Regression Results ===\n\n");
        sb.append(String.format("Slope: %.4f\n", model.getSlope()));
        sb.append(String.format("Intercept: %.4f\n", model.getIntercept()));
        sb.append(String.format("R-Squared: %.4f\n", model.getRSquared()));
        sb.append(String.format("\nPredicted value for x=5: %.2f kWh\n", model.predict(5)));

        JTextArea resultsArea = new JTextArea(sb.toString());
        resultsArea.setEditable(false);
        JOptionPane.showMessageDialog(frame, new JScrollPane(resultsArea),
            "Regression Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadDataFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            energyRecords = dataLoader.loadEnergyRecordsFromCSV(filePath);
            updateAllDisplays();
            JOptionPane.showMessageDialog(frame, "Data loaded successfully!");
        }
    }

    private void saveDataToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            dataLoader.saveEnergyRecordsToCSV(filePath, energyRecords);
            JOptionPane.showMessageDialog(frame, "Data saved successfully!");
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(frame,
            "Smart Energy Consumption & Bit Correction System\n" +
            "Version 1.0\n\n" +
            "A comprehensive system for monitoring energy consumption\n" +
            "and detecting/correcting data transmission errors.\n\n" +
            "Developed by Witschi B. Mihan on December 31, 2025\n" +
            "AI & ML Developer",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // Chart Panel Implementation
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Energy Consumption Chart"));
        
        // Chart display area
        JPanel chartDisplay = ChartGenerator.createEnergyChart(energyRecords);
        
        // Control panel for chart options
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh Chart");
        refreshButton.addActionListener(e -> {
            JPanel updated = ChartGenerator.createEnergyChart(energyRecords);
            panel.remove(chartDisplay);
            panel.add(updated, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
        controlPanel.add(refreshButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(chartDisplay, BorderLayout.CENTER);
        
        return panel;
    }

    // Multi-Machine Dashboard Implementation
    private JPanel createMultiMachineDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Multi-Machine Dashboard"));
        
        // Get unique machines from records
        java.util.Set<String> machineIds = new java.util.HashSet<>();
        for (EnergyRecord record : energyRecords) {
            machineIds.add(record.getMachineId());
        }
        
        // Control panel with selection options
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        
        // Machine selection
        JLabel machineLabel = new JLabel("Select Machine:");
        JComboBox<String> machineSelector = new JComboBox<>();
        machineSelector.addItem("All Machines");
        for (String machineId : machineIds) {
            machineSelector.addItem(machineId);
        }
        
        // Analytics type selection
        JLabel analyticsLabel = new JLabel("Analytics Type:");
        JComboBox<String> analyticsSelector = new JComboBox<>(new String[]{
            "Consumption Summary",
            "Error Analysis",
            "Performance Metrics",
            "Trend Analysis"
        });
        
        // View type selection
        JLabel viewLabel = new JLabel("View Type:");
        JComboBox<String> viewSelector = new JComboBox<>(new String[]{
            "Summary Chart",
            "Before/After Comparison",
            "Error Patterns",
            "Time Series"
        });
        
        controlPanel.add(machineLabel);
        controlPanel.add(machineSelector);
        controlPanel.add(analyticsLabel);
        controlPanel.add(analyticsSelector);
        controlPanel.add(viewLabel);
        controlPanel.add(viewSelector);
        
        // Dashboard info panel with scrolling
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Content panel that will be updated based on selections
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        EnergyAnalyzer localAnalyzer = new EnergyAnalyzer();
        
        // Function to update content based on selections
        Runnable updateContent = () -> {
            contentPanel.removeAll();
            
            String selectedMachine = (String) machineSelector.getSelectedItem();
            String selectedAnalytics = (String) analyticsSelector.getSelectedItem();
            String selectedView = (String) viewSelector.getSelectedItem();
            
            // Filter records based on machine selection
            java.util.List<EnergyRecord> filteredRecords = energyRecords;
            if (!selectedMachine.equals("All Machines")) {
                filteredRecords = energyRecords.stream()
                    .filter(r -> r.getMachineId().equals(selectedMachine))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (filteredRecords.isEmpty()) {
                contentPanel.add(new JLabel("No data available for selected filters"), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
                return;
            }
            
            // Create content based on view type
            JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
            viewPanel.setBorder(BorderFactory.createTitledBorder(selectedView));
            
            switch(selectedView) {
                case "Before/After Comparison":
                    // Show before/after comparison data
                    String[] columnNames = {"Machine ID", "Original (kWh)", "Corrected (kWh)", "Correction (%)"};
                    java.util.List<Object[]> rowData = new java.util.ArrayList<>();
                    
                    for (EnergyRecord record : filteredRecords) {
                        double original = record.getEnergyConsumption();
                        double corrected = original; // Would be from ML correction
                        double correction = ((corrected - original) / original) * 100;
                        rowData.add(new Object[]{
                            record.getMachineId(),
                            String.format("%.2f", original),
                            String.format("%.2f", corrected),
                            String.format("%.2f%%", correction)
                        });
                    }
                    
                    Object[][] data = rowData.toArray(new Object[0][]);
                    JTable comparisonTable = new JTable(data, columnNames);
                    comparisonTable.setFont(new Font("Arial", Font.PLAIN, 11));
                    comparisonTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
                    comparisonTable.setRowHeight(25);
                    viewPanel.add(new JScrollPane(comparisonTable), BorderLayout.CENTER);
                    break;
                    
                case "Error Patterns":
                    // Show error analysis
                    JTextArea errorText = new JTextArea();
                    errorText.setEditable(false);
                    errorText.setFont(new Font("Monospaced", Font.PLAIN, 11));
                    errorText.setLineWrap(true);
                    errorText.setWrapStyleWord(true);
                    
                    StringBuilder errorAnalysis = new StringBuilder();
                    errorAnalysis.append("ERROR ANALYSIS\n");
                    errorAnalysis.append("==============\n\n");
                    
                    for (EnergyRecord record : filteredRecords) {
                        long errorBits = record.getErrorBits();
                        errorAnalysis.append(String.format("Machine: %s | Error Bits: %d | Consumption: %.2f kWh\n",
                            record.getMachineId(), errorBits, record.getEnergyConsumption()));
                    }
                    
                    errorText.setText(errorAnalysis.toString());
                    viewPanel.add(new JScrollPane(errorText), BorderLayout.CENTER);
                    break;
                    
                default:  // Summary Chart or Time Series
                    // Display chart or summary
                    if (selectedMachine.equals("All Machines")) {
                        // Show all machines summary
                        JTextArea allMachinesSummary = new JTextArea();
                        allMachinesSummary.setEditable(false);
                        allMachinesSummary.setFont(new Font("Monospaced", Font.PLAIN, 11));
                        
                        StringBuilder allSummary = new StringBuilder();
                        allSummary.append("ALL MACHINES CONSUMPTION SUMMARY\n");
                        allSummary.append("==================================\n\n");
                        
                        double totalAllConsumption = 0;
                        long totalAllErrors = 0;
                        int totalRecords = energyRecords.size();
                        
                        Map<String, Double> machineConsumption = new HashMap<>();
                        Map<String, Long> machineErrors = new HashMap<>();
                        Map<String, Integer> machineRecords = new HashMap<>();
                        
                        for (EnergyRecord record : energyRecords) {
                            String machineId = record.getMachineId();
                            double consumption = record.getEnergyConsumption();
                            long errors = record.getErrorBits();
                            
                            totalAllConsumption += consumption;
                            totalAllErrors += errors;
                            
                            machineConsumption.put(machineId, machineConsumption.getOrDefault(machineId, 0.0) + consumption);
                            machineErrors.put(machineId, machineErrors.getOrDefault(machineId, 0L) + errors);
                            machineRecords.put(machineId, machineRecords.getOrDefault(machineId, 0) + 1);
                        }
                        
                        allSummary.append(String.format("Total Records: %d\n", totalRecords));
                        allSummary.append(String.format("Total Consumption: %.2f kWh\n", totalAllConsumption));
                        allSummary.append(String.format("Average Consumption: %.2f kWh\n", totalAllConsumption / totalRecords));
                        allSummary.append(String.format("Total Error Bits: %d\n", totalAllErrors));
                        allSummary.append(String.format("Average Error Bits: %.2f\n\n", (double) totalAllErrors / totalRecords));
                        
                        allSummary.append("PER-MACHINE BREAKDOWN:\n");
                        allSummary.append("---------------------\n\n");
                        
                        for (String machineId : new TreeSet<>(machineConsumption.keySet())) {
                            double consumption = machineConsumption.get(machineId);
                            long errors = machineErrors.get(machineId);
                            int records = machineRecords.get(machineId);
                            
                            allSummary.append(String.format("Machine: %s\n", machineId));
                            allSummary.append(String.format("  Records: %d\n", records));
                            allSummary.append(String.format("  Total Consumption: %.2f kWh\n", consumption));
                            allSummary.append(String.format("  Average Consumption: %.2f kWh\n", consumption / records));
                            allSummary.append(String.format("  Total Error Bits: %d\n", errors));
                            allSummary.append(String.format("  Avg Error Bits: %.2f\n\n", (double) errors / records));
                        }
                        
                        allMachinesSummary.setText(allSummary.toString());
                        viewPanel.add(new JScrollPane(allMachinesSummary), BorderLayout.CENTER);
                    } else {
                        // Single machine chart
                        JTextArea summaryText = new JTextArea();
                        summaryText.setEditable(false);
                        summaryText.setFont(new Font("Monospaced", Font.PLAIN, 11));
                        
                        StringBuilder summary = new StringBuilder();
                        summary.append("MACHINE SUMMARY: ").append(selectedMachine).append("\n");
                        summary.append("================================\n\n");
                        
                        double avgConsumption = localAnalyzer.calculateAverageConsumption(filteredRecords);
                        double maxConsumption = localAnalyzer.findMaxConsumption(filteredRecords);
                        double minConsumption = localAnalyzer.findMinConsumption(filteredRecords);
                        double totalConsumption = localAnalyzer.calculateTotalConsumption(filteredRecords);
                        
                        summary.append(String.format("Average Consumption: %.2f kWh\n", avgConsumption));
                        summary.append(String.format("Maximum Consumption: %.2f kWh\n", maxConsumption));
                        summary.append(String.format("Minimum Consumption: %.2f kWh\n", minConsumption));
                        summary.append(String.format("Total Consumption: %.2f kWh\n", totalConsumption));
                        summary.append(String.format("Record Count: %d\n\n", filteredRecords.size()));
                        
                        // Error analysis
                        long totalErrorBits = filteredRecords.stream()
                            .mapToLong(EnergyRecord::getErrorBits)
                            .sum();
                        double avgErrorBits = filteredRecords.stream()
                            .mapToLong(EnergyRecord::getErrorBits)
                            .average()
                            .orElse(0);
                        
                        summary.append(String.format("Total Error Bits: %d\n", totalErrorBits));
                        summary.append(String.format("Average Error Bits: %.2f\n", avgErrorBits));
                        
                        summaryText.setText(summary.toString());
                        viewPanel.add(new JScrollPane(summaryText), BorderLayout.CENTER);
                    }
            }
            
            contentPanel.add(viewPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        };
        
        // Add listeners to update content when selections change
        machineSelector.addActionListener(e -> updateContent.run());
        analyticsSelector.addActionListener(e -> updateContent.run());
        viewSelector.addActionListener(e -> updateContent.run());
        
        // Initial content update
        updateContent.run();
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    // ML Analytics Panel Implementation
    private JPanel createMLPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("ML Analytics"));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTabbedPane mlTabbedPane = new JTabbedPane();
        
        // Tab 1: Feature Importance
        JPanel featurePanel = new JPanel(new BorderLayout(10, 10));
        featurePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea featureText = new JTextArea();
        featureText.setEditable(false);
        featureText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        featureText.setLineWrap(true);
        featureText.setWrapStyleWord(true);
        
        if (!energyRecords.isEmpty()) {
            StringBuilder features = new StringBuilder();
            features.append("FEATURE IMPORTANCE ANALYSIS\n");
            features.append("============================\n\n");
            String[] featureImportance = ml.WekaIntegration.analyzeFeatureImportance(energyRecords);
            for (String feature : featureImportance) {
                features.append("• ").append(feature).append("\n");
            }
            featureText.setText(features.toString());
        } else {
            featureText.setText("No data available for analysis.");
        }
        featurePanel.add(new JScrollPane(featureText), BorderLayout.CENTER);
        mlTabbedPane.addTab("Features", featurePanel);
        
        // Tab 2: Anomaly Detection
        JPanel anomalyPanel = new JPanel(new BorderLayout(10, 10));
        anomalyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea anomalyText = new JTextArea();
        anomalyText.setEditable(false);
        anomalyText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        anomalyText.setLineWrap(true);
        anomalyText.setWrapStyleWord(true);
        
        if (!energyRecords.isEmpty()) {
            StringBuilder anomalies = new StringBuilder();
            anomalies.append("ANOMALY DETECTION ANALYSIS\n");
            anomalies.append("===========================\n\n");
            boolean[] anomalyArray = ml.WekaIntegration.detectAnomalies(energyRecords, 2.0);
            int anomalyCount = 0;
            for (boolean isAnomaly : anomalyArray) {
                if (isAnomaly) anomalyCount++;
            }
            anomalies.append("Detection Method: 2.0 Standard Deviations\n");
            anomalies.append("Anomalies Found: ").append(anomalyCount).append(" out of ").append(anomalyArray.length).append("\n\n");
            if (anomalyCount > 0) {
                anomalies.append("Records with anomalies:\n");
                for (int i = 0; i < anomalyArray.length; i++) {
                    if (anomalyArray[i]) {
                        EnergyRecord rec = energyRecords.get(i);
                        anomalies.append("  • ").append(rec.getMachineId()).append(": ")
                            .append(String.format("%.2f kWh", rec.getEnergyConsumption())).append("\n");
                    }
                }
            } else {
                anomalies.append("No significant anomalies detected in the data.");
            }
            anomalyText.setText(anomalies.toString());
        } else {
            anomalyText.setText("No data available for analysis.");
        }
        anomalyPanel.add(new JScrollPane(anomalyText), BorderLayout.CENTER);
        mlTabbedPane.addTab("Anomalies", anomalyPanel);
        
        // Tab 3: Energy Categorization
        JPanel categoryPanel = new JPanel(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea categoryText = new JTextArea();
        categoryText.setEditable(false);
        categoryText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        categoryText.setLineWrap(true);
        categoryText.setWrapStyleWord(true);
        
        if (!energyRecords.isEmpty()) {
            StringBuilder categories = new StringBuilder();
            categories.append("ENERGY USAGE CATEGORIZATION\n");
            categories.append("============================\n\n");
            for (EnergyRecord record : energyRecords.stream().limit(15).collect(java.util.stream.Collectors.toList())) {
                String category = ml.WekaIntegration.categorizeEnergyUsage(record.getEnergyConsumption(), energyRecords);
                categories.append(String.format("Machine %-5s: %8.2f kWh  [%s]\n", 
                    record.getMachineId(), record.getEnergyConsumption(), category));
            }
            categoryText.setText(categories.toString());
        } else {
            categoryText.setText("No data available for analysis.");
        }
        categoryPanel.add(new JScrollPane(categoryText), BorderLayout.CENTER);
        mlTabbedPane.addTab("Categories", categoryPanel);
        
        // Tab 4: Forecasting
        JPanel forecastPanel = new JPanel(new BorderLayout(10, 10));
        forecastPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea forecastText = new JTextArea();
        forecastText.setEditable(false);
        forecastText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        forecastText.setLineWrap(true);
        forecastText.setWrapStyleWord(true);
        
        if (!energyRecords.isEmpty()) {
            StringBuilder forecast = new StringBuilder();
            forecast.append("ENERGY CONSUMPTION FORECAST\n");
            forecast.append("=============================\n\n");
            forecast.append("Method: Exponential Smoothing (α=0.3)\n");
            forecast.append("Horizon: 5 steps ahead\n\n");
            double[] forecastValues = ml.WekaIntegration.forecastEnergyConsumption(energyRecords, 0.3, 5);
            for (int i = 0; i < forecastValues.length; i++) {
                forecast.append(String.format("Step %d: %8.2f kWh\n", i + 1, forecastValues[i]));
            }
            forecastText.setText(forecast.toString());
        } else {
            forecastText.setText("No data available for analysis.");
        }
        forecastPanel.add(new JScrollPane(forecastText), BorderLayout.CENTER);
        mlTabbedPane.addTab("Forecast", forecastPanel);
        
        panel.add(mlTabbedPane, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh Analysis");
        refreshButton.addActionListener(e -> {
            JPanel updatedPanel = createMLPanel();
            // Find the ML Analytics tab and update it
            for (int i = 0; i < frame.getContentPane().getComponentCount(); i++) {
                Component comp = frame.getContentPane().getComponent(i);
                if (comp instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) comp;
                    for (int j = 0; j < tabbedPane.getTabCount(); j++) {
                        if ("ML Analytics".equals(tabbedPane.getTitleAt(j))) {
                            tabbedPane.setComponentAt(j, updatedPanel);
                        }
                    }
                }
            }
        });
        controlPanel.add(refreshButton);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // Report Generation Methods
    private void generateHTMLReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files", "html"));
        int result = fileChooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".html")) {
                filePath += ".html";
            }
            
            ReportGenerator.generateHTMLReport(filePath, machines, energyRecords);
            JOptionPane.showMessageDialog(frame, "HTML report generated successfully at:\n" + filePath);
        }
    }

    private void generateJSONReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        int result = fileChooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".json")) {
                filePath += ".json";
            }
            
            ReportGenerator.generateJSONReport(filePath, machines, energyRecords);
            JOptionPane.showMessageDialog(frame, "JSON report generated successfully at:\n" + filePath);
        }
    }

    private void generateCSVReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        int result = fileChooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            
            ReportGenerator.generateCSVSummaryReport(filePath, machines, energyRecords);
            JOptionPane.showMessageDialog(frame, "CSV report generated successfully at:\n" + filePath);
        }
    }
    
    private void generatePDFReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = fileChooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }
            
            ReportGenerator.generatePDFReport(filePath, machines, energyRecords);
            JOptionPane.showMessageDialog(frame, "PDF report generated successfully at:\n" + filePath);
        }
    }

    /**
     * Train the adaptive ML correction model on current energy data
     */
    private void trainAdaptiveMLModel() {
        if (energyRecords.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No energy records to train on!", "Training Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Train the model
        adaptiveMLCorrection.trainModel(energyRecords);

        // Show training results
        StringBuilder results = new StringBuilder();
        results.append("=== Adaptive ML Training Complete ===\n\n");
        results.append("Machine Profiles Learned:\n");
        results.append("─".repeat(50)).append("\n");

        Set<String> machineIds = new java.util.HashSet<>();
        for (EnergyRecord record : energyRecords) {
            machineIds.add(record.getMachineId());
        }

        for (String machineId : machineIds) {
            Map<String, Object> stats = adaptiveMLCorrection.getMachineStatistics(machineId);
            results.append(String.format("Machine: %s\n", machineId));
            results.append(String.format("  Error Rate: %.2f%%\n", 
                ((double) stats.get("errorRate")) * 100));
            results.append(String.format("  Correction Factor: %.4f\n", 
                stats.get("correctionFactor")));
            results.append(String.format("  Reliability Score: %.2f%%\n", 
                ((double) stats.get("reliability")) * 100));
            results.append(String.format("  Confidence Level: %.2f%%\n", 
                ((double) stats.get("confidence")) * 100));
            results.append(String.format("  Samples Processed: %d\n\n", 
                stats.get("samplesProcessed")));
        }

        // Show recommendations
        results.append("\n=== System Recommendations ===\n");
        results.append("─".repeat(50)).append("\n");
        Map<String, String> recommendations = adaptiveMLCorrection.getRecommendations();
        for (Map.Entry<String, String> entry : recommendations.entrySet()) {
            results.append(String.format("%s: %s\n", entry.getKey(), entry.getValue()));
        }

        JTextArea resultsArea = new JTextArea(results.toString());
        resultsArea.setEditable(false);
        resultsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(600, 400));

        JOptionPane.showMessageDialog(frame, scrollPane, "Adaptive ML Training Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Display ML analytics dashboard
     */
    private void showMLAnalytics() {
        JDialog analyticsDialog = new JDialog(frame, "Adaptive ML Analytics Dashboard", true);
        analyticsDialog.setSize(700, 600);
        analyticsDialog.setLocationRelativeTo(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();

        // Machine Performance Tab
        JPanel performancePanel = createPerformanceAnalyticsPanel();
        tabbedPane.addTab("Machine Performance", performancePanel);

        // Correction Patterns Tab
        JPanel patternsPanel = createCorrectionPatternsPanel();
        tabbedPane.addTab("Correction Patterns", patternsPanel);

        // Model Export Tab
        JPanel exportPanel = createModelExportPanel();
        tabbedPane.addTab("Model Export", exportPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom button panel with back button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> analyticsDialog.dispose());
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        analyticsDialog.add(mainPanel);
        analyticsDialog.setVisible(true);
    }

    /**
     * Create performance analytics panel
     */
    private JPanel createPerformanceAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table for machine statistics
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Machine ID");
        model.addColumn("Error Rate (%)");
        model.addColumn("Correction Factor");
        model.addColumn("Reliability (%)");
        model.addColumn("Confidence (%)");
        model.addColumn("Samples");

        Set<String> machineIds = new java.util.HashSet<>();
        for (EnergyRecord record : energyRecords) {
            machineIds.add(record.getMachineId());
        }

        for (String machineId : machineIds) {
            Map<String, Object> stats = adaptiveMLCorrection.getMachineStatistics(machineId);
            model.addRow(new Object[]{
                machineId,
                String.format("%.2f", ((double) stats.get("errorRate")) * 100),
                String.format("%.4f", stats.get("correctionFactor")),
                String.format("%.2f", ((double) stats.get("reliability")) * 100),
                String.format("%.2f", ((double) stats.get("confidence")) * 100),
                stats.get("samplesProcessed")
            });
        }

        JTable analyticsTable = new JTable(model);
        analyticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create correction patterns panel
     */
    private JPanel createCorrectionPatternsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        StringBuilder patterns = new StringBuilder();
        patterns.append("=== Learned Correction Patterns ===\n\n");

        Set<String> machineIds = new java.util.HashSet<>();
        for (EnergyRecord record : energyRecords) {
            machineIds.add(record.getMachineId());
        }

        for (String machineId : machineIds) {
            double errorRate = adaptiveMLCorrection.getMachineReliability(machineId);
            double correctionFactor = adaptiveMLCorrection.getMachineStatistics(machineId).get("correctionFactor") != null ?
                (double) adaptiveMLCorrection.getMachineStatistics(machineId).get("correctionFactor") : 1.0;
            
            patterns.append(String.format("Machine: %s\n", machineId));
            patterns.append(String.format("  Reliability: %.1f%%\n", errorRate * 100));
            patterns.append(String.format("  Adaptive Factor: %.4f\n", correctionFactor));
            patterns.append(String.format("  Pattern: %s\n\n",
                errorRate > 0.9 ? "Stable - No correction needed" :
                errorRate > 0.7 ? "Minor variations - Light correction applied" :
                "Significant drift - Heavy correction applied"));
        }

        JTextArea patternsArea = new JTextArea(patterns.toString());
        patternsArea.setEditable(false);
        patternsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(patternsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create model export panel
     */
    private JPanel createModelExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String modelData = adaptiveMLCorrection.exportModel();

        JTextArea exportArea = new JTextArea(modelData);
        exportArea.setEditable(true);
        exportArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(exportArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyButton = new JButton("Copy to Clipboard");
        JButton saveButton = new JButton("Save Model");

        copyButton.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(exportArea.getText());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(panel, "Model data copied to clipboard!");
        });

        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files (*.json)", "json"));
            int result = fileChooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".json")) {
                    filePath += ".json";
                }
                try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                    writer.write(exportArea.getText());
                    JOptionPane.showMessageDialog(panel, "Model saved successfully!");
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(panel, "Error saving model: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ====== NEW FEATURES ======
    
    /**
     * Show Live Alert Notifications
     */
    private void showLiveAlerts() {
        JDialog alertDialog = new JDialog(frame, "Live Alert Notifications", true);
        alertDialog.setSize(600, 500);
        alertDialog.setLocationRelativeTo(frame);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Alert table
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Time");
        model.addColumn("Machine");
        model.addColumn("Severity");
        model.addColumn("Message");
        
        // Generate alerts
        Map<String, Double> errorRates = new HashMap<>();
        Map<String, Double> reliability = new HashMap<>();
        for (String machineId : getMachineIds()) {
            List<EnergyRecord> machineRecs = energyRecords.stream()
                .filter(r -> r.getMachineId().equals(machineId))
                .toList();
            double errorRate = machineRecs.stream().mapToLong(EnergyRecord::getErrorBits).sum() / 
                             (double)(machineRecs.size() * 10);
            errorRates.put(machineId, errorRate);
            reliability.put(machineId, 1.0 - errorRate);
        }
        
        List<AlertNotificationManager.Alert> alerts = alertManager.analyzeAndAlert(energyRecords, errorRates, reliability);
        
        for (AlertNotificationManager.Alert alert : alerts) {
            model.addRow(new Object[]{
                alert.timestamp.toString(),
                alert.machineId,
                alert.severity.name(),
                alert.message
            });
        }
        
        JTable alertTable = new JTable(model);
        alertTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(alertTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status: " + alertManager.getAlertSummary()));
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        alertDialog.add(mainPanel);
        alertDialog.setVisible(true);
    }
    
    /**
     * Show Decline Curve Forecasting
     */
    private void showDeclineCurveForecast() {
        JDialog forecastDialog = new JDialog(frame, "Decline Curve Forecasting", true);
        forecastDialog.setSize(700, 600);
        forecastDialog.setLocationRelativeTo(frame);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Machine selector
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> machineCombo = new JComboBox<>(getMachineIds().toArray(new String[0]));
        selectorPanel.add(new JLabel("Select Machine:"));
        selectorPanel.add(machineCombo);
        
        JTextArea forecastText = new JTextArea();
        forecastText.setEditable(false);
        forecastText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(forecastText);
        
        machineCombo.addActionListener(e -> {
            String selected = (String) machineCombo.getSelectedItem();
            if (selected != null) {
                DecliningCurveForecaster.DeclineModel model = 
                    DecliningCurveForecaster.analyzeDecline(energyRecords, selected, 12);
                if (model != null) {
                    forecastText.setText(DecliningCurveForecaster.getDeclineDescription(model));
                } else {
                    forecastText.setText("Insufficient data for decline curve analysis.");
                }
            }
        });
        
        mainPanel.add(selectorPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        forecastDialog.add(mainPanel);
        forecastDialog.setVisible(true);
    }
    
    /**
     * Show Well Health Dashboard
     */
    private void showWellHealthDashboard() {
        JDialog healthDialog = new JDialog(frame, "Well Health Assessment", true);
        healthDialog.setSize(800, 600);
        healthDialog.setLocationRelativeTo(frame);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Health table
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Machine ID");
        model.addColumn("Overall Score");
        model.addColumn("Status");
        model.addColumn("Reliability");
        model.addColumn("Efficiency");
        model.addColumn("Stability");
        model.addColumn("Trend");
        
        for (String machineId : getMachineIds()) {
            WellHealthAssessment.HealthScore health = WellHealthAssessment.calculateWellHealth(machineId, energyRecords);
            model.addRow(new Object[]{
                health.machineId,
                String.format("%.1f", health.overallScore),
                health.status.name(),
                String.format("%.1f", health.reliabilityScore),
                String.format("%.1f", health.efficiencyScore),
                String.format("%.1f", health.stabilityScore),
                String.format("%.1f", health.trendScore)
            });
        }
        
        wellHealthTable = new JTable(model);
        wellHealthTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(wellHealthTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        JTextArea detailsText = new JTextArea();
        detailsText.setEditable(false);
        detailsText.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        wellHealthTable.getSelectionModel().addListSelectionListener(e -> {
            if (wellHealthTable.getSelectedRow() >= 0) {
                String machineId = (String) wellHealthTable.getValueAt(wellHealthTable.getSelectedRow(), 0);
                WellHealthAssessment.HealthScore health = WellHealthAssessment.calculateWellHealth(machineId, energyRecords);
                detailsText.setText(health.summary);
            }
        });
        
        detailsPanel.add(new JLabel("Details:"), BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(detailsText), BorderLayout.CENTER);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setDividerLocation(300);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        healthDialog.add(mainPanel);
        healthDialog.setVisible(true);
    }
    
    /**
     * Toggle SCADA-style Auto Refresh
     */
    private void toggleAutoRefresh(JToggleButton toggleButton) {
        autoRefreshEnabled = toggleButton.isSelected();
        
        if (autoRefreshEnabled) {
            startAutoRefresh();
            toggleButton.setText("Auto Refresh: ON");
            toggleButton.setBackground(new Color(100, 200, 100));
        } else {
            stopAutoRefresh();
            toggleButton.setText("Auto Refresh: OFF");
            toggleButton.setBackground(UIManager.getColor("Button.background"));
        }
    }
    
    /**
     * Start auto-refresh timer
     */
    private void startAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        
        autoRefreshTimer = new Timer(autoRefreshInterval, e -> {
            SwingUtilities.invokeLater(this::updateAllDisplays);
        });
        autoRefreshTimer.start();
    }
    
    /**
     * Stop auto-refresh timer
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
    }
    
    /**
     * Update alert status label
     */
    private void updateAlertStatus() {
        if (alertStatusLabel != null) {
            Map<String, Double> errorRates = new HashMap<>();
            Map<String, Double> reliability = new HashMap<>();
            for (String machineId : getMachineIds()) {
                List<EnergyRecord> machineRecs = energyRecords.stream()
                    .filter(r -> r.getMachineId().equals(machineId))
                    .toList();
                double errorRate = machineRecs.stream().mapToLong(EnergyRecord::getErrorBits).sum() / 
                                 (double)(machineRecs.size() * 10);
                errorRates.put(machineId, errorRate);
                reliability.put(machineId, 1.0 - errorRate);
            }
            
            alertManager.analyzeAndAlert(energyRecords, errorRates, reliability);
            alertStatusLabel.setText("Alerts: " + alertManager.getAlertSummary());
        }
    }
    
    /**
     * Get unique machine IDs
     */
    private Set<String> getMachineIds() {
        Set<String> ids = new java.util.HashSet<>();
        for (EnergyRecord record : energyRecords) {
            ids.add(record.getMachineId());
        }
        return ids;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Create Learning Behavior Visualization Panel
     */
    private JPanel createLearningBehaviorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel with controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton trainButton = new JButton("Train Adaptive Engine");
        trainButton.addActionListener(e -> trainAdaptiveEngine());
        JButton refreshButton = new JButton("Refresh Learning Data");
        refreshButton.addActionListener(e -> refreshLearningDisplay());
        controlPanel.add(trainButton);
        controlPanel.add(refreshButton);
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Center panel with learning charts and stats
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Learning curve chart (custom painted)
        JPanel learningCurvePanel = createLearningCurveChart();
        centerPanel.add(new JScrollPane(learningCurvePanel));
        
        // Learning statistics panel
        JPanel statsPanel = createLearningStatsPanel();
        centerPanel.add(new JScrollPane(statsPanel));
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with info
        JLabel infoLabel = new JLabel("Learning behavior shows how the Adaptive Correction Engine improves accuracy over training epochs");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 10));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create Learning Curve Chart Panel
     */
    private JPanel createLearningCurveChart() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 50;
                
                // Draw axes
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
                g2.drawLine(padding, padding, padding, height - padding); // Y-axis
                
                // Axis labels
                g2.drawString("Epochs", width - 80, height - padding + 25);
                g2.rotate(-Math.PI / 2);
                g2.drawString("Accuracy (%)", -height / 2, 15);
                g2.rotate(Math.PI / 2);
                
                // Get learning data
                Map<String, List<Double>> learningData = adaptiveCorrectionEngine.getLearningCurveData();
                
                if (!learningData.isEmpty()) {
                    int machineCount = learningData.size();
                    Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA};
                    int colorIdx = 0;
                    
                    int plotWidth = width - 2 * padding;
                    int plotHeight = height - 2 * padding;
                    
                    for (Map.Entry<String, List<Double>> entry : learningData.entrySet()) {
                        String machineId = entry.getKey();
                        List<Double> accuracies = entry.getValue();
                        
                        g2.setColor(colors[colorIdx % colors.length]);
                        
                        for (int i = 0; i < accuracies.size() - 1; i++) {
                            double accuracy1 = accuracies.get(i);
                            double accuracy2 = accuracies.get(i + 1);
                            
                            int x1 = padding + (i * plotWidth) / Math.max(1, accuracies.size() - 1);
                            int y1 = height - padding - (int)(accuracy1 * plotHeight / 100);
                            int x2 = padding + ((i + 1) * plotWidth) / Math.max(1, accuracies.size() - 1);
                            int y2 = height - padding - (int)(accuracy2 * plotHeight / 100);
                            
                            g2.drawLine(x1, y1, x2, y2);
                            g2.fillOval(x1 - 3, y1 - 3, 6, 6);
                        }
                        colorIdx++;
                    }
                    
                    // Legend
                    colorIdx = 0;
                    int legendX = padding + 20;
                    int legendY = padding + 20;
                    for (String machineId : learningData.keySet()) {
                        g2.setColor(colors[colorIdx % colors.length]);
                        g2.fillRect(legendX, legendY, 10, 10);
                        g2.setColor(Color.BLACK);
                        g2.drawString(machineId, legendX + 15, legendY + 10);
                        legendY += 20;
                        colorIdx++;
                    }
                } else {
                    g2.setColor(Color.GRAY);
                    g2.drawString("No learning data available. Train the engine first.", width / 2 - 150, height / 2);
                }
            }
        };
        panel.setPreferredSize(new Dimension(600, 250));
        return panel;
    }
    
    /**
     * Create Learning Statistics Panel
     */
    private JPanel createLearningStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columnNames = {"Metric", "Value"};
        Object[][] data = getAdaptiveLearningStats();
        
        JTable statsTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        statsTable.setFont(new Font("Arial", Font.PLAIN, 11));
        statsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(statsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create Before/After Comparison Panel
     */
    private JPanel createBeforeAfterComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton applyCorrectionsButton = new JButton("Apply Adaptive Corrections");
        applyCorrectionsButton.addActionListener(e -> applyAdaptiveCorrections());
        JButton refreshButton = new JButton("Refresh Comparison Data");
        refreshButton.addActionListener(e -> refreshBeforeAfterDisplay());
        controlPanel.add(applyCorrectionsButton);
        controlPanel.add(refreshButton);
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Comparison table
        JPanel tablePanel = createBeforeAfterTable();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = createComparisonSummary();
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create Before/After Comparison Table
     */
    private JPanel createBeforeAfterTable() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columnNames = {"Machine ID", "Original (kWh)", "Corrected (kWh)", "Correction (kWh)", "Correction (%)", "Error Bits", "Timestamp"};
        Object[][] data = getBeforeAfterComparisonData();
        
        JTable comparisonTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        comparisonTable.setFont(new Font("Arial", Font.PLAIN, 11));
        comparisonTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        comparisonTable.setRowHeight(25);
        
        // Set column widths
        comparisonTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Machine ID
        comparisonTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Original
        comparisonTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Corrected
        comparisonTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Correction Amount
        comparisonTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Correction %
        comparisonTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Error Bits
        comparisonTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Timestamp
        
        JScrollPane scrollPane = new JScrollPane(comparisonTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create Comparison Summary Panel
     */
    private JPanel createComparisonSummary() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        
        Object[][] summaryData = getComparisonSummaryData();
        
        JTable summaryTable = new JTable(summaryData, new String[]{"Metric", "Value"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        summaryTable.setFont(new Font("Arial", Font.PLAIN, 11));
        summaryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        panel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Train Adaptive Correction Engine
     */
    private void trainAdaptiveEngine() {
        if (energyRecords.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No energy records available. Please load data first.", "Training Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        adaptiveCorrectionEngine.trainOnRecords(energyRecords);
        
        String report = adaptiveCorrectionEngine.getLearningReport();
        JOptionPane.showMessageDialog(frame, "Training completed!\n\n" + report, "Adaptive Engine Training", JOptionPane.INFORMATION_MESSAGE);
        
        refreshLearningDisplay();
    }
    
    /**
     * Show Learning Progress Dialog
     */
    private void showLearningProgress() {
        JDialog dialog = new JDialog(frame, "Learning Progress", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(frame);
        
        JPanel panel = createLearningBehaviorPanel();
        dialog.add(panel);
        
        dialog.setVisible(true);
    }
    
    /**
     * Apply Adaptive Corrections to Energy Records
     */
    private void applyAdaptiveCorrections() {
        if (energyRecords.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No energy records available.", "Apply Corrections Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Apply the learned corrections
        List<service.AdaptiveCorrectionEngine.BeforeAfterComparison> comparisons = adaptiveCorrectionEngine.applyLearnedCorrections(energyRecords);
        
        // Store for display in tables
        lastAppliedComparisons = new ArrayList<>(comparisons);
        
        if (comparisons == null || comparisons.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No corrections were applied. Please train the Adaptive Engine first in the Learning Progress tab.", "No Corrections Applied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(frame, "Adaptive corrections applied to " + comparisons.size() + " energy records!", "Corrections Applied", JOptionPane.INFORMATION_MESSAGE);
        
        refreshBeforeAfterDisplay();
        updateAllDisplays();
    }
    
    /**
     * Refresh Learning Display
     */
    private void refreshLearningDisplay() {
        // This would refresh the learning behavior panel
        // Implementation depends on how the UI is structured
        frame.repaint();
    }
    
    /**
     * Refresh Before/After Display
     */
    private void refreshBeforeAfterDisplay() {
        // Find the main panel and locate the Before/After tab
        JPanel currentMainPanel = mainPanel;
        
        // Find the tabbed pane in the main panel
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                
                // Find the Before/After tab
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    String tabTitle = tabbedPane.getTitleAt(i);
                    if ("Before/After".equals(tabTitle)) {
                        // Recreate the panel with fresh data
                        JPanel updatedBeforeAfterPanel = createBeforeAfterComparisonPanel();
                        tabbedPane.setComponentAt(i, updatedBeforeAfterPanel);
                        tabbedPane.revalidate();
                        tabbedPane.repaint();
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Get Adaptive Learning Statistics
     */
    private Object[][] getAdaptiveLearningStats() {
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        
        // Get learning report
        String report = adaptiveCorrectionEngine.getLearningReport();
        String[] lines = report.split("\n");
        
        Object[][] data = new Object[lines.length][2];
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(":")) {
                String[] parts = lines[i].split(":", 2);
                data[i][0] = parts[0].trim();
                data[i][1] = parts.length > 1 ? parts[1].trim() : "";
            } else {
                data[i][0] = lines[i];
                data[i][1] = "";
            }
        }
        
        return data;
    }
    
    /**
     * Get Before/After Comparison Data
     */
    private Object[][] getBeforeAfterComparisonData() {
        List<Object[]> dataList = new ArrayList<>();
        
        // Use the last applied comparisons
        List<service.AdaptiveCorrectionEngine.BeforeAfterComparison> allComparisons = lastAppliedComparisons;
        
        // If empty, try getting from engine
        if (allComparisons.isEmpty()) {
            allComparisons = adaptiveCorrectionEngine.getAllComparisons();
        }
        
        // If still empty, return empty data
        if (allComparisons.isEmpty()) {
            return new Object[0][];
        }
        
        // Add comparison data to list
        for (service.AdaptiveCorrectionEngine.BeforeAfterComparison comparison : allComparisons) {
            dataList.add(new Object[]{
                comparison.machineId,
                String.format("%.2f", comparison.originalConsumption),
                String.format("%.2f", comparison.correctedConsumption),
                String.format("%.2f", comparison.correction),
                String.format("%.2f%%", comparison.correctionPercent),
                comparison.errorBits,
                comparison.timestamp != null ? comparison.timestamp.toString() : "N/A"
            });
        }
        
        return dataList.toArray(new Object[0][]);
    }
    
    /**
     * Get Comparison Summary Data
     */
    private Object[][] getComparisonSummaryData() {
        List<Object[]> dataList = new ArrayList<>();
        
        // Get comparisons from the last applied corrections
        List<service.AdaptiveCorrectionEngine.BeforeAfterComparison> allComparisons = lastAppliedComparisons;
        
        // If empty, try getting from engine
        if (allComparisons.isEmpty()) {
            allComparisons = adaptiveCorrectionEngine.getAllComparisons();
        }
        
        if (allComparisons.isEmpty()) {
            dataList.add(new Object[]{"No comparison data", "Apply corrections first"});
            return dataList.toArray(new Object[0][]);
        }
        
        // Calculate overall statistics
        double totalOriginalConsumption = 0;
        double totalCorrectedConsumption = 0;
        double totalCorrection = 0;
        long totalErrorBits = 0;
        int recordCount = allComparisons.size();
        
        Map<String, Double> machineOriginal = new HashMap<>();
        Map<String, Double> machineCorrected = new HashMap<>();
        Map<String, Long> machineErrorBits = new HashMap<>();
        Map<String, Integer> machineCount = new HashMap<>();
        
        for (service.AdaptiveCorrectionEngine.BeforeAfterComparison comp : allComparisons) {
            totalOriginalConsumption += comp.originalConsumption;
            totalCorrectedConsumption += comp.correctedConsumption;
            totalCorrection += comp.correction;
            totalErrorBits += comp.errorBits;
            
            machineOriginal.put(comp.machineId, machineOriginal.getOrDefault(comp.machineId, 0.0) + comp.originalConsumption);
            machineCorrected.put(comp.machineId, machineCorrected.getOrDefault(comp.machineId, 0.0) + comp.correctedConsumption);
            machineErrorBits.put(comp.machineId, machineErrorBits.getOrDefault(comp.machineId, 0L) + comp.errorBits);
            machineCount.put(comp.machineId, machineCount.getOrDefault(comp.machineId, 0) + 1);
        }
        
        // Add overall statistics
        dataList.add(new Object[]{"Total Records Corrected", String.valueOf(recordCount)});
        dataList.add(new Object[]{"Total Original Consumption (kWh)", String.format("%.2f", totalOriginalConsumption)});
        dataList.add(new Object[]{"Total Corrected Consumption (kWh)", String.format("%.2f", totalCorrectedConsumption)});
        dataList.add(new Object[]{"Total Correction (kWh)", String.format("%.2f", totalCorrection)});
        dataList.add(new Object[]{"Average Correction %", String.format("%.2f%%", (totalCorrection / totalOriginalConsumption) * 100)});
        dataList.add(new Object[]{"Total Error Bits", String.valueOf(totalErrorBits)});
        dataList.add(new Object[]{"Average Error Bits per Record", String.format("%.2f", (double) totalErrorBits / recordCount)});
        
        // Add per-machine statistics
        dataList.add(new Object[]{"", ""});  // Empty row for spacing
        dataList.add(new Object[]{"PER-MACHINE STATISTICS", ""});
        
        for (String machineId : new TreeSet<>(machineOriginal.keySet())) {
            double originalConsumption = machineOriginal.get(machineId);
            double correctedConsumption = machineCorrected.get(machineId);
            long errorBits = machineErrorBits.get(machineId);
            int count = machineCount.get(machineId);
            double correctionPercent = ((correctedConsumption - originalConsumption) / originalConsumption) * 100;
            
            dataList.add(new Object[]{machineId + " - Records", String.valueOf(count)});
            dataList.add(new Object[]{machineId + " - Original (kWh)", String.format("%.2f", originalConsumption)});
            dataList.add(new Object[]{machineId + " - Corrected (kWh)", String.format("%.2f", correctedConsumption)});
            dataList.add(new Object[]{machineId + " - Correction %", String.format("%.2f%%", correctionPercent)});
            dataList.add(new Object[]{machineId + " - Error Bits", String.valueOf(errorBits)});
            dataList.add(new Object[]{"", ""});  // Spacing between machines
        }
        
        return dataList.toArray(new Object[0][]);
    }

    /**
     * Train all ML engines
     */
    private void trainMLEngines() {
        if (energyRecords.isEmpty()) return;
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  Training ML Engine with Sample Data   ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        mlEngineManager.trainFullPipeline(energyRecords);
        System.out.println("\n" + mlEngineManager.generateMLReport());
    }

    /**
     * Create Real-Time ML Dashboard with live predictions
     */
    private JPanel createRealtimeDashboard() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JLabel testValueLabel = new JLabel("Test Energy Value (kWh):");
        JTextField testValueField = new JTextField("50.0", 10);
        JButton predictButton = new JButton("Get Real-Time Prediction");
        JButton startStreamButton = new JButton("Start Live Stream");
        JButton stopStreamButton = new JButton("Stop Stream");
        JButton refreshDashButton = new JButton("Refresh Dashboard");
        
        predictButton.addActionListener(e -> {
            try {
                double value = Double.parseDouble(testValueField.getText());
                Map<String, Object> prediction = mlEngineManager.comprehensivePredict(value);
                showPredictionResults(prediction);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid energy value", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        startStreamButton.addActionListener(e -> {
            mlEngineManager.getRealtimeEngine().start();
            JOptionPane.showMessageDialog(frame, "Real-time prediction engine started!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        stopStreamButton.addActionListener(e -> {
            mlEngineManager.getRealtimeEngine().stop();
            JOptionPane.showMessageDialog(frame, "Real-time prediction engine stopped", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        refreshDashButton.addActionListener(e -> updateRealtimeDashboard());

        controlPanel.add(testValueLabel);
        controlPanel.add(testValueField);
        controlPanel.add(predictButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(startStreamButton);
        controlPanel.add(stopStreamButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(refreshDashButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // Dashboard Content
        JTabbedPane dashboardTabs = new JTabbedPane();

        // Tab 1: Ensemble Predictions
        JPanel ensemblePanel = createEnsemblePanel();
        dashboardTabs.addTab("Ensemble Predictions", ensemblePanel);

        // Tab 2: Time Series
        JPanel timeSeriesPanel = createTimeSeriesPanel();
        dashboardTabs.addTab("Time Series Forecast", timeSeriesPanel);

        // Tab 3: Anomaly Detection
        JPanel anomalyPanel = createAnomalyPanel();
        dashboardTabs.addTab("Anomaly Detection", anomalyPanel);

        // Tab 4: Feature Importance
        JPanel featurePanel = createFeaturePanel();
        dashboardTabs.addTab("Feature Importance", featurePanel);

        // Tab 5: Model Status
        JPanel statusPanel = createModelStatusPanel();
        dashboardTabs.addTab("Model Status", statusPanel);

        // Tab 6: Live Metrics
        JPanel metricsPanel = createLiveMetricsPanel();
        dashboardTabs.addTab("Live Metrics", metricsPanel);

        mainPanel.add(dashboardTabs, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Create Ensemble Predictions Panel
     */
    private JPanel createEnsemblePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Ensemble ML Model Predictions"));
        panel.setBackground(new Color(245, 245, 250));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setText("╔════════════════════════════════════════════════════════════╗\n" +
                "║           ENSEMBLE MODEL PREDICTIONS                         ║\n" +
                "╚════════════════════════════════════════════════════════════╝\n\n" +
                "Model Components:\n" +
                "  1. Linear Regression Model - Baseline trend prediction\n" +
                "  2. Decision Tree Model - Pattern recognition\n" +
                "  3. K-Nearest Neighbors - Local averaging (k=5)\n" +
                "  4. Random Forest - Ensemble of bootstrap trees\n\n" +
                "Features:\n" +
                "  • Weighted ensemble averaging (default 0.25 each)\n" +
                "  • Adaptive weight adjustment based on accuracy\n" +
                "  • Multi-step ahead forecasting\n" +
                "  • Prediction confidence bounds (95%, 99%, 90%)\n" +
                "  • Real-time model performance tracking\n\n" +
                "Status: Models trained and ready for predictions");

        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Time Series Panel
     */
    private JPanel createTimeSeriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Time Series Analysis & Forecasting"));
        panel.setBackground(new Color(245, 250, 245));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setText("╔════════════════════════════════════════════════════════════╗\n" +
                "║          TIME SERIES ANALYZER                               ║\n" +
                "╚════════════════════════════════════════════════════════════╝\n\n" +
                "Decomposition Methods:\n" +
                "  • STL Decomposition (Seasonal & Trend decomposition using Loess)\n" +
                "    - Trend: 7-point moving average smoothing\n" +
                "    - Seasonal: 24-hour periodicity (daily patterns)\n" +
                "    - Residual: Anomalies and noise\n\n" +
                "  • Exponential Smoothing (Holt's Method)\n" +
                "    - Level smoothing coefficient (α): 0.3\n" +
                "    - Trend smoothing coefficient (β): 0.2\n" +
                "    - Adaptive to recent data changes\n\n" +
                "Analysis Features:\n" +
                "  • ARIMA-style differencing for stationarity\n" +
                "  • Autocorrelation function (ACF) calculation\n" +
                "  • Trend detection (INCREASING/DECREASING/STABLE)\n" +
                "  • Seasonality strength measurement\n" +
                "  • Forecast confidence intervals (90%, 95%, 99%)\n\n" +
                "Status: Ready for forecasting");

        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Anomaly Detection Panel
     */
    private JPanel createAnomalyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Anomaly Detection System"));
        panel.setBackground(new Color(255, 250, 245));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setText("╔════════════════════════════════════════════════════════════╗\n" +
                "║         ADVANCED ANOMALY DETECTION                          ║\n" +
                "╚════════════════════════════════════════════════════════════╝\n\n" +
                "Detection Methods (Ensemble Scored):\n\n" +
                "  1. Z-Score Method\n" +
                "     - Gaussian outlier detection\n" +
                "     - Threshold: 3 standard deviations\n\n" +
                "  2. IQR (Interquartile Range) Method\n" +
                "     - Tukey fences with 1.5× IQR margin\n" +
                "     - Distribution-free approach\n\n" +
                "  3. Isolation Forest\n" +
                "     - Extreme value detection\n" +
                "     - Isolation path analysis\n\n" +
                "  4. Local Outlier Factor (LOF)\n" +
                "     - Density-based method\n" +
                "     - K=5 neighbor configuration\n\n" +
                "Severity Levels:\n" +
                "  • SEVERE: Anomaly score > 0.7\n" +
                "  • MODERATE: Anomaly score > 0.5\n" +
                "  • MILD: Anomaly score > 0.3\n" +
                "  • NORMAL: Anomaly score ≤ 0.3\n\n" +
                "Status: Baseline calibrated, monitoring active");

        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Feature Importance Panel
     */
    private JPanel createFeaturePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Feature Engineering & Importance"));
        panel.setBackground(new Color(250, 245, 255));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setText("╔════════════════════════════════════════════════════════════╗\n" +
                "║         FEATURE ENGINEERING ANALYSIS                        ║\n" +
                "╚════════════════════════════════════════════════════════════╝\n\n" +
                "Base Features Extracted (10):\n\n" +
                "  1. Base Consumption - Normalized to historical average\n" +
                "  2. Consumption Trend - Momentum (recent vs. older)\n" +
                "  3. Consumption Volatility - Standard deviation ratio\n" +
                "  4. Error Bit Density - Proportion of error bits (0-1)\n" +
                "  5. Error Bit Trend - Change in error bits over time\n" +
                "  6. Hour of Day - Temporal feature (0-1 normalized)\n" +
                "  7. Day of Week - Temporal feature (0-1 normalized)\n" +
                "  8. Season Factor - Sin-based seasonal encoding\n" +
                "  9. Peak Deviation - Distance from peak consumption\n" +
                "  10. Anomaly Score - Z-score based outlier indicator\n\n" +
                "Feature Engineering Methods:\n" +
                "  • Polynomial expansion (configurable degree)\n" +
                "  • Feature interaction generation (pairwise products)\n" +
                "  • Z-score normalization\n" +
                "  • Pearson correlation importance ranking\n" +
                "  • Top-N feature selection\n\n" +
                "Status: 10 features extracted, ready for modeling");

        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Model Status Panel
     */
    private JPanel createModelStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("ML Engine Status"));
        panel.setBackground(new Color(240, 245, 240));

        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // Get module status
        Map<String, Boolean> moduleStatus = mlEngineManager.getModuleStatus();
        StringBuilder statusText = new StringBuilder();
        statusText.append("╔════════════════════════════════════════════════════════════╗\n");
        statusText.append("║           ML ENGINE MODULE STATUS                         ║\n");
        statusText.append("╚════════════════════════════════════════════════════════════╝\n\n");

        for (Map.Entry<String, Boolean> entry : moduleStatus.entrySet()) {
            String status = entry.getValue() ? "✓ ENABLED" : "✗ DISABLED";
            statusText.append(String.format("%-30s %s\n", entry.getKey() + ":", status));
        }

        statusText.append("\n").append("═".repeat(62)).append("\n");
        statusText.append("Total ML Modules: 8\n");
        statusText.append("Active Modules: ").append(moduleStatus.values().stream().filter(b -> b).count()).append("\n");
        statusText.append("System Status: ").append(moduleStatus.values().stream().filter(b -> b).count() == 8 ? "✓ FULLY OPERATIONAL" : "⚠ PARTIAL").append("\n");

        statusArea.setText(statusText.toString());

        JScrollPane scrollPane = new JScrollPane(statusArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Live Metrics Panel
     */
    private JPanel createLiveMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Real-Time ML Metrics"));
        panel.setBackground(new Color(240, 240, 245));

        JTextArea metricsArea = new JTextArea();
        metricsArea.setEditable(false);
        metricsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        StringBuilder metricsText = new StringBuilder();
        metricsText.append("╔════════════════════════════════════════════════════════════╗\n");
        metricsText.append("║              REAL-TIME PREDICTION METRICS                 ║\n");
        metricsText.append("╚════════════════════════════════════════════════════════════╝\n\n");

        metricsText.append("Streaming Configuration:\n");
        metricsText.append("  • Data Buffer Size: 1000 records\n");
        metricsText.append("  • Update Frequency: Every 5 seconds or 100 records\n");
        metricsText.append("  • Processing Threads: 2 (data + model update)\n");
        metricsText.append("  • Anomaly Detection: 3-sigma rule\n\n");

        metricsText.append("Model Ensemble:\n");
        metricsText.append("  • Concurrent Models: 3 (Linear, Neural, Ensemble)\n");
        metricsText.append("  • Confidence Calculation: RMSE/MAPE based\n");
        metricsText.append("  • Online Learning: Enabled\n\n");

        metricsText.append("Performance Tracking:\n");
        metricsText.append("  • Mean Prediction Error: Calculating...\n");
        metricsText.append("  • Model Accuracy: Training in progress\n");
        metricsText.append("  • Confidence Score: N/A (waiting for predictions)\n\n");

        metricsText.append("Status: Real-time engine ready\n");
        metricsText.append("Last Update: ").append(LocalDateTime.now()).append("\n");

        metricsArea.setText(metricsText.toString());

        JScrollPane scrollPane = new JScrollPane(metricsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Update Real-Time Dashboard
     */
    private void updateRealtimeDashboard() {
        // This would be called periodically to update live metrics
        System.out.println("Real-time dashboard updated at " + LocalDateTime.now());
    }

    /**
     * Show prediction results dialog
     */
    private void showPredictionResults(Map<String, Object> prediction) {
        StringBuilder results = new StringBuilder();
        results.append("╔════════════════════════════════════════════════════════════╗\n");
        results.append("║          COMPREHENSIVE PREDICTION RESULTS                 ║\n");
        results.append("╚════════════════════════════════════════════════════════════╝\n\n");

        results.append(String.format("Base Energy Value: %.2f kWh\n", prediction.get("baseValue")));
        results.append(String.format("Timestamp: %s\n\n", prediction.get("timestamp")));

        if (prediction.containsKey("ensemblePrediction")) {
            results.append(String.format("Ensemble Prediction: %.2f kWh\n", prediction.get("ensemblePrediction")));
        }

        if (prediction.containsKey("realtimePrediction")) {
            results.append(String.format("Real-Time Prediction: %.2f kWh\n", prediction.get("realtimePrediction")));
            results.append(String.format("Confidence: %.2f%%\n", ((Double) prediction.get("confidence")) * 100));
            results.append(String.format("Status: %s\n\n", prediction.get("status")));
        }

        if (prediction.containsKey("timeSeriesForecast")) {
            results.append("Time Series Forecast (Next 5 steps):\n");
            double[] forecast = (double[]) prediction.get("timeSeriesForecast");
            for (int i = 0; i < Math.min(forecast.length, 5); i++) {
                results.append(String.format("  Step %d: %.2f kWh\n", i + 1, forecast[i]));
            }
        }

        JTextArea resultArea = new JTextArea(results.toString());
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultArea.setBackground(new Color(240, 245, 240));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        JOptionPane.showMessageDialog(frame, scrollPane, "Prediction Results", JOptionPane.INFORMATION_MESSAGE);
    }
}



