package gui;

import javax.swing.*;
import java.awt.*;

public class EnergyApp extends JFrame {
    private EnergyController controller;
    private String authenticatedUser;

    public EnergyApp() {
        // Show login dialog before initializing the main application
        if (!showLoginDialog()) {
            // User failed to login or cancelled
            System.exit(0);
            return;
        }
        
        setTitle("Adaptive Energy Optimization System (AEOS) - User: " + authenticatedUser + " | Developed by Witschi B. Mihan on December 31, 2025");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        controller = new EnergyController(this);
        setContentPane(controller.getMainPanel());

        setVisible(true);
    }
    
    /**
     * Display login dialog and authenticate user
     * @return true if user successfully authenticates, false otherwise
     */
    private boolean showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);
        
        if (loginDialog.isAuthenticated()) {
            authenticatedUser = loginDialog.getAuthenticatedUsername();
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the currently authenticated user
     * @return The username of the authenticated user
     */
    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnergyApp());
    }
    
    // Suggested code changes incorporated as comments
    /*
    1. EMA Accuracy: new_acc = 0.2 * current + 0.8 * previous
    2. Learning Rate Decay: rate = 0.1 / (1 + 0.1 * epoch)
    3. Learning Curve: curve = 1 - exp(-epochs * 0.3)
    4. Correction Factor: 0-15% based on learned accuracy
    5. Error Influence Mapping: 0-10 error bits → 0-1.0 factor
    */
}
