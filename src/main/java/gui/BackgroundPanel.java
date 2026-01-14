package gui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * A JPanel that renders a watermark-style background image
 */
public class BackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;
    private float transparency = 0.15f; // 15% opacity for watermark effect
    
    public BackgroundPanel() {
        setLayout(null);
        loadBackgroundImage();
    }
    
    public BackgroundPanel(String imagePath) {
        setLayout(null);
        loadBackgroundImage(imagePath);
    }
    
    /**
     * Load background image from default assets folder
     */
    private void loadBackgroundImage() {
        loadBackgroundImage("assets/AEOS_logo.png");
    }
    
    /**
     * Load background image from specified path
     */
    private void loadBackgroundImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                System.out.println("Background image not found: " + imagePath);
            }
        } catch (IOException e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }
    }
    
    /**
     * Set the transparency level (0.0f to 1.0f)
     */
    public void setTransparency(float transparency) {
        this.transparency = Math.max(0.0f, Math.min(1.0f, transparency));
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Only draw background image if it's loaded
        if (backgroundImage == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
            
            // Set rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Center the image and scale it
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = backgroundImage.getWidth();
            int imgHeight = backgroundImage.getHeight();
            
            // Calculate scaling to fit the panel while maintaining aspect ratio
            double scale = Math.max(
                (double) panelWidth / imgWidth,
                (double) panelHeight / imgHeight
            ) * 1.2; // 20% larger for better watermark effect
            
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            
            // Center the image
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;
            
            // Apply transparency
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            
            // Draw the scaled image
            g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, this);
            
            g2d.dispose();
    }
}