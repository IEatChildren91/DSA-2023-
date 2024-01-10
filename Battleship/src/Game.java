import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: Game.
 * The Game class initializes the main frame and controls
 * the navigation between different panels representing
 * game states like difficulty selection and strategy display.
 */
public class Game implements KeyListener {
    /**
     * Entry point for the application to create an instance of the Game class.
     * @param args Not used.
     */
    public static void main(String[] args) {
        Game game = new Game();
    }
    /**
     * Reference to the GamePanel object to pass key events to.
     */
    private GamePanel gamePanel;
    /**
     * A frame to store the panels.
     */
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

        BackgroundPanel backgroundPanel = new BackgroundPanel("bgbts(1).jpg");
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setPreferredSize(new Dimension(1192 / 2, 705));

        JPanel buttonPanel = ButtonManager.RoundedButton.createButtonPanel(
                e -> showStrategyPanel(),
                e -> showDifficultyPanel(),
                e -> System.exit(0)
        );

        backgroundPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(backgroundPanel);
        frame.addKeyListener(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    /**
     * Starts the game by creating a new GamePanel based on the chosen difficulty level,
     * updating the frame content, setting up key inputs, and focusing on the game panel
     * to begin gameplay.
     * @param difficulty The chosen difficulty level for the game.
     */
    private void startGame(GameDifficulty difficulty) {
        // Creating a new game panel with the chosen difficulty
        gamePanel = new GamePanel(difficulty);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(gamePanel);

        // Accommodating the new panel
        frame.pack();
        frame.revalidate();
        frame.repaint();
        frame.setLocationRelativeTo(null);

        // Key input handling
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
        gamePanel.addKeyListener(this);
    }
    /**
     * Displays the strategy panel. This method sets up a new panel showing strategic information.
     */
    private void showStrategyPanel() {
        // Creating and configuring a background panel with a specific image
        BackgroundPanel strategyPanel = new BackgroundPanel("strate2.png");
        strategyPanel.setLayout(new BorderLayout());
        strategyPanel.setPreferredSize(new Dimension(600, 720));

        // Setting up a panel for the "START" button
        JPanel buttonStart = new JPanel(new GridLayout(3, 1, 5, 20)); //
        buttonStart.setOpaque(false); //

        // Print the strategy using animation method
        strategyPanel.drawTextOnPanel("Each player deploys his ships (of lengths varying from 2 to 5" +
                " squares) secretly on a square grid. Then each player shoots at " +
                "the other's grid by calling a location. The defender responds by" +
                " \"Hit!\" or \"Miss!\". You try to deduce where the enemy ships are " +
                "and sink them.", 1192/2 - 40);

        // Creating and adding a custom rounded button
        ButtonManager.RoundedButton backButton = new ButtonManager.RoundedButton("START");
        backButton.addActionListener(e -> showDifficultyPanel());
        buttonStart.add(backButton);
        backButton.setForeground(new Color(10, 139 - 50, 50 - 50));
        backButton.setPreferredSize(new Dimension(110, 50)); //

        // Wrapper panel for positioning the button
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(buttonStart); // Adding the button panel to the wrapper
        // Adding padding
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, -145, 0)); // Top, Left, Bottom, Right
        // Adding the wrapper panel to the strategy panel
        strategyPanel.add(wrapperPanel, BorderLayout.SOUTH);
        // Updating the main frame to display the strategy panel
        frame.getContentPane().removeAll();
        frame.getContentPane().add(strategyPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        if (gamePanel != null) {
            gamePanel.requestFocusInWindow();
        }
    }
    /**
     * Displays the difficulty selection panel. This method sets up a new panel for choosing game difficulty.
     */
    private void showDifficultyPanel() {
        // Creating a background panel with a specific image
        BackgroundPanel lvPanel = new BackgroundPanel("gif2.gif");
        lvPanel.setLayout(new BorderLayout());
        lvPanel.setPreferredSize(new Dimension(1192 / 2, 705));

        // Panel for difficulty selection buttons
        JPanel buttonDifficuly = new JPanel(new GridLayout(3, 1, 40, 60)); //
        buttonDifficuly.setOpaque(false); //

        // Creating and adding custom buttons for different difficulty levels
        ButtonManager.RoundedButton easyButton = new ButtonManager.RoundedButton("EASY");
        easyButton.addActionListener(e -> startGame(GameDifficulty.EASY));
        easyButton.setForeground(new Color(10, 139 - 50, 50 - 50));
        buttonDifficuly.add(easyButton);

        ButtonManager.RoundedButton mediumButton = new ButtonManager.RoundedButton("MEDIUM");
        mediumButton.addActionListener(e -> {
            startGame(GameDifficulty.MEDIUM);
        });
        mediumButton.setForeground(new Color(10, 139 - 50, 50 - 50));
        buttonDifficuly.add(mediumButton);

        ButtonManager.RoundedButton hardButton = new ButtonManager.RoundedButton("HARD");
        hardButton.addActionListener(e -> startGame(GameDifficulty.HARD));
        hardButton.setForeground(new Color(10, 139 - 50, 50 - 50));
        buttonDifficuly.add(hardButton);

        // Setting button sizes
        easyButton.setPreferredSize(new Dimension(110, 50)); //
        mediumButton.setPreferredSize(new Dimension(110, 50)); //
        hardButton.setPreferredSize(new Dimension(110, 50));

        // Wrapper panel for positioning the buttons
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(buttonDifficuly); //
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
     * Called when the key is pressed down. Passes the key press on to the
     * GamePanel.
     * @param e Information about what key was pressed.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode())); // For debugging
        gamePanel.handleInput(e.getKeyCode());
    }
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }
}
