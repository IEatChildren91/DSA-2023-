import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class Game implements KeyListener {
    /**
     * Entry point for the application to create an instance of the Game class.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        Game game = new Game();
    }


    /**
     * Reference to the GamePanel object to pass key events to.
     */
    private GamePanel gamePanel;
    private JFrame frame;

    /**
     * Enum for representing game difficulty levels.
     */
    public enum GameDifficulty {
        EASY, MEDIUM, HARD
    }


    /**
     * Creates the JFrame with a GamePanel inside it, attaches a key listener,
     * and makes everything visible.
     */
    public Game() {

        frame = new JFrame("BATTLE SHIP");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);


        // Setup for the background panel with a specified image
        BackgroundPanel backgroundPanel = new BackgroundPanel(
                "DSA-Project-main\\bgbts(1).jpg");
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setPreferredSize(new Dimension(1192 / 2, 705));

        // Creating a panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 hàng, 1 cột
        buttonPanel.setOpaque(false); // Making the panel transparent
        // Adding custom buttons to the button panel
        buttonPanel.add(createCustomButton("STRATEGY", e -> showStrategyPanel()));
        buttonPanel.add(createCustomButton("PLAY NOW", e -> showDifficultyPanel()));
        buttonPanel.add(createCustomButton("EXIT", e -> System.exit(0)));
        // Adding the button panel to the background panel
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Initializing the game panel
        // gamePanel = new GamePanel(Game.GameDifficulty.EASY);
        // gamePanel.setFocusable(true);
        frame.getContentPane().add(backgroundPanel);
        frame.addKeyListener(this);// Adding key listener for keyboard input
        frame.pack();// Packing the frame to arrange its components
        frame.setLocationRelativeTo(null);// Centering the frame again after packing
        frame.setVisible(true);        
        // gamePanel.requestFocusInWindow();

    }

    /**
     * Method to create a custom JButton with specific text and action.
     * @param text The text to be displayed on the button.
     * @param action The action to be performed when the button is clicked.
     * @return A customized JButton.
     */
    private JButton createCustomButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        customizeButtonAppearance(button);
        button.addActionListener(action);

        // Adding mouse effects for hover in and hover out
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setFont(new Font("Arial", Font.BOLD, 20)); // Font to hơn
                button.setBorder(BorderFactory.createLineBorder(Color.YELLOW)); // Viền màu khác
                button.setForeground(Color.YELLOW); 
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                customizeButtonAppearance(button); 
            }
        });

        return button;
    }

    /**
     * Method to customize the appearance of a JButton.
     * @param button The JButton to be customized.
     */
    private void customizeButtonAppearance(JButton button) {
        button.setOpaque(false); 
        button.setContentAreaFilled(false); 
        button.setBorderPainted(false); 
        button.setForeground(Color.WHITE); 
        button.setFont(new Font("Game Font", Font.BOLD, 16)); // Font 
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(10,1)); // Round border

        // Adding mouse hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 130, 160)); // Lighter color
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(120, 150, 180)); // Original color
            }
        });
    }


    /**
     * Custom border class with rounded corners. Implements the Border interface.
     */
    class RoundedBorder implements Border {
        private int radius;
        private int thickness; // Thickness of the border

        RoundedBorder(int radius, int thickness) {
            this.radius = radius;
            this.thickness = thickness; // Setting the border thickness
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
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(thickness)); // Applying the set thickness
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    /**
     * Custom JButton class with a rounded appearance and dynamic background color change on mouse hover.
     */
    class RoundedButton extends JButton {
        private Color hoverBackgroundColor;
        private Color normalBackgroundColor;

        public void setBorderThickness(int thickness) {
            this.setBorder(new RoundedBorder(10, thickness));
        }

        public RoundedButton(String text) {
            super(text);
            normalBackgroundColor = new Color(130,0,0); // Default background color
            setBorderThickness(1);
            hoverBackgroundColor = new Color(200, 0, 0); // Background color on mouse hover
            setBorderPainted(false); 
            setFocusPainted(false); 
            setContentAreaFilled(false); 
            setOpaque(false); 
            setFont(new Font("Game Font", Font.BOLD, 16));

            // Adding mouse hover effects
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(hoverBackgroundColor);
                    setBorderThickness(5); 
                    setFont(new Font("Game Font", Font.BOLD, 18)); 

                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(normalBackgroundColor);
                    setBorderThickness(1); 
                    setFont(new Font("Game Font", Font.BOLD, 16)); 

                    
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Tạo gradient
            GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, getHeight(), getBackground());
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            // Vẽ viền
            g2.setColor(Color.ORANGE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /**
     * Displays the strategy panel. This method sets up a new panel showing strategic information.
     */
    private void showStrategyPanel() {
        // Creating and configuring a background panel with a specific image
        BackgroundPanel strategyPanel = new BackgroundPanel(
                "DSA-Project-main\\strate.png");
        strategyPanel.setLayout(new BorderLayout());
        strategyPanel.setPreferredSize(new Dimension(1192 / 2, 705));

        // Setting up a panel for the "START" button
        JPanel buttonStart = new JPanel(new GridLayout(3, 1, 5, 20)); // 3 hàng, 1 cột, khoảng cách giữa các hàng và
        buttonStart.setOpaque(false); // Làm cho panel trong suốt

        // Creating and adding a custom rounded button
        RoundedButton backButton = new RoundedButton("START");
        backButton.addActionListener(e -> showDifficultyPanel());
        buttonStart.add(backButton);

        backButton.setPreferredSize(new Dimension(120, 55)); // Chiều rộng 150, chiều cao 40

        // Wrapper panel for positioning the button
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(buttonStart); // Adding the button panel to the wrapper
        // Adding padding
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 0)); // Top, Left, Bottom, Right

        // Adding the wrapper panel to the strategy panel
        strategyPanel.add(wrapperPanel, BorderLayout.SOUTH);

        // Updating the main frame to display the strategy panel
        frame.getContentPane().removeAll();
        frame.getContentPane().add(strategyPanel); 
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    /**
     * Displays the difficulty selection panel. This method sets up a new panel for choosing game difficulty.
     */
    private void showDifficultyPanel() {
        // Creating a background panel with a specific image
        BackgroundPanel lvPanel = new BackgroundPanel("DSA-Project-main\\lvl(gif2).gif");
        lvPanel.setLayout(new BorderLayout());
        lvPanel.setPreferredSize(new Dimension(1192 / 2, 705));

        // Panel for difficulty selection buttons
        JPanel buttonDifficuly = new JPanel(new GridLayout(3, 1, 5, 22)); // 3 hàng, 1 cột, khoảng cách giữa các hàng và
        buttonDifficuly.setOpaque(false); // Làm cho panel trong suốt

        // Creating and adding custom buttons for different difficulty levels
        RoundedButton easyButton = new RoundedButton("EASY");
        easyButton.addActionListener(e -> startGame(GameDifficulty.EASY));
        buttonDifficuly.add(easyButton);

        RoundedButton mediumButton = new RoundedButton("MEDIUM");
        mediumButton.addActionListener(e -> {
            startGame(GameDifficulty.MEDIUM);
        });
        buttonDifficuly.add(mediumButton);

        RoundedButton hardButton = new RoundedButton("HARD");
        hardButton.addActionListener(e -> startGame(GameDifficulty.HARD));
        buttonDifficuly.add(hardButton);

        // Setting button sizes
        easyButton.setPreferredSize(new Dimension(120, 55)); // Chiều rộng 150, chiều cao 40
        mediumButton.setPreferredSize(new Dimension(120, 55)); // Tương tự
        hardButton.setPreferredSize(new Dimension(120, 55));

        // Wrapper panel for positioning the buttons
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(buttonDifficuly); // Thêm buttonDifficuly vào wrapperPanel
        // Adding padding
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 6, 0)); // Top, Left, Bottom, Right

        // Adding the wrapper panel to the main background panel
        lvPanel.add(wrapperPanel, BorderLayout.SOUTH);

        // Updating the main frame to display the difficulty selection panel
        frame.getContentPane().removeAll();
        frame.getContentPane().add(lvPanel); 
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Starts the game with the selected difficulty.
     * @param difficulty The difficulty level selected for the game.
     */
    private void startGame(GameDifficulty difficulty) {
        // Creating a new game panel with the chosen difficulty
        gamePanel = new GamePanel(difficulty);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(gamePanel);

        frame.pack();
        frame.revalidate();
        frame.repaint();
        frame.setLocation(200, 50);

        // Key input
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
        gamePanel.addKeyListener(this);


    }

    /**
     * Called when the key is pressed down. Passes the key press on to the
     * GamePanel.
     *
     * @param e Information about what key was pressed.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode())); // For debugging
        gamePanel.handleInput(e.getKeyCode());
    }

    

    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }
}
