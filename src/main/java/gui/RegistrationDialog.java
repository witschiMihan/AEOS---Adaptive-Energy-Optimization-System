package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * RegistrationDialog - User registration interface for the Smart Energy System
 * Allows new users to create an account
 */
public class RegistrationDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private boolean registered = false;
    private String registeredUsername;
    private String registeredPassword;
    
    public RegistrationDialog(Frame owner) {
        super(owner, "User Registration - AEOS", true);
        initializeUI();
        configureDialog();
    }
    
    /**
     * Initialize the registration UI components
     */
    private void initializeUI() {
        // Main panel with background watermark
        BackgroundPanel mainPanel = new BackgroundPanel("assets/AEOS_logo.png");
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setOpaque(true);
        
        // Header panel with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        JLabel subtitleLabel = new JLabel("Register for AEOS Access");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(240, 240, 240));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Registration form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(usernameField, gbc);
        
        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(passwordField, gbc);
        
        // Confirm Password label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel confirmPasswordLabel = new JLabel("Confirm:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(confirmPasswordField, gbc);
        
        // Password requirements info
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel requirementsLabel = new JLabel("<html><small>Password must be at least 6 characters long</small></html>");
        requirementsLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        requirementsLabel.setForeground(new Color(100, 100, 100));
        formPanel.add(requirementsLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setBackground(new Color(0, 153, 76));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setMargin(new Insets(8, 25, 8, 25));
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(e -> handleRegister());
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setMargin(new Insets(8, 25, 8, 25));
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> handleCancel());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Allow Enter key to trigger registration
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.addActionListener(e -> handleRegister());
    }
    
    /**
     * Configure dialog properties
     */
    private void configureDialog() {
        setSize(450, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Handle register button click
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        // Validate username
        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            usernameField.requestFocus();
            return;
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("Username can only contain letters, numbers, and underscores");
            usernameField.requestFocus();
            return;
        }
        
        // Check if username already exists
        if (LoginDialog.userExists(username)) {
            showError("Username already exists. Please choose a different username.");
            usernameField.setText("");
            usernameField.requestFocus();
            return;
        }
        
        // Validate password
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            passwordField.setText("");
            confirmPasswordField.setText("");
            passwordField.requestFocus();
            return;
        }
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match. Please try again.");
            passwordField.setText("");
            confirmPasswordField.setText("");
            passwordField.requestFocus();
            return;
        }
        
        // Register the user
        LoginDialog.addUser(username, password);
        registered = true;
        registeredUsername = username;
        registeredPassword = password;
        
        showSuccess("Registration successful! You can now login with your credentials.");
        dispose();
    }
    
    /**
     * Handle cancel button click
     */
    private void handleCancel() {
        dispose();
    }
    
    /**
     * Show error message dialog
     * @param message The error message to display
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Registration Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show success message dialog
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Registration Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Check if registration was successful
     * @return true if registration was successful, false otherwise
     */
    public boolean isRegistered() {
        return registered;
    }
    
    /**
     * Get the registered username
     * @return The username that was registered
     */
    public String getRegisteredUsername() {
        return registeredUsername;
    }
    
    /**
     * Get the registered password
     * @return The password that was registered
     */
    public String getRegisteredPassword() {
        return registeredPassword;
    }
}



