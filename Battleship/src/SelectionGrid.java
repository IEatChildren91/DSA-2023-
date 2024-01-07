
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;



/**
 * Battleship
 * Author: Kudo
 *
 * SelectionGrid class:
 * Defines the grid for storing Ships with a grid of markers to
 * indicate hit/miss detection.
 */
public class SelectionGrid extends Rectangle {
    public boolean isComputerGrid;
    public boolean isPlayer;
    public boolean showMines;
    private Map <Position, Boolean> treasureStates = new HashMap<>();
    private BufferedImage closedtreasureImage;
    private BufferedImage opentreasureImage;
    private BufferedImage mineImage;
    private Font VT323;
    private List<Position> treasures;
    private List<Position> mines;
    /**
     * Size of each grid cell in pixels.
     */
    public static final int CELL_SIZE = 50;
    /**
     * Number of grid cells on the Horizontal axis.
     */
    public static final int GRID_WIDTH = 11;
    /**
     * Number of grid cells on the Vertical axis.
     */
    public static final int GRID_HEIGHT = 11;
    /**
     * Definitions of the number of Ships, and the number of segments that make up each of those ships.
     */
    public static final int[] BOAT_SIZES = {5, 4, 3, 3, 2};

    /**
     * A grid of markers to indicate visually the hit/miss on attacks.
     */
    private Marker[][] markers = new Marker[GRID_WIDTH][GRID_HEIGHT];
    /**
     * A list of all the ships on this grid.
     */
    private List<Ship> ships;
    /**
     * Shared random reference to use for randomisation of the ship placement.
     */
    private Random rand;
    private Random rand2;
    /**
     * Ships are drawn when true. This is mostly used to make the player's ships always show.
     */
    private boolean showShips;
    private boolean showTreasures;
    /**
     * True once all the elements in ships have been destroyed.
     */
    private boolean allShipsDestroyed;

    /**
     * Configures the grid to create the default configuration of markers.
     *
     * @param x X coordinate to offset the grid by in pixels.
     * @param y Y coordinate to offset the grid by in pixels.
     */
    public SelectionGrid(int x, int y, boolean isComputerGrid) {
        super(x, y, CELL_SIZE * GRID_WIDTH, CELL_SIZE * GRID_HEIGHT);
        this.isComputerGrid = isComputerGrid;
        createMarkerGrid();
        ships = new ArrayList<>();
        treasures = new ArrayList<>();
        mines = new ArrayList<>();
        rand2 = new Random();
        initilizeTreasures(3);
        rand = new Random();
        showShips = false;
        try {
            VT323 = Font.createFont(Font.TRUETYPE_FONT, new File("VT323-Regular.ttf")).deriveFont(40f); // Adjust the font size as needed
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(VT323);
            closedtreasureImage = ImageIO.read(new File("closed.png"));
            opentreasureImage = ImageIO.read(new File("open.png"));
            mineImage = ImageIO.read(new File("mine.png"));
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            VT323 = new Font("Serif", Font.BOLD, 16); // Fallback font in case of error
        }
    }
    private void initilizeTreasures(int numOfTreasures) {
        while (treasures.size() < numOfTreasures && isComputerGrid) {
            int x = rand2.nextInt(GRID_WIDTH - 1) + 1;
            int y = rand2.nextInt(GRID_HEIGHT - 1) + 1;
            Position potentialTreasure = new Position(x, y);

            if (!isTreasureAtPosition(potentialTreasure) && !markers[x][y].isShip()) {
                treasures.add(potentialTreasure);
                treasureStates.put(potentialTreasure, false); // Initialize as not hit
                markers[x][y].setAsTreasure(potentialTreasure);
            }
        }
    }
    public boolean isTreasureAtPosition(Position pos) {
        for (Position treasure : treasures) {
            if (treasure.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draws the ships if all ships on this grid are to be shown, or if debug mode is active,
     * or if each individual ship is flagged as having been destroyed. Then draws all markers
     * that should be shown for attacks made so far, and a grid of lines to show where the grid
     * is overlaid.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        drawGrid(g);
        drawMarkers(g);
        // Draw the grid and other components
        for (Ship ship : ships) {
            if (showShips || GamePanel.debugModeActive || ship.isDestroyed()) {
                ship.paint(g);
            }
        }
        drawTreasures(g);
        drawMine(g);
    }

    /**
     * Modifies the state of the grid to show all the ships if set to true.
     *
     * @param showShips True will make all the ships on this grid be visible.
     */
    public void setShowShips(boolean showShips) {
        this.showShips = showShips;
    }
    public void setShowTreasures (boolean showTreasures) {
        this.showTreasures = showTreasures;
    }

    /**
     * Resets the SelectionGrid by telling all the markers to reset,
     * removing all ships from the grid, and defaulting back to not
     * showing any ships, and a state where no ships have been destroyed.
     */
    public void reset() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y].reset();
            }
        }
        for (Position treasure : treasures) {
            treasureStates.put(treasure, false); // Reset treasure to closed state
        }
        ships.clear();
        showShips = false;
        allShipsDestroyed = false;
    }

    /**
     * Marks the specified position and then checks all ships to determine if they have
     * all been destroyed.
     *
     * @param posToMark Position to mark.
     * @return True if the marked position was a ship.
     */
    public boolean markPosition(Position posToMark, boolean isPlayer) {
        if (posToMark.x < 1 || posToMark.y < 1) {
            return false;
        }

        markers[posToMark.x][posToMark.y].mark();

        boolean hitTreasure = isTreasureAtPosition(posToMark);
        if (hitTreasure && isPlayer) {
            markers[posToMark.x][posToMark.y].setAsTreasure(posToMark); // Mark the marker as a treasure
        }
        allShipsDestroyed = true;
        for (Ship ship : ships) {
            if (!ship.isDestroyed()) {
                allShipsDestroyed = false;
                break;
            }
        }
        return markers[posToMark.x][posToMark.y].isShip() || hitTreasure;
    }


    /**
     * Checks if all ships have been destroyed.
     *
     * @return True if all the ships have been destroyed.
     */
    public boolean areAllShipsDestroyed() {
        return allShipsDestroyed;
    }

    /**
     * Checks if the specified position is already marked.
     *
     * @param posToTest Position to test if it is marked.
     * @return True if the marker at the specified position is marked.
     */
    public boolean isPositionMarked(Position posToTest) {
        return markers[posToTest.x][posToTest.y].isMarked();
    }

    /**
     * Gets the marker at the specified position. Useful for allowing the AI more access to the data on the grid.
     *
     * @param posToSelect Position on the grid to select the marker at.
     * @return Returns a reference to the marker at the specified position.
     */
    public Marker getMarkerAtPosition(Position posToSelect) {
        return markers[posToSelect.x][posToSelect.y];
    }

    /**
     * Translates the mouse position to a grid coordinate if possible.
     *
     * @param mouseX Mouse X coordinate.
     * @param mouseY Mouse Y coordinate.
     * @return Returns either (-1,-1) for an invalid position, or the corresponding grid position related to the coordinates.
     */
    public Position getPositionInGrid(int mouseX, int mouseY) {
        if (!isPositionInside(new Position(mouseX, mouseY))) return new Position(-1, -1);

        return new Position((mouseX - position.x) / CELL_SIZE, (mouseY - position.y) / CELL_SIZE);
    }

    /**
     * Tests if a ship given the specified properties would be valid for placement.
     * Tests this by checking if the ship fits within the bounds of the grid, and then
     * checks if all the segments would fall on places where a ship does not already sit.
     * This is handled separately depending on whether it is a horizontal (sideways) or
     * vertical ship.
     *
     * @param gridX    Grid X coordinate.
     * @param gridY    Grid Y coordinate.
     * @param segments The number of cells that tail the coordinate.
     * @param sideways True indicates it is horizontal, false insides it is vertical.
     * @return True if the ship can be placed with the specified properties.
     */
    public boolean canPlaceShipAt(int gridX, int gridY, int segments, boolean sideways) {
        if (gridX < 1 || gridY < 1) return false;

        if (sideways) { // handle the case when horizontal
            if (gridY > GRID_HEIGHT || gridX + segments > GRID_WIDTH) return false;
            for (int x = 0; x < segments; x++) {
                if (markers[gridX + x][gridY].isShip() || isTreasureAtPosition(new Position(gridX + x, gridY))) return false;
            }
        } else { // handle the case when vertical
            if (gridY + segments > GRID_HEIGHT || gridX > GRID_WIDTH) return false;
            for (int y = 0; y < segments; y++) {
                if (markers[gridX][gridY + y].isShip() || isTreasureAtPosition(new Position(gridX, gridY + y))) return false;
            }
        }
        return true;
    }

    /**
     * Draws a grid made up of single pixel black lines.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        Color labelBG = new Color(10, 139 - 50, 50 - 50);
        g2d.setColor(labelBG);

        g2d.fillRect(position.x, position.y, GRID_WIDTH * CELL_SIZE, CELL_SIZE); //top row
        g2d.fillRect(position.x, position.y, CELL_SIZE, GRID_HEIGHT * CELL_SIZE); //leftmost column

        g2d.setColor(new Color(0, 255, 100)); // Set the color for the grid lines
        float thickness = 2.0f; // Adjust for desired thickness
        g2d.setStroke(new BasicStroke(thickness));

        g2d.setFont(VT323);

        // Get font metrics for centering text
        FontMetrics metrics = g2d.getFontMetrics(VT323);

        // Draw vertical lines and letters
        for (int x = 0; x <= GRID_WIDTH; x++) {
            int xPos = position.x + x * CELL_SIZE;
            g2d.drawLine(xPos, position.y, xPos, position.y + height);
            if (x > 0 && x <= 10) { // Skip the first column for numbers
                char label = (char) ('A' + x - 1); // Letters start from 'A'
                String labelText = String.valueOf(label);
                int labelWidth = metrics.stringWidth(labelText);
                int labelHeight = metrics.getHeight();
                // Center text in the middle of the cell
                g2d.drawString(labelText, xPos + (CELL_SIZE - labelWidth) / 2, position.y + (CELL_SIZE + labelHeight) / 2 - metrics.getDescent());
            }
        }

        // Draw horizontal lines and numbers
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            int yPos = position.y + y * CELL_SIZE;
            g2d.drawLine(position.x, yPos, position.x + width, yPos);
            if (y > 0) { // Skip the first row for letters
                String labelText = String.valueOf(y - 1);
                int labelWidth = metrics.stringWidth(labelText);
                int labelHeight = metrics.getHeight();
                // Center text in the middle of the cell
                g2d.drawString(labelText, position.x + (CELL_SIZE - labelWidth) / 2, yPos + (CELL_SIZE + labelHeight) / 2 - metrics.getDescent());
            }
        }

        g2d.dispose();
    }


    /**
     * Draws all the markers. The markers will determine individually if it is necessary to draw.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawMarkers(Graphics g) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y].paint(g);
            }
        }
    }

    /**
     * Creates all the marker objects setting their draw positions on the grid to initialise them.
     */
    private void createMarkerGrid() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y] = new Marker(position.x + x * CELL_SIZE, position.y + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Clears all current ships, and then randomly places all the ships. The ships
     * will not be placed over the top of other ships. This method assumes there is
     * plenty of space to place all the ships regardless of configuration.
     */
    public void populateShips() {
        ships.clear();
        for (int i = 0; i < BOAT_SIZES.length; i++) {
            boolean sideways = rand.nextBoolean();
            int gridX, gridY;
            do {
                gridX = rand.nextInt(sideways ? GRID_WIDTH - BOAT_SIZES[i] : GRID_WIDTH);
                gridY = rand.nextInt(sideways ? GRID_HEIGHT : GRID_HEIGHT - BOAT_SIZES[i]);
            } while (!canPlaceShipAt(gridX, gridY, BOAT_SIZES[i], sideways));
            placeShip(gridX, gridY, BOAT_SIZES[i], sideways);
        }
    }

    /**
     * Places a ship on the grid with the specified properties. Assumes checks have already been
     * made to verify the ship can be placed there. Indicates to the marker cells that a ship is
     * on top of them to use for placement of other ships, and hit detection.
     *
     * @param gridX    X coordinate on the grid.
     * @param gridY    Y coordinate on the grid.
     * @param segments Number of cells the ship occupies.
     * @param sideways True indicates horizontal, or false indicates vertical.
     */
    public void placeShip(int gridX, int gridY, int segments, boolean sideways) {
        placeShip(new Ship(new Position(gridX, gridY),
                new Position(position.x + gridX * CELL_SIZE, position.y + gridY * CELL_SIZE),
                segments, sideways), gridX, gridY);
    }

    /**
     * Places a ship on the grid with the specified properties. Assumes checks have already been
     * made to verify the ship can be placed there. Indicates to the marker cells that a ship is
     * on top of them to use for placement of other ships, and hit detection.
     *
     * @param ship  The ship to place on the grid with already configured properties.
     * @param gridX X coordinate on the grid.
     * @param gridY Y coordinate on the grid.
     */
    public void placeShip(Ship ship, int gridX, int gridY) {
        ships.add(ship);
        if (ship.isSideways()) { // If the ship is horizontal
            for (int x = 0; x < ship.getSegments(); x++) {
                markers[gridX + x][gridY].setAsShip(ships.get(ships.size() - 1));
            }
        } else { // If the ship is vertical
            for (int y = 0; y < ship.getSegments(); y++) {
                markers[gridX][gridY + y].setAsShip(ships.get(ships.size() - 1));
            }
        }
    }

    private void drawTreasures(Graphics g) {
        if (isComputerGrid) {
            for (Position treasure : treasures) {
                // Check if the treasure's position is marked
                if (markers[treasure.x][treasure.y].isMarked() || GamePanel.debugModeActive || showTreasures) {
                    BufferedImage imgToDraw = treasureStates.get(treasure) ? opentreasureImage : closedtreasureImage;

                    int x = position.x + treasure.x * CELL_SIZE;
                    int y = position.y + treasure.y * CELL_SIZE;

                    int treasureWidth = 45; // Set your desired width
                    int treasureHeight = 45; // Set your desired height

                    double scaleX = (double) treasureWidth / imgToDraw.getWidth();
                    double scaleY = (double) treasureHeight / imgToDraw.getHeight();

                    AffineTransform at = AffineTransform.getTranslateInstance(x + (CELL_SIZE - treasureWidth) / 2, y + (CELL_SIZE - treasureHeight) / 2);
                    at.scale(scaleX, scaleY);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawImage(imgToDraw, at, null);
                }
            }
        }
    }
    public void markTreasureAsOpened(Position pos) {
        if (treasureStates.containsKey(pos)) {
            treasureStates.put(pos, true);
        }
    }
    private void drawMine (Graphics g) {
        for (Position mine : mines) {
            int x = position.x + mine.x * CELL_SIZE;
            int y = position.y + mine.y * CELL_SIZE;
            g.drawImage(mineImage, x, y, CELL_SIZE, CELL_SIZE, null);
        }
    }
}