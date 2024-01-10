import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: BackgroundPanel.
 * A custom JPanel designed to display an image as a
 * background and animate text on top of it.
 */
public class BackgroundPanel extends JPanel {
    /**
     * Image of the background.
     */
    private Image backgroundImage;
    /**
     * The font of the text.
     */
    private Font VT323;
    /**
     * A list to store the text to draw on the panel.
     */
    private List<String> textToDraw;
    /**
     * Lines to show on the panel.
     */
    private int linesToShow;
    /**
     * Lines counting variable.
     */
    private int linesCount;
    /**
     * A timer to set for the animation of the text on the panel.
     */
    private Timer animationTimer;
    /**
     * Constructs a BackgroundPanel with the provided image path as a background.
     * @param imagePath The file path of the image to be used as the background.
     */
    public BackgroundPanel(String imagePath) {
        setOpaque(false);
        try {
            backgroundImage = Toolkit.getDefaultToolkit().getImage(imagePath);
            VT323 = Font.createFont(Font.TRUETYPE_FONT, new File("VT323-Regular.ttf")).deriveFont(20f).deriveFont(Font.BOLD);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(VT323);
        } catch (Exception e) {
            e.printStackTrace();
        }
        linesCount = 0;
        linesToShow = 0;
        animationTimer = new Timer(700 , e -> {
            if (linesCount < textToDraw.size()) {
                linesToShow++;
                repaint();
            } else {
                animationTimer.stop();
            }
        });
    }
    /**
     * Initiates the text animation on the panel.
     */
    public void startTextAnimation() {
        linesCount = 0;
        linesToShow = 0;
        animationTimer.start();
    }
    /**
     * Prepares and displays text on the panel within the specified maximum width.
     * @param text     The text to be displayed on the panel.
     * @param maxWidth The maximum width of the text on the panel.
     */
    public void drawTextOnPanel(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return;
        }
        this.textToDraw = splitText(text, maxWidth);
        startTextAnimation();
    }

    /**
     * Spliting the long text into shorter lines.
     * @param text The text to be displayed on the panel.
     * @param maxWidth the maximum width of the text on the panel.
     * @return the lines of text.
     */
    private List<String> splitText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = getFontMetrics(VT323);
        String[] words = text.split("\\s+");

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() == 0) {
                currentLine.append(word);
            } else {
                String testLine = currentLine + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    currentLine.append(" ").append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }
    /**
     * Paint the backgroundImage.
     * Paint the text with selected font and color at specified location.
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
        if (textToDraw != null && !textToDraw.isEmpty()) {
            g.setFont(VT323);
            g.setColor(new Color(0, 255, 100));
            int y = 600;
            for (int i = 0; i < linesToShow && i < textToDraw.size(); i++) {
                g.drawString(textToDraw.get(i), 10, y);
                y += g.getFontMetrics().getHeight();
            }
            linesCount = linesToShow;
        }
    }
}
