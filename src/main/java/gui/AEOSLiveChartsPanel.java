package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import ml.AEOSMLBridge;
import ml.AEOSMLBridge.MLData;

/**
 * AEOS Live Charts Dashboard Panel
 * Real-time monitoring for Load, Pressure, Efficiency
 * Connected to Python ML Engine via AEOSMLBridge
 */
public class AEOSLiveChartsPanel extends JPanel {
    
    private AEOSLiveChartPanel loadChart;
    private AEOSLiveChartPanel pressureChart;
    private AEOSLiveChartPanel efficiencyChart;
    private AEOSMLBridge mlBridge;
    private JLabel connectionStatusLabel;
    private Thread mlDataThread;
    private boolean running = true;
    
    public AEOSLiveChartsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 15, 25));
        
        // Initialize ML Bridge
        mlBridge = new AEOSMLBridge();
        
        // Header with connection status
        ChartsHeaderPanel header = new ChartsHeaderPanel();
        connectionStatusLabel = new JLabel("Connecting to ML Engine...");
        connectionStatusLabel.setForeground(new Color(255, 200, 0));
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.add(connectionStatusLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        
        // Charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        chartsPanel.setBackground(new Color(10, 15, 25));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        loadChart = new AEOSLiveChartPanel("SYSTEM LOAD (%)", 0, 100);
        pressureChart = new AEOSLiveChartPanel("PIPE PRESSURE (bar)", 80, 200);
        efficiencyChart = new AEOSLiveChartPanel("ENERGY EFFICIENCY (%)", 70, 100);
        
        chartsPanel.add(loadChart);
        chartsPanel.add(pressureChart);
        chartsPanel.add(efficiencyChart);
        
        add(chartsPanel, BorderLayout.CENTER);
        
        // Start ML data streaming
        startMLDataStream();
    }
    
    /**
     * Start streaming ML data from Python engine
     */
    private void startMLDataStream() {
        mlDataThread = new Thread(() -> {
            // Try to connect
            if (!mlBridge.connect()) {
                SwingUtilities.invokeLater(() -> {
                    connectionStatusLabel.setText("ML Engine Offline - Using Simulated Data");
                    connectionStatusLabel.setForeground(new Color(255, 150, 0));
                });
                return;
            }
            
            SwingUtilities.invokeLater(() -> {
                connectionStatusLabel.setText("✓ ML Engine Connected - LIVE DATA");
                connectionStatusLabel.setForeground(new Color(0, 255, 150));
            });
            
            // Continuously read ML data
            while (running) {
                try {
                    MLData mlData = mlBridge.getLiveData();
                    
                    if (mlData != null) {
                        // Update charts with real ML data
                        SwingUtilities.invokeLater(() -> {
                            loadChart.addDataPoint((int) mlData.load);
                            pressureChart.addDataPoint((int) mlData.pressure);
                            efficiencyChart.addDataPoint((int) mlData.efficiency);
                            
                            // Check for anomalies
                            if (mlData.anomaly == 1) {
                                connectionStatusLabel.setText("⚠ ANOMALY DETECTED - ML Engine Active");
                                connectionStatusLabel.setForeground(new Color(255, 100, 100));
                            } else {
                                connectionStatusLabel.setText("✓ ML Engine Connected - LIVE DATA");
                                connectionStatusLabel.setForeground(new Color(0, 255, 150));
                            }
                        });
                    } else {
                        // Connection lost, try to reconnect
                        if (!mlBridge.isConnected()) {
                            SwingUtilities.invokeLater(() -> {
                                connectionStatusLabel.setText("Reconnecting to ML Engine...");
                                connectionStatusLabel.setForeground(new Color(255, 200, 0));
                            });
                            mlBridge.reconnect();
                        }
                    }
                    
                    Thread.sleep(1000); // 1 second update rate
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Error in ML data stream: " + e.getMessage());
                    try {
                        Thread.sleep(2000); // Wait before retry
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
        
        mlDataThread.setDaemon(true);
        mlDataThread.start();
    }
    
    /**
     * Stop all animations and ML connection when panel is removed
     */
    public void stopAnimations() {
        running = false;
        if (mlBridge != null) {
            mlBridge.disconnect();
        }
        if (loadChart != null) loadChart.stopTimer();
        if (pressureChart != null) pressureChart.stopTimer();
        if (efficiencyChart != null) efficiencyChart.stopTimer();
        if (mlDataThread != null) {
            mlDataThread.interrupt();
        }
    }
}

/* ================= HEADER ================= */

class ChartsHeaderPanel extends JPanel {
    
    public ChartsHeaderPanel() {
        setPreferredSize(new Dimension(100, 60));
        setBackground(new Color(15, 25, 40));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel title = new JLabel(" AEOS • Real-Time Energy Monitoring");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel status = new JLabel("LIVE DATA STREAM   ");
        status.setForeground(new Color(0, 220, 160));
        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        add(title, BorderLayout.WEST);
        add(status, BorderLayout.EAST);
    }
}

/* ================= LIVE CHART ================= */
/* 
 * NOTE: AEOSLiveChartPanel class has been moved to AEOSAnomalyDashboard.java
 * This file is kept for reference but the chart panel is now integrated
 * into the Anomaly Detection Dashboard.
 */

