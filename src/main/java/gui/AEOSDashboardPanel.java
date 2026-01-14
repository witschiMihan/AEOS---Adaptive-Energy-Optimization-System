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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import model.EnergyRecord;
import service.EnergyAnalyzer;

/**
 * AEOS Animated Energy Flow Dashboard Panel
 * Professional energy system visualization with real-time data
 * Can be integrated into the main EnergyController
 */
public class AEOSDashboardPanel extends JPanel {
    
    private final List<AEOSEnergyParticle> particles = new ArrayList<>();
    private final List<EnergyNode> nodes = new ArrayList<>();
    private Timer animationTimer;
    private EnergyAnalyzer energyAnalyzer;
    private List<EnergyRecord> energyRecords;
    
    // KPI Data
    private double totalEnergy = 0.0;
    private double avgEfficiency = 94.2;
    private String systemStatus = "OPTIMIZING";
    private String carbonIndex = "Low";
    private String systemLoad = "Normal";
    
    // Animation state
    private float turbineRotation = 0f;
    private float pulsePhase = 0f;
    private Random random = new Random();
    
    public AEOSDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 15, 25));
        
        initializeNodes();
        initializeParticles();
        startAnimations();
        
        // Create UI components
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createEnergyFlowPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Set energy analyzer and records for real-time data
     */
    public void setEnergyData(EnergyAnalyzer analyzer, List<EnergyRecord> records) {
        this.energyAnalyzer = analyzer;
        this.energyRecords = records;
        updateKPIs();
    }
    
    /**
     * Update KPI values from real data
     */
    private void updateKPIs() {
        if (energyRecords != null && !energyRecords.isEmpty()) {
            totalEnergy = energyRecords.stream()
                .mapToDouble(EnergyRecord::getEnergyConsumption)
                .sum();
            
            if (energyAnalyzer != null) {
                avgEfficiency = energyAnalyzer.calculateAverageConsumption(energyRecords);
            }
            
            // Determine system status based on data
            if (totalEnergy > 10000) {
                systemLoad = "High";
                systemStatus = "MONITORING";
            } else if (totalEnergy > 5000) {
                systemLoad = "Medium";
                systemStatus = "OPTIMIZING";
            } else {
                systemLoad = "Normal";
                systemStatus = "OPTIMIZING";
            }
        }
    }
    
    private void initializeNodes() {
        nodes.add(new EnergyNode(200, 200, "Turbine", new Color(0, 170, 255)));
        nodes.add(new EnergyNode(200, 400, "Solar", new Color(255, 200, 0)));
        nodes.add(new EnergyNode(550, 300, "AEOS Core", new Color(0, 255, 150)));
        nodes.add(new EnergyNode(900, 200, "Rig Systems", new Color(255, 100, 100)));
        nodes.add(new EnergyNode(900, 400, "Data Center", new Color(150, 100, 255)));
    }
    
    private void initializeParticles() {
        // Turbine to Core
        for (int i = 0; i < 30; i++) {
            particles.add(new AEOSEnergyParticle(200, 200, 550, 300));
        }
        // Solar to Core
        for (int i = 0; i < 30; i++) {
            particles.add(new AEOSEnergyParticle(200, 400, 550, 300));
        }
        // Core to Rig Systems
        for (int i = 0; i < 20; i++) {
            particles.add(new AEOSEnergyParticle(550, 300, 900, 200));
        }
        // Core to Data Center
        for (int i = 0; i < 20; i++) {
            particles.add(new AEOSEnergyParticle(550, 300, 900, 400));
        }
    }
    
    private void startAnimations() {
        animationTimer = new Timer(30, e -> {
            // Update particles
            particles.forEach(AEOSEnergyParticle::move);
            
            // Update animations
            turbineRotation += 2f;
            if (turbineRotation > 360) turbineRotation = 0;
            
            pulsePhase += 0.05f;
            if (pulsePhase > Math.PI * 2) pulsePhase = 0;
            
            // Update KPIs periodically
            if (random.nextInt(100) == 0) {
                updateKPIs();
            }
            
            repaint();
        });
        animationTimer.start();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(100, 60));
        header.setBackground(new Color(15, 25, 40));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel title = new JLabel(" AEOS • Adaptive Energy Optimization System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel status = new JLabel("System Status: " + systemStatus + "   ");
        status.setForeground(new Color(0, 200, 120));
        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Update status periodically
        Timer statusTimer = new Timer(2000, e -> {
            status.setText("System Status: " + systemStatus + "   ");
            status.repaint();
        });
        statusTimer.start();
        
        header.add(title, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createEnergyFlowPanel() {
        JPanel flowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                drawGrid(g2);
                drawPipelines(g2);
                drawNodes(g2);
                
                // Draw particles
                for (AEOSEnergyParticle p : particles) {
                    p.draw(g2);
                }
            }
        };
        flowPanel.setBackground(new Color(10, 15, 25));
        flowPanel.setPreferredSize(new Dimension(1200, 500));
        return flowPanel;
    }
    
    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(30, 40, 60));
        for (int i = 0; i < getWidth(); i += 40) {
            g2.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 40) {
            g2.drawLine(0, i, getWidth(), i);
        }
    }
    
    private void drawPipelines(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(0, 120, 255, 180));
        
        // Turbine to Core
        g2.drawLine(200, 200, 550, 300);
        // Solar to Core
        g2.drawLine(200, 400, 550, 300);
        // Core to Rig Systems
        g2.drawLine(550, 300, 900, 200);
        // Core to Data Center
        g2.drawLine(550, 300, 900, 400);
    }
    
    private void drawNodes(Graphics2D g2) {
        for (EnergyNode node : nodes) {
            node.draw(g2, turbineRotation, pulsePhase);
        }
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new GridLayout(1, 5));
        statusPanel.setPreferredSize(new Dimension(100, 80));
        statusPanel.setBackground(new Color(18, 22, 30));
        statusPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        statusPanel.add(createKPIBox("AI Status", "Active", new Color(0, 200, 255)));
        statusPanel.add(createKPIBox("Efficiency", String.format("%.1f%%", avgEfficiency), new Color(0, 255, 150)));
        statusPanel.add(createKPIBox("Total Energy", String.format("%.0f kWh", totalEnergy), new Color(255, 200, 0)));
        statusPanel.add(createKPIBox("Carbon Index", carbonIndex, new Color(100, 200, 100)));
        statusPanel.add(createKPIBox("System Load", systemLoad, new Color(255, 150, 100)));
        
        // Update KPIs periodically
        Timer kpiTimer = new Timer(1000, e -> {
            updateKPIs();
            statusPanel.removeAll();
            statusPanel.add(createKPIBox("AI Status", "Active", new Color(0, 200, 255)));
            statusPanel.add(createKPIBox("Efficiency", String.format("%.1f%%", avgEfficiency), new Color(0, 255, 150)));
            statusPanel.add(createKPIBox("Total Energy", String.format("%.0f kWh", totalEnergy), new Color(255, 200, 0)));
            statusPanel.add(createKPIBox("Carbon Index", carbonIndex, new Color(100, 200, 100)));
            statusPanel.add(createKPIBox("System Load", systemLoad, new Color(255, 150, 100)));
            statusPanel.revalidate();
            statusPanel.repaint();
        });
        kpiTimer.start();
        
        return statusPanel;
    }
    
    private JPanel createKPIBox(String title, String value, Color valueColor) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(18, 22, 30));
        box.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        box.add(titleLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        
        return box;
    }
    
    /**
     * Stop animations when panel is removed
     */
    public void stopAnimations() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}

/* ================= ENERGY NODE ================= */

class EnergyNode {
    private int x, y;
    private String name;
    private Color color;
    private float pulseSize = 1.0f;
    
    public EnergyNode(int x, int y, String name, Color color) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.color = color;
    }
    
    public void draw(Graphics2D g2, float rotation, float pulsePhase) {
        // Pulsing effect
        pulseSize = 1.0f + (float) Math.sin(pulsePhase) * 0.2f;
        int size = (int) (40 * pulseSize);
        int offset = size / 2;
        
        // Outer glow
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.fillOval(x - offset - 5, y - offset - 5, size + 10, size + 10);
        
        // Main node
        g2.setColor(color);
        g2.fillOval(x - offset, y - offset, size, size);
        
        // Border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - offset, y - offset, size, size);
        
        // Rotation effect for turbine
        if (name.equals("Turbine")) {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(2));
            int r = size / 2;
            for (int i = 0; i < 4; i++) {
                double angle = Math.toRadians(rotation + i * 90);
                int x1 = x + (int) (r * Math.cos(angle));
                int y1 = y + (int) (r * Math.sin(angle));
                int x2 = x + (int) ((r + 10) * Math.cos(angle));
                int y2 = y + (int) ((r + 10) * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);
            }
        }
        
        // Label
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        int textWidth = g2.getFontMetrics().stringWidth(name);
        g2.drawString(name, x - textWidth / 2, y + offset + 20);
    }
}

/* ================= ENERGY PARTICLE ================= */

class AEOSEnergyParticle {
    private float x, y;
    private float startX, startY, endX, endY;
    private float progress = (float) Math.random();
    private float speed = 0.008f + (float) (Math.random() * 0.005f);
    
    public AEOSEnergyParticle(float sx, float sy, float ex, float ey) {
        startX = sx;
        startY = sy;
        endX = ex;
        endY = ey;
        updatePosition();
    }
    
    public void move() {
        progress += speed;
        if (progress > 1) progress = 0;
        updatePosition();
    }
    
    private void updatePosition() {
        x = startX + (endX - startX) * progress;
        y = startY + (endY - startY) * progress;
    }
    
    public void draw(Graphics2D g2) {
        // Glow effect
        g2.setColor(new Color(0, 255, 180, 100));
        g2.fillOval((int) x - 3, (int) y - 3, 12, 12);
        
        // Main particle
        g2.setColor(new Color(0, 255, 180));
        g2.fillOval((int) x, (int) y, 6, 6);
        
        // Bright center
        g2.setColor(Color.WHITE);
        g2.fillOval((int) x + 1, (int) y + 1, 4, 4);
    }
}

