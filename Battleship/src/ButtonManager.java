import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.border.Border;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: ButtonManager.
 * Manages custom buttons and border styles for Swing components.
 */
public class ButtonManager {
    private static Font VT323;
    static {
        try {
            VT323 = Font.createFont(Font.TRUETYPE_FONT, new File("VT323-Regular.ttf")).deriveFont(30f).deriveFont(Font.BOLD); // Adjust the font size as needed
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(VT323);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            VT323 = new Font("Serif", Font.BOLD, 20);
        }
    }
    /**
     * A custom Border implementation that creates a rounded border for Swing components.
     */
    public static class RoundedBorder implements Border {
        private int radius;
        private int thickness;
        RoundedBorder(int radius, int thickness) {
            this.radius = radius;
            this.thickness = thickness;
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        @Override
        // Paint a rounded button
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
    /**
     * A custom JButton implementation creating rounded buttons with specific hover effects and styles.
     */
    public static class RoundedButton extends JButton {
        private Color hoverBackgroundColor;
        private Color normalBackgroundColor;
        public void setBorderThickness(int thickness) {
            this.setBorder(new RoundedBorder(10, thickness));
        }
        public RoundedButton(String text) {
            // Customizing the button's appearance and action
            super(text);
            normalBackgroundColor = new Color(0, 255, 100); // Default background color
            setBorderThickness(1);
            hoverBackgroundColor = new Color(10, 139 - 50, 50 - 50); // Background color on mouse hover
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(VT323);

            // Adding mouse hover effects
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(hoverBackgroundColor);
                    setBorderThickness(10);
                    setFont(VT323);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(normalBackgroundColor);
                    setBorderThickness(10);
                    setFont(VT323);
                }
            });
        }
        /**
         * Creates a panel with custom buttons and specified actions.
         * @param strategyAction Action listener for the "STRATEGY" button.
         * @param playNowAction  Action listener for the "PLAY NOW" button.
         * @param exitAction     Action listener for the "EXIT" button.
         * @return JPanel containing custom buttons.
         */
        public static JPanel createButtonPanel(ActionListener strategyAction,
                                               ActionListener playNowAction,
                                               ActionListener exitAction) {
            JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            buttonPanel.setOpaque(false);

            // Adding custom buttons to the panel
            buttonPanel.add(createCustomButton("STRATEGY", strategyAction));
            buttonPanel.add(createCustomButton("PLAY NOW", playNowAction));
            buttonPanel.add(createCustomButton("EXIT", exitAction));

            return buttonPanel;
        }
        /**
         * Creates a custom JButton with customized appearance and mouse hover effects.
         * @param text   The text label to display on the button.
         * @param action The ActionListener to handle button click events.
         * @return A customized JButton with defined appearance and mouse hover effects.
         */
        private static JButton createCustomButton(String text, ActionListener action) {
            JButton button = new JButton(text);
            customizeButtonAppearance(button);
            button.addActionListener(action);

            // Mouse effects for hover in and hover out
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setFont(VT323);
                    button.setForeground(Color.YELLOW);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    customizeButtonAppearance(button);
                }
            });
            return button;
        }
        /**
         * Customizes the appearance of the provided JButton by setting various visual properties
         * such as transparency, border, text color, font, and mouse hover effects.
         * @param button The JButton to be visually customized.
         */
        private static void customizeButtonAppearance(JButton button) {
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setForeground(Color.WHITE);
            button.setFont(VT323);
            button.setFocusPainted(false);
            button.setBorder(new RoundedBorder(10, 1));

            // Mouse hover effects
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(100, 130, 160));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(120, 150, 180));
                }
            });
        }
        /**
         * Overrides the paintComponent method to customize the visual rendering of the JButton,
         * creating a gradient-filled round rectangle with a thicker border.
         * @param g The Graphics object used for painting.
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Create a thicker border stroke
            int borderThickness = 3; // Adjust the thickness as needed
            g2.setStroke(new BasicStroke(borderThickness));

            // Draw a gradient-filled round rectangle
            GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), getBackground());
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            // Draw the thicker border
            g2.setColor(new Color(0, 255, 100));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}

