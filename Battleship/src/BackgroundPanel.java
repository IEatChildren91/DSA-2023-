import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private Font VT323;
    private List<String> textToDraw;
    private int linesToShow;
    private int linesCount;
    private Timer animationTimer;

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
    public void startTextAnimation() {
        linesCount = 0;
        linesToShow = 0;
        animationTimer.start();
    }

    public void drawTextOnPanel(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return;
        }
        this.textToDraw = splitText(text, maxWidth);
        startTextAnimation();
    }
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
