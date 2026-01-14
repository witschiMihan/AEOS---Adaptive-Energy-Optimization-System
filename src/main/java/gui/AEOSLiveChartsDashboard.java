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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * AEOS Live Charts Dashboard
 * Real-time monitoring for Load, Pressure, Efficiency
 */
public class AEOSLiveChartsDashboard extends JFrame {

    public AEOSLiveChartsDashboard() {
        setTitle("AEOS Live Monitoring - Energy Optimization System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new ChartsHeader(), BorderLayout.NORTH);

        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        chartsPanel.setBackground(new Color(10, 15, 25));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        chartsPanel.add(new LiveChartPanel("SYSTEM LOAD (%)", 0, 100));
        chartsPanel.add(new LiveChartPanel("PIPE PRESSURE (bar)", 80, 200));
        chartsPanel.add(new LiveChartPanel("ENERGY EFFICIENCY (%)", 70, 100));

        add(chartsPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AEOSLiveChartsDashboard().setVisible(true));
    }
}

/* ================= HEADER ================= */

class ChartsHeader extends JPanel {

    public ChartsHeader() {
        setPreferredSize(new Dimension(100, 60));
        setBackground(new Color(15, 25, 40));
        setLayout(new BorderLayout());

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

class LiveChartPanel extends JPanel {

    private final String title;
    private final int minY, maxY;
    private final LinkedList<Integer> data = new LinkedList<>();
    private final Random rand = new Random();

    public LiveChartPanel(String title, int minY, int maxY) {
        this.title = title;
        this.minY = minY;
        this.maxY = maxY;

        setBackground(new Color(18, 22, 30));
        setBorder(BorderFactory.createLineBorder(new Color(0, 120, 255), 2));

        for (int i = 0; i < 50; i++) {
            data.add(generateValue());
        }

        Timer timer = new Timer(800, e -> {
            if (data.size() > 60) data.removeFirst();
            data.add(generateValue());
            repaint();
        });
        timer.start();
    }

    private int generateValue() {
        int base = (minY + maxY) / 2;
        int variation = rand.nextInt((maxY - minY) / 5);
        return base + (rand.nextBoolean() ? variation : -variation);
    }

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
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString(title, 15, 25);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(50, 60, 80));
        for (int i = 50; i < getHeight(); i += 50)
            g2.drawLine(0, i, getWidth(), i);

        for (int i = 50; i < getWidth(); i += 80)
            g2.drawLine(i, 40, i, getHeight());
    }

    private void drawLineChart(Graphics2D g2) {
        g2.setColor(new Color(0, 200, 255));
        g2.setStroke(new BasicStroke(2.5f));

        int w = getWidth() - 40;
        int h = getHeight() - 80;
        int startX = 20;
        int startY = 50;

        for (int i = 0; i < data.size() - 1; i++) {
            int x1 = startX + (i * w / data.size());
            int x2 = startX + ((i + 1) * w / data.size());

            int y1 = startY + h - scale(data.get(i), h);
            int y2 = startY + h - scale(data.get(i + 1), h);

            g2.drawLine(x1, y1, x2, y2);
            g2.fillOval(x2 - 3, y2 - 3, 6, 6);
        }
    }

    private void drawCurrentValue(Graphics2D g2) {
        int value = data.getLast();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        g2.setColor(new Color(0, 255, 170));
        g2.drawString(value + "", 20, getHeight() - 20);
    }

    private int scale(int value, int height) {
        return (int) ((value - minY) / (double) (maxY - minY) * height);
    }
}
