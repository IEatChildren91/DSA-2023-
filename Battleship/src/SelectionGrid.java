import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: SelectionGrid.
 * Represents a grid for the game, managing ships, treasures, and markers for gameplay.
 */
public class SelectionGrid extends Rectangle {
    /**
     * Determine if the current grid is computer's grid or player's grid.
     */
    public boolean isComputerGrid;
    /**
     * Determine it is player's move or computer's move
     */
    public boolean isPlayer;
    /**
     * HashMap to manage treasure's state
     */
    private Map <Position, Boolean> treasureStates = new HashMap<>();
    /**
     * Image of the closed treasure (when the treasure is still hidden).
     */
    private BufferedImage closedtreasureImage;
    /**
     * Image of the opened treasure (when player find it on the computer's grid).
     */
    private BufferedImage opentreasureImage;
    /**
     * The font VT323.
     */
    private Font VT323;
    /**
     * A list to store position of the treasures.
     * Since 1 treasure = 1 cell = 1 position, there is no need to make another class for the treasures.
     */
    private List<Position> treasures;
    /**
     * The size of 1 cell in pixel.
     */
    public static final int CELL_SIZE = 50;
    /**
     * Number of cells of 1 grid horizontally.
     */
    public static final int GRID_WIDTH = 11;
    /**
     * Number of cells of 1 grid vertically.
     */
    public static final int GRID_HEIGHT = 11;
    /**
     * Definitions of the number of Ships, and the number of segments that make up each of those ships.
     */
    public static final int[] BOAT_SIZES = {5, 4, 3, 3, 2};
    /**
     * A grid of marker to indicate hit/miss on the grid.
     */
    private Marker[][] markers = new Marker[GRID_WIDTH][GRID_HEIGHT];
    /**
     * A list of ships on the grid.
     */
    private List<Ship> ships;
    /**
     * Shared random reference to use for randomisation of the ship placement.
     */
    private Random rand;
    /**
     * Shared random reference to use for randomisation of the treasure placement.
     */
    private Random rand2;
    /**
     * Ships are drawn when true. This is mostly used to make the player's ships always show.
     */
    private boolean showShips;
    /**
     * Treasures are drawn when true.
     */
    private boolean showTreasures;
    /**
     * True once all the elements in ships have been destroyed.
     */
    private boolean allShipsDestroyed;
    /**
     * Constructs a SelectionGrid object with specified coordinates and grid properties.
     * @param x The x-coordinate of the grid.
     * @param y The y-coordinate of the grid.
     * @param isComputerGrid Boolean indicating if the grid belongs to the computer.
     */
    public SelectionGrid(int x, int y, boolean isComputerGrid) {
        super(x, y, CELL_SIZE * GRID_WIDTH, CELL_SIZE * GRID_HEIGHT);
        this.isComputerGrid = isComputerGrid;
        createMarkerGrid();
        ships = new ArrayList<>(); //Make an ArrayList to store the ships.
        treasures = new ArrayList<>(); //Make an ArrayList to store the treasures.
        rand2 = new Random(); //Random position of the treasures on computer's grid.
        initilizeTreasures(3); //3 treasures
        rand = new Random(); //Random position of the ships on the computer's grid
        showShips = false;
        try {
            VT323 = Font.createFont(Font.TRUETYPE_FONT, new File("VT323-Regular.ttf")).deriveFont(40f); // Adjust the font size as needed
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(VT323);
            closedtreasureImage = ImageIO.read(new File("closed.png"));
            opentreasureImage = ImageIO.read(new File("open.png"));
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            VT323 = new Font("Serif", Font.BOLD, 16); // Fallback font in case of error
        }
    }
    /**
     * Initializes a specified number of treasures on the grid.
     * The treasures are placed randomly without overlapping on the grid.
     * Not place the treasures on the label col and row.
     * @param numOfTreasures The number of treasures to initialize.
     */
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
    /**
     * Checks if a treasure is present at the specified position.
     * @param pos The position to check for treasure.
     * @return True if a treasure is found at the given position, false otherwise.
     */
    public boolean isTreasureAtPosition(Position pos) {
        for (Position treasure : treasures) {
            if (treasure.equals(pos)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Paints the grid, markers, ships, and treasures on the screen.
     * @param g The Graphics object to paint on.
     */
    public void paint(Graphics g) {
        drawGrid(g); //Draw grid first to prevent false layering.
        drawMarkers(g);
        for (Ship ship : ships) {
            if (showShips || GamePanel.debugModeActive || ship.isDestroyed()) {
                ship.paint(g);
            }
        }
        drawTreasures(g);
    }
    /**
     * Sets the visibility of ships on the grid.
     * @param showShips Boolean indicating whether to display ships or not.
     */
    public void setShowShips(boolean showShips) {
        this.showShips = showShips;
    }
    /**
     * Sets the visibility of treasures on the grid.
     * @param showTreasures Boolean indicating whether to display ships or not.
     */
    public void setShowTreasures (boolean showTreasures) {
        this.showTreasures = showTreasures;
    }
    /**
     * Resets the grid by clearing markers, treasures, and ships.
     */
    public void reset() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y].reset(); //Reset the marker's grid.
            }
        }
        for (Position treasure : treasures) {
            treasureStates.put(treasure, false); // Reset treasure to closed state.
        }
        ships.clear(); //Clear all the ships on the grids.
        showShips = false;
        allShipsDestroyed = false;
    }
    /**
     * Marks the specified position and checks if it hits a ship or treasure.
     * @param posToMark The position to be marked.
     * @param isPlayer  Boolean indicating if the marking is by the player.
     * @return True if the position hits a ship or treasure, false otherwise.
     */
    public boolean markPosition(Position posToMark, boolean isPlayer) {
        if (posToMark.x < 1 || posToMark.y < 1) {
            return false; //If the posToMark on the label col and row or outside the grid, return false.
        }
        markers[posToMark.x][posToMark.y].mark();
        boolean hitTreasure = isTreasureAtPosition(posToMark);
        if (hitTreasure && isPlayer) {
            markers[posToMark.x][posToMark.y].setAsTreasure(posToMark); // Mark the marker as a treasure.
        }
        allShipsDestroyed = true;
        for (Ship ship : ships) {
            if (!ship.isDestroyed()) {
                allShipsDestroyed = false;
                break;
            }
        }
        return markers[posToMark.x][posToMark.y].isShip() || hitTreasure; //If hit ship or hit treasure, it will draw the marker on the grid.
    }
    /**
     * Checks if all ships on the grid are destroyed.
     * @return True if all ships are destroyed, false otherwise.
     */
    public boolean areAllShipsDestroyed() {
        return allShipsDestroyed;
    }
    /**
     * Checks if a position on the grid is marked.
     * @param posToTest The position to be tested for marking.
     * @return True if the position is marked, false otherwise.
     */
    public boolean isPositionMarked(Position posToTest) {
        return markers[posToTest.x][posToTest.y].isMarked();
    }
    /**
     * Retrieves the marker at the specified position.
     * @param posToSelect The position to retrieve the marker from.
     * @return The marker object at the specified position.
     */
    public Marker getMarkerAtPosition(Position posToSelect) {
        return markers[posToSelect.x][posToSelect.y];
    }
    /**
     * Determines the grid position based on mouse coordinates.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @return The position in the grid corresponding to the mouse coordinates.
     */
    public Position getPositionInGrid(int mouseX, int mouseY) {
        if (!isPositionInside(new Position(mouseX, mouseY))) return new Position(-1, -1);
        return new Position((mouseX - position.x) / CELL_SIZE, (mouseY - position.y) / CELL_SIZE);
    }
    /**
     * Checks if a ship can be placed at the specified position.
     * @param gridX     The x-coordinate in the grid.
     * @param gridY     The y-coordinate in the grid.
     * @param segments  The number of segments the ship occupies.
     * @param sideways  Boolean indicating if the ship is placed sideways.
     * @return True if the ship can be placed at the position, false otherwise.
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
     * Draws the grid lines and labels on the graphics object.
     * @param g The Graphics object used for drawing.
     */
    private void drawGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Color labelBG = new Color(10, 139 - 50, 50 - 50);
        g2d.setColor(labelBG);
        g2d.fillRect(position.x, position.y, GRID_WIDTH * CELL_SIZE, CELL_SIZE); //top row
        g2d.fillRect(position.x, position.y, CELL_SIZE, GRID_HEIGHT * CELL_SIZE); //leftmost column
        g2d.setColor(new Color(0, 255, 100)); // Set the color for the grid lines
        float thickness = 2.0f; // Thicker grid lines
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
     * Draws markers on the grid based on their states.
     * @param g The Graphics object used for drawing.
     */
    private void drawMarkers(Graphics g) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y].paint(g);
            }
        }
    }
    /**
     * Initializes the marker grid with Marker objects.
     */
    private void createMarkerGrid() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                markers[x][y] = new Marker(position.x + x * CELL_SIZE, position.y + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    /**
     * Populates the grid with ships using randomized positions and sizes.
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
     * Places a ship on the grid at the specified position.
     * @param gridX     The x-coordinate in the grid.
     * @param gridY     The y-coordinate in the grid.
     * @param segments  The number of segments the ship occupies.
     * @param sideways  Boolean indicating if the ship is placed sideways.
     */
    public void placeShip(int gridX, int gridY, int segments, boolean sideways) {
        placeShip(new Ship(new Position(gridX, gridY),
                new Position(position.x + gridX * CELL_SIZE, position.y + gridY * CELL_SIZE),
                segments, sideways), gridX, gridY);
    }
    /**
     * Places a ship object on the grid at the specified position.
     * @param ship      The Ship object to be placed.
     * @param gridX     The x-coordinate in the grid.
     * @param gridY     The y-coordinate in the grid.
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
    /**
     * Draws treasures on the grid based on their states.
     * @param g The Graphics object used for drawing.
     */
    private void drawTreasures(Graphics g) {
        if (isComputerGrid) {
            for (Position treasure : treasures) {
                // Check if the treasure's position is marked
                if (markers[treasure.x][treasure.y].isMarked() || GamePanel.debugModeActive || showTreasures) {
                    BufferedImage imgToDraw = treasureStates.get(treasure) ? opentreasureImage : closedtreasureImage;

                    int x = position.x + treasure.x * CELL_SIZE;
                    int y = position.y + treasure.y * CELL_SIZE;

                    int treasureWidth = 45; // Smaller than the CELL_SIZE
                    int treasureHeight = 45; // Smaller than the CELL_SIZE

                    //Scale the image and draw
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
    /**
     * Marks a treasure as opened at the specified position.
     * @param pos The position of the opened treasure.
     */
    public void markTreasureAsOpened(Position pos) {
        if (treasureStates.containsKey(pos)) {
            treasureStates.put(pos, true);
        }
    }
}