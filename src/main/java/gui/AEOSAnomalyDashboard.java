package gui;

import ml.AEOSMLBridge;
import ml.AEOSMLBridge.MLData;

import javax.swing.*;
import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.Random;

/**
 * AEOS Anomaly Detection Dashboard with Live Charts
 * Connected to Python ML Engine - Real-time monitoring
 */
public class AEOSAnomalyDashboard extends JPanel {

    private JLabel statusLabel;
    private JLabel lamp;
    private JTextArea logArea;
    private JProgressBar riskBar;
    
    // Live Charts
    private AEOSLiveChartPanel loadChart;
    private AEOSLiveChartPanel pressureChart;
    private AEOSLiveChartPanel efficiencyChart;

    public AEOSAnomalyDashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 15, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        startMLListener(); // 🔥 ML Connected
    }

    /**
     * Create a standalone frame for testing purposes
     */
    public static JFrame createStandaloneFrame() {
        JFrame frame = new JFrame("AEOS Anomaly Detection - AI Monitoring System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);
        
        AEOSAnomalyDashboard dashboard = new AEOSAnomalyDashboard();
        frame.add(dashboard);
        
        return frame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createStandaloneFrame();
            frame.setVisible(true);
        });
    }

    /* ================= HEADER ================= */

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 25, 40));
        header.setPreferredSize(new Dimension(100, 60));

        JLabel title = new JLabel(" AEOS • Anomaly Detection System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        statusLabel = new JLabel("AI STATUS: CONNECTING...   ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(new Color(0, 200, 255));

        header.add(title, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        return header;
    }

    /* ================= MAIN ================= */

    private JPanel buildMain() {
        // Use BoxLayout vertical to ensure all components maintain their space
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(10, 15, 25));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top row: Health and Log panels - Fixed height
        JPanel topRow = new JPanel(new GridLayout(1, 2, 15, 15));
        topRow.setBackground(new Color(10, 15, 25));
        topRow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 250));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        topRow.setMinimumSize(new Dimension(0, 200));
        
        JPanel healthPanel = buildHealthPanel();
        healthPanel.setPreferredSize(new Dimension(0, 250));
        healthPanel.setMinimumSize(new Dimension(200, 200));
        
        JPanel logPanel = buildLogPanel();
        logPanel.setPreferredSize(new Dimension(0, 250));
        logPanel.setMinimumSize(new Dimension(200, 200));
        
        topRow.add(healthPanel);
        topRow.add(logPanel);

        // Bottom row: Live Charts - Fixed height
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        chartsPanel.setBackground(new Color(10, 15, 25));
        chartsPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 220));
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        chartsPanel.setMinimumSize(new Dimension(0, 180));
        
        loadChart = new AEOSLiveChartPanel("SYSTEM LOAD (%)", 0, 100);
        pressureChart = new AEOSLiveChartPanel("PIPE PRESSURE (bar)", 80, 200);
        efficiencyChart = new AEOSLiveChartPanel("ENERGY EFFICIENCY (%)", 70, 100);
        
        // Set minimum sizes for charts to prevent them from disappearing
        loadChart.setMinimumSize(new Dimension(200, 180));
        pressureChart.setMinimumSize(new Dimension(200, 180));
        efficiencyChart.setMinimumSize(new Dimension(200, 180));
        
        chartsPanel.add(loadChart);
        chartsPanel.add(pressureChart);
        chartsPanel.add(efficiencyChart);

        main.add(topRow);
        main.add(Box.createVerticalStrut(15));
        main.add(chartsPanel);

        return main;
    }

    private JPanel buildHealthPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(18, 22, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 255), 2));

        JLabel title = new JLabel("SYSTEM HEALTH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        lamp = new JLabel("●");
        lamp.setFont(new Font("Segoe UI", Font.BOLD, 100));
        lamp.setForeground(Color.GRAY);
        lamp.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(25));
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lamp);

        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(18, 22, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 255), 2));

        JLabel title = new JLabel("LIVE AI EVENT LOG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        logArea = new JTextArea();
        logArea.setBackground(new Color(12, 15, 22));
        logArea.setForeground(new Color(0, 220, 140));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setEditable(false);
        // Set preferred rows to limit height
        logArea.setRows(8);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 200));
        scroll.setMinimumSize(new Dimension(200, 150));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /* ================= FOOTER ================= */

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(15, 20, 28));
        footer.setPreferredSize(new Dimension(100, 70));

        JLabel riskLabel = new JLabel("  AI Risk Index");
        riskLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        riskLabel.setForeground(Color.WHITE);

        riskBar = new JProgressBar(0, 100);
        riskBar.setStringPainted(true);
        riskBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        riskBar.setForeground(new Color(0, 200, 120));
        riskBar.setBackground(new Color(40, 40, 40));

        footer.add(riskLabel, BorderLayout.NORTH);
        footer.add(riskBar, BorderLayout.CENTER);

        return footer;
    }

    /* ================= ML CONNECTION ================= */

    private void startMLListener() {

        AEOSMLBridge ml = new AEOSMLBridge();
        boolean connected = ml.connect();

        // Update status based on connection
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("AI STATUS: CONNECTED   ");
                statusLabel.setForeground(new Color(0, 220, 140));
                log("Connected to ML Engine - Waiting for data...");
            } else {
                statusLabel.setText("AI STATUS: OFFLINE   ");
                statusLabel.setForeground(Color.ORANGE);
                log("ML Engine not running");
                log("Please start: python aeos_ml_server_enhanced.py");
            }
        });

        new Thread(() -> {
            int consecutiveNulls = 0;
            while (true) {
                MLData data = ml.getLiveData();
                
                if (data != null) {
                    consecutiveNulls = 0;
                    
                    double load = data.load;
                    double pressure = data.pressure;
                    double efficiency = data.efficiency;
                    int anomaly = data.anomaly;

                    int risk = Math.min(100, (int) ((pressure / 200.0) * 100));

                    SwingUtilities.invokeLater(() -> {
                        // Update connection status if we're receiving data
                        if (!ml.isConnected()) {
                            statusLabel.setText("AI STATUS: RECONNECTING...   ");
                            statusLabel.setForeground(Color.ORANGE);
                        } else {
                            statusLabel.setText("AI STATUS: CONNECTED   ");
                            statusLabel.setForeground(new Color(0, 220, 140));
                        }

                        // Update charts with real-time data
                        loadChart.addDataPoint((int) load);
                        pressureChart.addDataPoint((int) pressure);
                        efficiencyChart.addDataPoint((int) efficiency);

                        riskBar.setValue(risk);

                        if (anomaly == 1) {
                            setCritical("ML anomaly detected | Load=" + load +
                                    " Pressure=" + pressure +
                                    " Efficiency=" + efficiency);
                        } else if (risk > 60) {
                            setWarning("Unstable pattern detected by AI");
                        } else {
                            setNormal("System operating normally");
                        }
                    });
                } else {
                    consecutiveNulls++;
                    
                    // If we haven't received data for a while, try to reconnect
                    if (consecutiveNulls > 50 && !ml.isConnected()) { // 5 seconds of no data
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("AI STATUS: RECONNECTING...   ");
                            statusLabel.setForeground(Color.ORANGE);
                        });
                        ml.reconnect();
                        consecutiveNulls = 0;
                    } else if (consecutiveNulls > 10 && !ml.isConnected()) {
                        // Show offline status if not connected
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("AI STATUS: OFFLINE   ");
                            statusLabel.setForeground(Color.ORANGE);
                            lamp.setForeground(Color.GRAY);
                            riskBar.setValue(0);
                        });
                    }
                }
                
                // Add a small delay to prevent excessive CPU usage
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    /* ================= STATES ================= */

    private void setCritical(String msg) {
        lamp.setForeground(Color.RED);
        statusLabel.setText("AI STATUS: CRITICAL   ");
        log("CRITICAL → " + msg);
    }

    private void setWarning(String msg) {
        lamp.setForeground(Color.ORANGE);
        statusLabel.setText("AI STATUS: WARNING   ");
        log("WARNING → " + msg);
    }

    private void setNormal(String msg) {
        lamp.setForeground(new Color(0, 220, 140));
        statusLabel.setText("AI STATUS: NORMAL   ");
        log("NORMAL → " + msg);
    }

    private void log(String msg) {
        logArea.append("[" + java.time.LocalTime.now().withNano(0) + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}

/* ================= LIVE CHART PANEL ================= */

class AEOSLiveChartPanel extends JPanel {
    
    private final String title;
    private final int minY, maxY;
    private final LinkedList<Integer> data = new LinkedList<>();
    private final Random rand = new Random();
    private Timer timer;
    private boolean useMLData = false;
    
    public AEOSLiveChartPanel(String title, int minY, int maxY) {
        this.title = title;
        this.minY = minY;
        this.maxY = maxY;
        
        setBackground(new Color(18, 22, 30));
        setBorder(BorderFactory.createLineBorder(new Color(0, 120, 255), 2));
        
        // Set fixed sizes to prevent charts from disappearing
        setPreferredSize(new Dimension(300, 220));
        setMinimumSize(new Dimension(200, 180));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        
        // Initialize with some data points for smooth initial display
        int initialPoints = 30;
        for (int i = 0; i < initialPoints; i++) {
            data.add(generateValue());
        }
        
        // Start animation timer (fallback to random if ML not connected)
        timer = new Timer(1000, e -> {
            if (!useMLData) {
                // Keep data buffer size reasonable
                if (data.size() > 60) data.removeFirst();
                data.add(generateValue());
                repaint();
            }
        });
        timer.start();
    }
    
    /**
     * Add data point from ML Bridge
     * @param value The value to add
     */
    public void addDataPoint(int value) {
        useMLData = true;
        if (data.size() > 60) data.removeFirst();
        data.add(value);
        repaint();
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    private int generateValue() {
        int base = (minY + maxY) / 2;
        int variation = rand.nextInt((maxY - minY) / 5);
        return base + (rand.nextBoolean() ? variation : -variation);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawTitle(g2);
        drawGrid(g2);
        drawLineChart(g2);
        drawCurrentValue(g2);
    }
    
    private void drawTitle(Graphics2D g2) {
        // Draw title with better styling
        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Center the title or align left with padding
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = Math.max(15, (getWidth() - titleWidth) / 2);
        
        g2.drawString(title, titleX, 25);
        
        // Draw underline
        g2.setColor(new Color(0, 120, 255, 150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(titleX, 28, titleX + titleWidth, 28);
    }
    
    private void drawGrid(Graphics2D g2) {
        int chartAreaTop = 40;
        int chartAreaBottom = getHeight() - 50;
        int chartAreaLeft = 20;
        int chartAreaRight = getWidth() - 20;
        
        // Draw horizontal grid lines
        g2.setColor(new Color(50, 60, 80));
        g2.setStroke(new BasicStroke(1.0f));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = chartAreaTop + (i * (chartAreaBottom - chartAreaTop) / gridLines);
            g2.drawLine(chartAreaLeft, y, chartAreaRight, y);
        }
        
        // Draw vertical grid lines
        int verticalLines = 6;
        for (int i = 0; i <= verticalLines; i++) {
            int x = chartAreaLeft + (i * (chartAreaRight - chartAreaLeft) / verticalLines);
            g2.drawLine(x, chartAreaTop, x, chartAreaBottom);
        }
    }
    
    private void drawLineChart(Graphics2D g2) {
        if (data.isEmpty()) return;
        
        int chartAreaTop = 40;
        int chartAreaBottom = getHeight() - 50;
        int chartAreaLeft = 20;
        int chartAreaRight = getWidth() - 20;
        int chartWidth = chartAreaRight - chartAreaLeft;
        int chartHeight = chartAreaBottom - chartAreaTop;
        
        // Draw the line with gradient effect
        g2.setColor(new Color(0, 200, 255));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int dataSize = data.size();
        if (dataSize < 2) return;
        
        // Draw smooth line connecting all points
        for (int i = 0; i < dataSize - 1; i++) {
            int x1 = chartAreaLeft + (i * chartWidth / Math.max(dataSize - 1, 1));
            int x2 = chartAreaLeft + ((i + 1) * chartWidth / Math.max(dataSize - 1, 1));
            
            int y1 = chartAreaBottom - scale(data.get(i), chartHeight);
            int y2 = chartAreaBottom - scale(data.get(i + 1), chartHeight);
            
            // Ensure coordinates are within bounds
            y1 = Math.max(chartAreaTop, Math.min(chartAreaBottom, y1));
            y2 = Math.max(chartAreaTop, Math.min(chartAreaBottom, y2));
            
            g2.drawLine(x1, y1, x2, y2);
        }
        
        // Draw data points (only on the last few points to avoid clutter)
        g2.setColor(new Color(0, 255, 200));
        int pointsToShow = Math.min(10, dataSize);
        for (int i = dataSize - pointsToShow; i < dataSize; i++) {
            int x = chartAreaLeft + (i * chartWidth / Math.max(dataSize - 1, 1));
            int y = chartAreaBottom - scale(data.get(i), chartHeight);
            y = Math.max(chartAreaTop, Math.min(chartAreaBottom, y));
            
            // Draw point with glow effect
            g2.setColor(new Color(0, 255, 200, 200));
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.setColor(new Color(0, 200, 255));
            g2.fillOval(x - 3, y - 3, 6, 6);
        }
    }
    
    private void drawCurrentValue(Graphics2D g2) {
        if (data.isEmpty()) return;
        
        int value = data.getLast();
        
        // Draw background for better visibility
        g2.setColor(new Color(18, 22, 30, 200));
        g2.fillRect(15, getHeight() - 45, 80, 35);
        
        // Draw value text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g2.setColor(new Color(0, 255, 170));
        
        String valueStr = String.valueOf(value);
        // Add unit if needed
        if (title.contains("%")) {
            valueStr += "%";
        } else if (title.contains("bar")) {
            valueStr += " bar";
        }
        
        g2.drawString(valueStr, 20, getHeight() - 18);
    }
    
    private int scale(int value, int height) {
        if (maxY == minY) return height / 2;
        
        // Clamp value to range
        int clampedValue = Math.max(minY, Math.min(maxY, value));
        
        // Scale to chart height
        double ratio = (clampedValue - minY) / (double) (maxY - minY);
        return (int) (ratio * height);
    }
}
