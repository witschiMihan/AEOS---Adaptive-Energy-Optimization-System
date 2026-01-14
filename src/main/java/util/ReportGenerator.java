package util;

import model.EnergyRecord;
import model.Machine;
import service.EnergyAnalyzer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates comprehensive energy reports in multiple formats
 */
public class ReportGenerator {

    /**
     * Generate HTML report with formatted tables and statistics
     */
    public static void generateHTMLReport(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<title>AEOS Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1 { color: #333; }");
            writer.println("h2 { color: #666; border-bottom: 2px solid #0066CC; padding-bottom: 10px; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            writer.println("th { background-color: #0066CC; color: white; }");
            writer.println("tr:nth-child(even) { background-color: #f2f2f2; }");
            writer.println(".stat { display: inline-block; margin: 10px 20px; }");
            writer.println(".stat-value { font-size: 24px; font-weight: bold; color: #0066CC; }");
            writer.println(".stat-label { font-size: 14px; color: #666; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");

            // Header
            writer.println("<h1>AEOS Report</h1>");
            writer.println("<p>Generated: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");

            // Machines Table
            writer.println("<h2>Registered Machines</h2>");
            writer.println("<table>");
            writer.println("<tr><th>Machine ID</th><th>Name</th><th>Location</th><th>Status</th></tr>");
            for (Machine machine : machines) {
                writer.println("<tr>");
                writer.println("<td>" + machine.getMachineId() + "</td>");
                writer.println("<td>" + machine.getMachineName() + "</td>");
                writer.println("<td>" + machine.getLocation() + "</td>");
                writer.println("<td>" + (machine.isActive() ? "Active" : "Inactive") + "</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");

            // Energy Records Table
            writer.println("<h2>Energy Records (First 100)</h2>");
            writer.println("<table>");
            writer.println(
                    "<tr><th>Record ID</th><th>Machine ID</th><th>Energy (kWh)</th><th>Timestamp</th><th>Error Bits</th></tr>");
            int count = 0;
            for (EnergyRecord record : records) {
                if (count >= 100)
                    break;
                writer.println("<tr>");
                writer.println("<td>" + record.getRecordId() + "</td>");
                writer.println("<td>" + record.getMachineId() + "</td>");
                writer.println("<td>" + String.format("%.2f", record.getEnergyConsumption()) + "</td>");
                writer.println("<td>" + record.getTimestamp() + "</td>");
                writer.println("<td>" + record.getErrorBits() + "</td>");
                writer.println("</tr>");
                count++;
            }
            writer.println("</table>");

            // Footer
            writer.println("<hr>");
            writer.println("<p><small>AEOS (Adaptive Energy Optimization System) v1.0</small></p>");
            writer.println("</body>");
            writer.println("</html>");

            System.out.println("HTML report generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating HTML report: " + e.getMessage());
        }
    }

    /**
     * Generate JSON report with machine and energy record data
     */
    public static void generateJSONReport(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("{");
            writer.println("  \"report_generated\": \"" + LocalDateTime.now() + "\",");
            writer.println("  \"machines\": [");

            for (int i = 0; i < machines.size(); i++) {
                Machine m = machines.get(i);
                writer.println("    {");
                writer.println("      \"id\": \"" + m.getMachineId() + "\",");
                writer.println("      \"name\": \"" + m.getMachineName() + "\",");
                writer.println("      \"location\": \"" + m.getLocation() + "\",");
                writer.println("      \"active\": " + m.isActive());
                writer.println("    }" + (i < machines.size() - 1 ? "," : ""));
            }
            writer.println("  ],");
            writer.println("  \"energy_records\": [");

            int count = 0;
            for (int i = 0; i < records.size(); i++) {
                if (count >= 100)
                    break;
                EnergyRecord r = records.get(i);
                writer.println("    {");
                writer.println("      \"id\": \"" + r.getRecordId() + "\",");
                writer.println("      \"machine_id\": \"" + r.getMachineId() + "\",");
                writer.println("      \"consumption_kwh\": " + r.getEnergyConsumption() + ",");
                writer.println("      \"timestamp\": \"" + r.getTimestamp() + "\",");
                writer.println("      \"error_bits\": " + r.getErrorBits());
                writer.println("    }" + (i < records.size() - 1 && count < 99 ? "," : ""));
                count++;
            }
            writer.println("  ]");
            writer.println("}");

            System.out.println("JSON report generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating JSON report: " + e.getMessage());
        }
    }

    /**
     * Generate CSV summary report with statistics by machine
     */
    public static void generateCSVSummaryReport(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Summary header
            writer.println("AEOS Summary Report");
            writer.println("Generated," + LocalDateTime.now());
            writer.println();

            // Overall statistics
            writer.println("OVERALL STATISTICS");
            writer.println("Metric,Value");

            EnergyAnalyzer analyzer = new EnergyAnalyzer();
            writer.println("Average Consumption (kWh),"
                    + String.format("%.2f", analyzer.calculateAverageConsumption(records)));
            writer.println("Maximum Consumption (kWh)," + String.format("%.2f", analyzer.findMaxConsumption(records)));
            writer.println("Minimum Consumption (kWh)," + String.format("%.2f", analyzer.findMinConsumption(records)));
            writer.println(
                    "Total Consumption (kWh)," + String.format("%.2f", analyzer.calculateTotalConsumption(records)));
            writer.println(
                    "Standard Deviation (kWh)," + String.format("%.2f", analyzer.calculateStandardDeviation(records)));
            writer.println("Total Records," + records.size());
            writer.println();

            // Machine consumption
            writer.println("MACHINE SUMMARY");
            writer.println("Machine ID,Name,Location,Total Consumption (kWh)");
            for (Machine machine : machines) {
                double totalForMachine = 0;
                for (EnergyRecord record : records) {
                    if (record.getMachineId().equals(machine.getMachineId())) {
                        totalForMachine += record.getEnergyConsumption();
                    }
                }
                writer.println(machine.getMachineId() + "," + machine.getMachineName() + "," +
                        machine.getLocation() + "," + String.format("%.2f", totalForMachine));
            }

            System.out.println("CSV summary report generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating CSV report: " + e.getMessage());
        }
    }
    
    /**
     * Generate PDF report using plain text table formatting
     * (iText library alternative using text-based approach)
     */
    public static void generatePDFReport(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            
            // PDF Header (text representation)
            writer.println("================================================================================");
            writer.println("               AEOS REPORT");
            writer.println("================================================================================");
            writer.println();
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            
            // Summary Statistics Section
            writer.println("--------------------------------------------------------------------------------");
            writer.println("SUMMARY STATISTICS");
            writer.println("--------------------------------------------------------------------------------");
            writer.println();
            
            EnergyAnalyzer analyzer = new EnergyAnalyzer();
            double avgConsumption = analyzer.calculateAverageConsumption(records);
            double maxConsumption = analyzer.findMaxConsumption(records);
            double minConsumption = analyzer.findMinConsumption(records);
            double totalConsumption = analyzer.calculateTotalConsumption(records);
            double stdDeviation = analyzer.calculateStandardDeviation(records);
            
            writer.println("Total Records:                    " + records.size());
            writer.println("Total Energy Consumption:         " + String.format("%.2f kWh", totalConsumption));
            writer.println("Average Energy Consumption:       " + String.format("%.2f kWh", avgConsumption));
            writer.println("Maximum Energy Consumption:       " + String.format("%.2f kWh", maxConsumption));
            writer.println("Minimum Energy Consumption:       " + String.format("%.2f kWh", minConsumption));
            writer.println("Standard Deviation:               " + String.format("%.2f kWh", stdDeviation));
            writer.println();
            
            // Average Error Bits Analysis
            double avgErrorBits = records.stream()
                    .mapToLong(EnergyRecord::getErrorBits)
                    .average()
                    .orElse(0);
            long totalErrorBits = records.stream()
                    .mapToLong(EnergyRecord::getErrorBits)
                    .sum();
            
            writer.println("Total Error Bits Found:           " + totalErrorBits);
            writer.println("Average Error Bits per Record:    " + String.format("%.2f", avgErrorBits));
            writer.println();
            
            // Machines Summary Section
            writer.println("--------------------------------------------------------------------------------");
            writer.println("REGISTERED MACHINES SUMMARY");
            writer.println("--------------------------------------------------------------------------------");
            writer.println();
            
            String machineFormat = "%-10s %-25s %-30s %15s";
            writer.println(String.format(machineFormat, "Machine ID", "Machine Name", "Location", "Total Consumption"));
            writer.println("-".repeat(80));
            
            for (Machine machine : machines) {
                double totalForMachine = 0;
                long errorBitsForMachine = 0;
                int recordCount = 0;
                
                for (EnergyRecord record : records) {
                    if (record.getMachineId().equals(machine.getMachineId())) {
                        totalForMachine += record.getEnergyConsumption();
                        errorBitsForMachine += record.getErrorBits();
                        recordCount++;
                    }
                }
                
                writer.println(String.format(machineFormat, 
                        machine.getMachineId(), 
                        machine.getMachineName(), 
                        machine.getLocation(),
                        String.format("%.2f kWh", totalForMachine)));
                writer.println(String.format("  ├─ Records: %d, Errors: %d, Avg Error: %.2f bits", 
                        recordCount, errorBitsForMachine, recordCount > 0 ? (double)errorBitsForMachine / recordCount : 0));
                writer.println();
            }
            
            // Detailed Energy Records Section
            writer.println("--------------------------------------------------------------------------------");
            writer.println("DETAILED ENERGY RECORDS");
            writer.println("--------------------------------------------------------------------------------");
            writer.println();
            
            String recordFormat = "%-8s %-12s %15s %20s %12s";
            writer.println(String.format(recordFormat, "Record#", "Machine ID", "Consumption (kWh)", "Timestamp", "Error Bits"));
            writer.println("-".repeat(80));
            
            int recordNum = 1;
            for (EnergyRecord record : records) {
                String timestampStr = "N/A";
                if (record.getTimestamp() != null) {
                    String fullTimestamp = record.getTimestamp().toString();
                    // Extract first 19 chars (YYYY-MM-DD HH:MM:SS) if available, otherwise use full string
                    timestampStr = fullTimestamp.length() >= 19 ? fullTimestamp.substring(0, 19) : fullTimestamp;
                }
                writer.println(String.format(recordFormat,
                        "R" + recordNum++,
                        record.getMachineId(),
                        String.format("%.2f", record.getEnergyConsumption()),
                        timestampStr,
                        record.getErrorBits()));
            }
            
            writer.println();
            writer.println("--------------------------------------------------------------------------------");
            writer.println("REPORT GENERATION NOTES");
            writer.println("--------------------------------------------------------------------------------");
            writer.println("This report contains comprehensive energy consumption analysis including:");
            writer.println("- Overall system statistics and trends");
            writer.println("- Per-machine consumption and error analysis");
            writer.println("- Detailed record-level information");
            writer.println("- Statistical metrics for monitoring and optimization");
            writer.println();
            writer.println("Report Generated By: Energy Consumption & Bit Correction System");
            writer.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("Developed by Witschi B. Mihan, 2025, AI & ML Developer");
            writer.println("================================================================================");
            
            System.out.println("PDF report generated successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
        }
    }
    
    /**
     * Generate advanced PDF report with iText library (requires iText dependency)
     * Fallback method if iText library is available
     */
    public static void generatePDFReportAdvanced(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        // This method would use iText library if available
        // For now, we provide a text-based PDF alternative
        // To use real iText, add dependency: com.itextpdf:itextpdf:5.5.13
        
        try {
            // Attempt to use iText if available
            Class.forName("com.itextpdf.text.pdf.PdfWriter");
            Class.forName("com.itextpdf.text.Document");
            Class.forName("com.itextpdf.text.Paragraph");
            Class.forName("com.itextpdf.text.Table");
            
            // If classes are available, use iText generation
            System.out.println("iText library detected. Generating advanced PDF...");
            generateAdvancedPDFWithiText(filePath, machines, records);
        } catch (ClassNotFoundException e) {
            // iText not available, use text-based approach
            System.out.println("iText library not found. Using text-based PDF format...");
            generatePDFReport(filePath, machines, records);
        }
    }
    
    /**
     * Advanced PDF generation using iText library
     * Only called if iText is available
     */
    private static void generateAdvancedPDFWithiText(String filePath, List<Machine> machines, List<EnergyRecord> records) {
        try {
            // Use reflection to avoid compile-time dependency on iText
            Class<?> documentClass = Class.forName("com.itextpdf.text.Document");
            Class<?> pdfWriterClass = Class.forName("com.itextpdf.text.pdf.PdfWriter");
            
            // Create document instance
            Object document = documentClass.getDeclaredConstructor().newInstance();
            
            // Create PdfWriter
            pdfWriterClass.getDeclaredConstructor(documentClass, FileOutputStream.class)
                    .newInstance(document, new FileOutputStream(filePath));
            
            // Open document
            documentClass.getMethod("open").invoke(document);
            
            // Add title using reflection
            Class<?> paragraphClass = Class.forName("com.itextpdf.text.Paragraph");
            Object title = paragraphClass.getDeclaredConstructor(String.class).newInstance(
                    "ENERGY CONSUMPTION & BIT CORRECTION REPORT");
            documentClass.getMethod("add", Class.forName("com.itextpdf.text.Element")).invoke(document, title);
            
            // Add metadata
            Object timestamp = paragraphClass.getDeclaredConstructor(String.class).newInstance(
                    "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            documentClass.getMethod("add", Class.forName("com.itextpdf.text.Element")).invoke(document, timestamp);
            
            // Close document
            documentClass.getMethod("close").invoke(document);
            
            System.out.println("Advanced PDF report generated successfully: " + filePath);
        } catch (Exception e) {
            System.err.println("Error generating advanced PDF: " + e.getMessage());
            System.err.println("Falling back to text-based PDF format...");
            generatePDFReport(filePath, machines, records);
        }
    }
}

