package util;

import model.EnergyRecord;
import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Generates line charts for energy consumption visualization
 */
public class ChartGenerator {

    /**
     * Create a simple line chart panel for energy vs time
     * @param records list of energy records
     * @return JPanel containing the chart
     */
    public static JPanel createEnergyChart(List<EnergyRecord> records) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (records == null || records.isEmpty()) {
                    g2.drawString("No data available", 50, 50);
                    return;
                }

                int width = getWidth();
                int height = getHeight();
                int margin = 50;

                // Draw axes
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
                g2.drawLine(margin, margin, margin, height - margin); // Y-axis

                // Find min and max energy values
                double minEnergy = Double.MAX_VALUE;
                double maxEnergy = 0;
                for (EnergyRecord record : records) {
                    minEnergy = Math.min(minEnergy, record.getEnergyConsumption());
                    maxEnergy = Math.max(maxEnergy, record.getEnergyConsumption());
                }

                double energyRange = maxEnergy - minEnergy;
                if (energyRange == 0) energyRange = 1;

                // Plot points and connect with lines
                int chartWidth = width - 2 * margin;
                int chartHeight = height - 2 * margin;

                g2.setColor(new Color(0, 102, 204));
                g2.setStroke(new BasicStroke(2));

                for (int i = 0; i < records.size() - 1; i++) {
                    int x1 = margin + (int) (chartWidth * i / (records.size() - 1));
                    int y1 = height - margin - (int) (chartHeight * (records.get(i).getEnergyConsumption() - minEnergy) / energyRange);

                    int x2 = margin + (int) (chartWidth * (i + 1) / (records.size() - 1));
                    int y2 = height - margin - (int) (chartHeight * (records.get(i + 1).getEnergyConsumption() - minEnergy) / energyRange);

                    g2.drawLine(x1, y1, x2, y2);
                    g2.fillOval(x1 - 3, y1 - 3, 6, 6);
                }

                // Draw last point
                if (!records.isEmpty()) {
                    int lastIdx = records.size() - 1;
                    int x = margin + (int) (chartWidth * lastIdx / (records.size() - 1));
                    int y = height - margin - (int) (chartHeight * (records.get(lastIdx).getEnergyConsumption() - minEnergy) / energyRange);
                    g2.fillOval(x - 3, y - 3, 6, 6);
                }

                // Draw axis labels
                g2.setColor(Color.BLACK);
                g2.drawString("Time →", width - margin - 20, height - margin + 25);
                g2.rotate(-Math.PI / 2);
                g2.drawString("Energy (kWh) →", -height / 2, 15);
                g2.rotate(Math.PI / 2);

                // Draw grid lines and labels for Y-axis
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1));
                for (int i = 0; i <= 5; i++) {
                    int y = height - margin - (chartHeight * i / 5);
                    g2.drawLine(margin, y, width - margin, y);
                    double value = minEnergy + (energyRange * i / 5);
                    g2.setColor(Color.BLACK);
                    g2.drawString(String.format("%.0f", value), 5, y + 4);
                    g2.setColor(new Color(200, 200, 200));
                }
            }
        };

        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        return chartPanel;
    }

    /**
     * Create a multi-machine comparison chart
     * @param records list of energy records
     * @param machineIds array of machine IDs
     * @return JPanel containing the comparison chart
     */
    public static JPanel createMultiMachineChart(List<EnergyRecord> records, String[] machineIds) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (records == null || records.isEmpty()) {
                    g2.setColor(Color.BLACK);
                    g2.drawString("No data available", 50, 50);
                    return;
                }

                int width = getWidth();
                int height = getHeight();
                int margin = 60;

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(margin, height - margin, width - margin, height - margin);
                g2.drawLine(margin, margin, margin, height - margin);

                // Calculate bar positions
                int numMachines = machineIds.length;
                int chartWidth = width - 2 * margin;
                int barWidth = chartWidth / Math.max(numMachines, 1);
                int barSpacing = 5;

                // Group records by machine and calculate total consumption
                double maxConsumption = 0;
                for (String machineId : machineIds) {
                    double total = 0;
                    for (EnergyRecord record : records) {
                        if (record.getMachineId().equals(machineId)) {
                            total += record.getEnergyConsumption();
                        }
                    }
                    maxConsumption = Math.max(maxConsumption, total);
                }

                if (maxConsumption == 0) maxConsumption = 1;

                // Draw bars
                Color[] colors = {new Color(0, 102, 204), new Color(255, 102, 0), new Color(51, 204, 51), 
                                 new Color(204, 0, 102), new Color(102, 102, 0)};
                int chartHeight = height - 2 * margin;

                for (int i = 0; i < numMachines; i++) {
                    double total = 0;
                    for (EnergyRecord record : records) {
                        if (record.getMachineId().equals(machineIds[i])) {
                            total += record.getEnergyConsumption();
                        }
                    }

                    int barHeight = (int) (chartHeight * total / maxConsumption);
                    int x = margin + i * barWidth + barSpacing;
                    int y = height - margin - barHeight;

                    g2.setColor(colors[i % colors.length]);
                    g2.fillRect(x, y, barWidth - 2 * barSpacing, barHeight);

                    g2.setColor(Color.BLACK);
                    g2.drawString(machineIds[i], x + 5, height - margin + 20);
                    g2.drawString(String.format("%.0f", total), x + 5, y - 5);
                }
            }
        };

        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        return chartPanel;
    }
}
