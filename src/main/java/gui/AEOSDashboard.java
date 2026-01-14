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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * AEOS Animated Energy Flow Dashboard
 * Professional energy system visualization panel
 */
public class AEOSDashboard extends JFrame {

    public AEOSDashboard() {
        setTitle("AEOS Dashboard - Adaptive Energy Optimization System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderPanel(), BorderLayout.NORTH);
        add(new EnergyFlowPanel(), BorderLayout.CENTER);
        add(new StatusPanel(), BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AEOSDashboard().setVisible(true);
        });
    }
}

/* ================= HEADER ================= */

class HeaderPanel extends JPanel {
    public HeaderPanel() {
        setPreferredSize(new Dimension(100, 60));
        setBackground(new Color(15, 25, 40));
        setLayout(new BorderLayout());

        JLabel title = new JLabel(" AEOS • Adaptive Energy Optimization System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel status = new JLabel("System Status: OPTIMIZING   ");
        status.setForeground(new Color(0, 200, 120));
        status.setFont(new Font("Segoe UI", Font.BOLD, 13));

        add(title, BorderLayout.WEST);
        add(status, BorderLayout.EAST);
    }
}

/* ================= ENERGY FLOW ================= */

class EnergyFlowPanel extends JPanel {

    private final List<EnergyParticle> particles = new ArrayList<>();

    public EnergyFlowPanel() {
        setBackground(new Color(10, 15, 25));

        // create particles
        for (int i = 0; i < 80; i++) {
            particles.add(new EnergyParticle(200, 200, 900, 200));
            particles.add(new EnergyParticle(200, 400, 900, 400));
        }

        Timer timer = new Timer(30, e -> {
            particles.forEach(EnergyParticle::move);
            repaint();
        });
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawNodes(g2);
        drawPipelines(g2);

        for (EnergyParticle p : particles) {
            p.draw(g2);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(30, 40, 60));
        for (int i = 0; i < getWidth(); i += 40)
            g2.drawLine(i, 0, i, getHeight());
        for (int i = 0; i < getHeight(); i += 40)
            g2.drawLine(0, i, getWidth(), i);
    }

    private void drawNodes(Graphics2D g2) {
        drawNode(g2, 200, 200, "Turbine");
        drawNode(g2, 200, 400, "Solar");
        drawNode(g2, 550, 300, "AEOS Core");
        drawNode(g2, 900, 200, "Rig Systems");
        drawNode(g2, 900, 400, "Data Center");
    }

    private void drawPipelines(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(0, 120, 255));

        g2.drawLine(200, 200, 550, 300);
        g2.drawLine(200, 400, 550, 300);
        g2.drawLine(550, 300, 900, 200);
        g2.drawLine(550, 300, 900, 400);
    }

    private void drawNode(Graphics2D g2, int x, int y, String name) {
        g2.setColor(new Color(0, 170, 255));
        g2.fillOval(x - 20, y - 20, 40, 40);
        g2.setColor(Color.WHITE);
        g2.drawOval(x - 20, y - 20, 40, 40);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(name, x - 35, y + 35);
    }
}

/* ================= PARTICLES ================= */

class EnergyParticle {

    float x, y;
    float startX, startY, endX, endY;
    float progress = (float) Math.random();

    public EnergyParticle(float sx, float sy, float ex, float ey) {
        startX = sx;
        startY = sy;
        endX = ex;
        endY = ey;
        updatePosition();
    }

    public void move() {
        progress += 0.01;
        if (progress > 1) progress = 0;
        updatePosition();
    }

    private void updatePosition() {
        x = startX + (endX - startX) * progress;
        y = startY + (endY - startY) * progress;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 180));
        g2.fillOval((int) x, (int) y, 6, 6);
    }
}

/* ================= STATUS BAR ================= */

class StatusPanel extends JPanel {
    public StatusPanel() {
        setPreferredSize(new Dimension(100, 60));
        setBackground(new Color(18, 22, 30));
        setLayout(new GridLayout(1, 4));

        add(createBox("AI Status", "Active"));
        add(createBox("Efficiency", "94.2%"));
        add(createBox("Carbon Index", "Low"));
        add(createBox("System Load", "Normal"));
    }

    private JPanel createBox(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(18, 22, 30));

        JLabel t = new JLabel(title, JLabel.CENTER);
        t.setForeground(Color.GRAY);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel v = new JLabel(value, JLabel.CENTER);
        v.setForeground(new Color(0, 200, 255));
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
