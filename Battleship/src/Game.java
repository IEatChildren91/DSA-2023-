import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

    /**
     * Creates the JFrame with a GamePanel inside it, attaches a key listener,
     * and makes everything visible.
     */
    public Game() {
        // Choose Level Difficulty
        String[] options = new String[] {"Easy", "Medium", "Hard"};
        Font font = new Font("Garamond", Font.BOLD, 30);
        UIManager.put("OptionPane.messageFont", font);
        String message = "The Easy mode will have the AI making random moves.\nIn Medium level, the AI will concentrate on areas where it detects ships."
                + "\nIn Hard level, the AI will make more intelligent decisions compared to Medium.";
        int difficultyChoice = JOptionPane.showOptionDialog(null, message,
                "Choose Level Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        JFrame frame = new JFrame("BATTLESHIP   ");
        frame.setLocation(560,0);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        gamePanel = new GamePanel(difficultyChoice);
        frame.getContentPane().add(gamePanel);

        frame.addKeyListener(this);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Called when the key is pressed down. Passes the key press on to the GamePanel.
     *
     * @param e Information about what key was pressed.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        gamePanel.handleInput(e.getKeyCode());
    }

    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void keyTyped(KeyEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void keyReleased(KeyEvent e) {}
}
