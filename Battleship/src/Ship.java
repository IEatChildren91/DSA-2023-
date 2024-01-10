import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: Ship.
 * Defines a simple ship object that can be drawn onto the screen. Stores
 * information about how many segments it includes, the direction of the
 * ship, how many of the segments have been destroyed, and properties to
 * set the colour.
 */
public class Ship {
    /**
     * Image of the ships.
     */
    private BufferedImage shipImage;
    /**
     * Image of the ship with red filter (sunk).
     */
    private BufferedImage shipImageRed;
    /**
     * The position in grid coordinates for where the ship is located.
     */
    private Position gridPosition;
    /**
     * The position in pixels for drawing the ship.
     */
    private Position drawPosition;
    /**
     * The number of segments in the ship to show how many cells it goes across.
     */
    private int segments;
    /**
     * True indicates the ship is horizontal, and false indicates the ship is vertical.
     */
    private boolean isSideways;
    /**
     * The number of destroyed sections to help determine if all of the ship has been destroyed when compared to segments.
     */
    private int destroyedSections;
    /**
     * Creates the ship with default properties ready for use. Assumes it has already been placed when created.
     * @param gridPosition The position where the ship is located in terms of grid coordinates.
     * @param drawPosition Top left corner of the cell to start drawing the ship in represented in pixels.
     * @param segments The number of segments in the ship to show how many cells it goes across.
     * @param isSideways True indicates the ship is horizontal, and false indicates the ship is vertical.
     */
    public Ship(Position gridPosition, Position drawPosition, int segments, boolean isSideways) {
        this.gridPosition = gridPosition;
        this.drawPosition = drawPosition;
        this.segments = segments;
        this.isSideways = isSideways;
        destroyedSections = 0;
        initializeImages();
    }
    /**
     * Initializes the ship images based on the number of segments and orientation.
     * Constructs the ship image path based on the number of segments and orientation,
     * then reads the ship image from the file and applies a red color filter.
     */
    private void initializeImages() {
        String imagePath = "ship" + segments + (isSideways ? "h" : "v") + ".png";
        try {
            shipImage = ImageIO.read(new File(imagePath));
            shipImageRed = applyRedColorFilter(shipImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Paints the ship image on the graphics context based on its state.
     * Determines the image to draw based on whether the ship is destroyed or not
     * and then draws the respective ship image on the graphics context.
     * @param g The Graphics object to paint on.
     */
    public void paint (Graphics g) {
        BufferedImage imgToDraw = isDestroyed() ? shipImageRed : shipImage;
        drawImage (g, imgToDraw);
    }
    /**
     * Draws the specified image on the graphics context based on the ship's orientation.
     * Scales and positions the image according to the ship's segments and orientation,
     * then draws it onto the graphics context (g).
     * @param g   The Graphics object to draw on.
     * @param img The BufferedImage to be drawn.
     */
    private void drawImage(Graphics g, BufferedImage img) {
        if (isSideways) { // Handle the horizontal ship

            // Calculate scaling factors and scale the image
            double scaleWidth = ((double) SelectionGrid.CELL_SIZE * segments) / img.getWidth();
            double scaleHeight = ((double) SelectionGrid.CELL_SIZE * 0.8) / img.getHeight();
            AffineTransform at = new AffineTransform();
            at.translate(drawPosition.x, drawPosition.y + SelectionGrid.CELL_SIZE / 2 - (img.getHeight() * scaleHeight) / 2);
            at.scale(scaleWidth, scaleHeight);

            // Draw the image
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(img, at, null);
        }
        else { // Handle the vertical ship

            // Calculate scaling factors and scale the image
            double scaleWidth = ((double)SelectionGrid.CELL_SIZE * 0.8) / img.getWidth();
            double scaleHeight = ((double)SelectionGrid.CELL_SIZE * segments) / img.getHeight();
            AffineTransform at = new AffineTransform();
            at.translate(drawPosition.x + SelectionGrid.CELL_SIZE / 2 - (img.getWidth() * scaleWidth) / 2, drawPosition.y);
            at.scale(scaleWidth, scaleHeight);

            // Draw the image
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(img, at, null);
        }
    }
    /**
     * Toggles the current state between vertical and horizontal.
     */
    public void toggleSideways() {
        isSideways = !isSideways;
        initializeImages(); // Change the image path to call the opposite direction of the current ship
    }
    /**
     * Call when a section has been destroyed to let the ship keep track of how many sections have been destroyed.
     */
    public void destroySection() {
        destroyedSections++;
    }

    /**
     * Tests if the number of sections destroyed indicate all segments have been destroyed.
     * @return True if all sections have been destroyed.
     */
    public boolean isDestroyed() { return destroyedSections >= segments; }

    /**
     * Updates the position to draw the ship at to the newPosition.
     * @param gridPosition Position where the ship is now on the grid.
     * @param drawPosition Position to draw the Ship at in Pixels.
     */
    public void setDrawPosition(Position gridPosition, Position drawPosition) {
        this.drawPosition = drawPosition;
        this.gridPosition = gridPosition;
    }
    /**
     * Gets the current direction of the ship.
     * @return True if the ship is currently horizontal, or false if vertical.
     */
    public boolean isSideways() {
        return isSideways;
    }
    /**
     * Gets the number of segments that make up the ship.
     * @return The number of cells the ship occupies.
     */
    public int getSegments() {
        return segments;
    }
    /**
     * Gets a list of all cells that this ship occupies to be used for validation in AI checks.
     * @return A list of all cells that this ship occupies.
     */
    public List <Position> getOccupiedCoordinates() {
        List<Position> result = new ArrayList<>();
        if(isSideways) { // handle the case when horizontal
            for(int x = 0; x < segments; x++) {
                result.add(new Position(gridPosition.x+x, gridPosition.y));
            }
        } else { // handle the case when vertical
            for(int y = 0; y < segments; y++) {
                result.add(new Position(gridPosition.x, gridPosition.y+y));
            }
        }
        return result;
    }
    /**
     * Applies a red color filter to the given BufferedImage by modifying its red channel.
     * If the original image has an alpha channel, the alpha channel is retained.
     * @param original The original BufferedImage to which the red filter will be applied.
     * @return A new BufferedImage with the red color filter applied.
     */
    public static BufferedImage applyRedColorFilter(BufferedImage original) {
        // Check if the original image has an alpha channel
        boolean hasAlphaChannel = original.getColorModel().hasAlpha();

        // Create lookup tables for red, green, and blue components
        short[] red = new short[256];
        short[] zero = new short[256];
        for (int i = 0; i < 256; i++) {
            red[i] = (short) i; // retain red component
            zero[i] = 0;        // zero out green and blue components
        }
        short[][] data = hasAlphaChannel ? new short[][]{red, zero, zero, red} : new short[][]{red, zero, zero}; // Include alpha if original has alpha
        ShortLookupTable lookupTable = new ShortLookupTable(0, data);

        // Applying the lookup operation
        BufferedImage processed = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        LookupOp op = new LookupOp(lookupTable, null);
        return op.filter(original, processed);
    }
}
