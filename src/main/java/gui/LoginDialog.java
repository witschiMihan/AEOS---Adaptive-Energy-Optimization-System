package gui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

/**
 * LoginDialog - Professional login interface for AEOS
 * Adaptive Energy Optimization System
 * Developed for RigVisionX Technology concept
 */
public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;
    private boolean authenticated = false;

    // In production, replace with database / secure auth service
    private static final Map<String, String> USER_CREDENTIALS = new HashMap<>();

    static {
        // Default demo credentials
        USER_CREDENTIALS.put("admin", "admin123");
        USER_CREDENTIALS.put("operator", "operator123");
        USER_CREDENTIALS.put("user", "user123");
    }

    public LoginDialog(Frame owner) {
        super(owner, "AEOS - Adaptive Energy Optimization System", true);
        initializeUI();
        configureDialog();
    }

    /* ========================= UI DESIGN ========================= */

    private void initializeUI() {

        // === MAIN BACKGROUND ===
        BackgroundPanel mainPanel = new BackgroundPanel("assets/AEOS_logo.png");
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(28, 32, 38)); // dark energy-tech background

        // === LOGIN CARD ===
        JPanel cardPanel = new JPanel();
        cardPanel.setPreferredSize(new Dimension(420, 320));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 102, 204), 2, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // === HEADER ===
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("AEOS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Adaptive Energy Optimization System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(90, 90, 90));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(subtitleLabel);

        // === FORM ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        usernameField = new JTextField();
        styleField(usernameField);

        passwordField = new JPasswordField();
        styleField(passwordField);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        formPanel.add(passLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        JLabel infoLabel = new JLabel("New user? Click Register to create an account");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        infoLabel.setForeground(new Color(120, 120, 120));
        infoLabel.setHorizontalAlignment(JLabel.CENTER);

        gbc.gridy = 4;
        gbc.insets = new Insets(12, 5, 0, 5);
        formPanel.add(infoLabel, gbc);

        // === BUTTONS ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttonPanel.setBackground(Color.WHITE);

        registerButton = createButton("Register", new Color(0, 153, 76));
        loginButton = createButton("Login", new Color(0, 102, 204));
        exitButton = createButton("Exit", new Color(200, 200, 200));
        exitButton.setForeground(Color.BLACK);

        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        // === FOOTER ===
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(new Color(245, 246, 248));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Optional separator line
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        footerPanel.add(separator);
        footerPanel.add(Box.createVerticalStrut(3));

        // First line: trademark / subsidiary
        JLabel line1 = new JLabel("AEOS © 2025 • Subsidiary of RigVisionX Technology");
        line1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        line1.setForeground(new Color(120, 120, 120));
        line1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Second line: clickable links
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        linksPanel.setBackground(new Color(245, 246, 248));

        String githubURL = "https://github.com/witschiMihan";

        linksPanel.add(createLinkLabel("Terms Of Use", githubURL));
        linksPanel.add(createLinkLabel("Privacy Notice", githubURL));
        linksPanel.add(createLinkLabel("Opt-Out", githubURL));
        linksPanel.add(createLinkLabel("Go to RigVisionX", githubURL));

        footerPanel.add(line1);
        footerPanel.add(Box.createVerticalStrut(2));
        footerPanel.add(linksPanel);

        // === ASSEMBLE CARD ===
        cardPanel.add(headerPanel, BorderLayout.NORTH);
        cardPanel.add(formPanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Color.WHITE);
        south.add(buttonPanel, BorderLayout.CENTER);
        south.add(footerPanel, BorderLayout.SOUTH);

        cardPanel.add(south, BorderLayout.SOUTH);

        mainPanel.add(cardPanel);
        setContentPane(mainPanel);

        // === ACTIONS ===
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
        exitButton.addActionListener(e -> handleExit());

        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createLinkLabel(String text, String url) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        label.setForeground(new Color(0, 102, 204));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openWebpage(url);
            }
        });
        return label;
    }

    private void openWebpage(String uri) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureDialog() {
        setSize(520, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleExit();
            }
        });
    }

    /* ========================= LOGIC ========================= */

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password)) {
            authenticated = true;
            JOptionPane.showMessageDialog(this, "Welcome to AEOS, " + username);
            dispose();
        } else {
            showError("Invalid username or password.");
            passwordField.setText("");
        }
    }

    private void handleRegister() {
        RegistrationDialog dialog = new RegistrationDialog((Frame) getOwner());
        dialog.setVisible(true);

        if (dialog.isRegistered()) {
            usernameField.setText(dialog.getRegisteredUsername());
            passwordField.setText("");
            passwordField.requestFocus();
            JOptionPane.showMessageDialog(this,
                    "Registration successful! Please login with your new credentials.",
                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExit() {
        int result = JOptionPane.showConfirmDialog(this,
                "Exit AEOS system?", "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private boolean authenticateUser(String user, String pass) {
        return USER_CREDENTIALS.containsKey(user) &&
                USER_CREDENTIALS.get(user).equals(pass);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "AEOS Security", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthenticatedUsername() {
        return authenticated ? usernameField.getText().trim() : null;
    }

    public static void addUser(String u, String p) {
        USER_CREDENTIALS.put(u, p);
    }

    public static boolean userExists(String username) {
        return USER_CREDENTIALS.containsKey(username);
    }

    public static boolean changePassword(String username, String newPassword) {
        if (USER_CREDENTIALS.containsKey(username)) {
            USER_CREDENTIALS.put(username, newPassword);
            return true;
        }
        return false;
    }
}
