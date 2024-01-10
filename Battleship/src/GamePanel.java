import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;
import javax.imageio.ImageIO;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: GamePanel.
 * It controls the state information and interactions among game elements, overseeing two game grids:
 * one for the human player and another for the computer, separated by a status panel.
 * This panel displays the current game state, allowing players to place their ships and target their opponent.
 * During ship placement, the player can position their ships on their grid.
 * In the attack phase, the player targets the computer's grid to destroy its ships and find the hidden treasures.
 * The status panel provides updates on game progress and the comparative state of both player and computer grids.
 * It also handles player inputs, enabling actions like ship placement, attacking, and toggling debug mode.
 * The AI determines the computer's moves, and the panel manages the game flow based on the current state.
 * Additionally, it contains methods to handle mouse and key events for gameplay interactions.
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {
    /**
     * Enumerates the different states of the game:
     * `PlacingShips`: Represents the phase where the player is placing their ships on the grid.
     * `FiringShots`: Represents the phase where the player is attacking the computer's grid.
     * `GameOver`: Represents the phase where the game has ended.
     */
    public enum GameState { PlacingShips, FiringShots, GameOver }
    /**
     * Variable to store and calculate computer's score.
     */
    private int compCount;
    /**
     * Variable to store and calculate player's score.
     */
    private int playerCount;
    /**
     * Reference to the status panel to pass text messages to show what is happening.
     */
    private StatusPanel statusPanel;
    /**
     * The computer's grid for the player to attack.
     */
    protected SelectionGrid computer;
    /**
     * The player's grid for the computer to attack.
     */
    protected SelectionGrid player;
    /**
     * AI to manage what the computer will do each turn.
     */
    private BattleshipAI aiController;

    /**
     * Reference to the temporary ship that is being placed during the PlacingShips state.
     */
    private Ship placingShip;
    /**
     * Grid position where the placingShip is located.
     */
    private Position tempPlacingPosition;
    /**
     * Reference to which ship should be placed next during the PlacingShips state.
     */
    private int placingShipIndex;
    /**
     * The game state to represent whether the player can place ships, attack the computer,
     * or if the game is already over.
     */
    private GameState gameState;
    /**
     * A state that can be toggled with D to show the computer's ships.
     */
    public static boolean debugModeActive;
    /**
     * A state that can be toggled when player hit treasures.
     */
    public boolean hasExtraTurn;
    /**
     * Image to draw as the background of the grids.
     */
    private BufferedImage radarBG;
    /**
     * Draws a radar background image on the provided graphics context at a specified position and size.
     * If the radar background image exists, it adjusts its position and opacity before drawing it on the graphics context.
     * @param g The graphics context on which the radar background will be drawn.
     * @param gridX The x-coordinate of the grid.
     * @param gridY The y-coordinate of the grid.
     * @param gridWidth The width of the grid.
     * @param gridHeight The height of the grid.
     */
    private void drawRadarBackground(Graphics g, int gridX, int gridY, int gridWidth, int gridHeight) {
        if (radarBG != null) {
            int imageWidth = 50*10 + 200;
            int imageHeight = 50*9-35;

            int newX = gridX + (gridWidth - imageWidth) / 2 + 15;
            int newY = gridY + (gridHeight - imageHeight) / 2 + 25;
            Graphics2D g2d = (Graphics2D) g;
            float alpha = 0.7f;
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(alphaComposite);
            g2d.drawImage(radarBG, newX, newY, imageWidth, imageHeight, null);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }
    /**
     * Constructs a GamePanel instance with specific settings based on the given difficulty level.
     * It initializes grids for the player and computer, sets up the user interface components,
     * and prepares the game environment.
     * @param difficulty The difficulty level chosen for the game.
     */
    public GamePanel(Game.GameDifficulty difficulty) {
        try {
            radarBG = ImageIO.read(new File("radar.png")); // Load the radar image
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Choose the AI asociated to the difficulty
        int aiChoice = mapDifficultyToAIChoice(difficulty);

        // Gap between the two grids
        int gap = 60;
        // Initialize the grids
        computer = new SelectionGrid(0, 0, true);
        player = new SelectionGrid(computer.getWidth() + gap, 0, false);
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        int totalWidth = computer.getWidth() + player.getWidth() + gap;
        int maxHeight = Math.max(computer.getHeight(), player.getHeight());
        setPreferredSize(new Dimension(totalWidth, maxHeight + 150));
        addMouseListener(this);
        addMouseMotionListener(this);

        // Choose the AI to play with player
        if (aiChoice == 0) {
            aiController = new SimpleRandomAI(player);
        } else if (aiChoice == 1) {
            aiController = new SmarterAI(player, false, true);
        } else {
            aiController = new SmarterAI(player, true, true);
        }

        // Draw the status panel at the bottom of the grid
        statusPanel = new StatusPanel(new Position(0, maxHeight), totalWidth, 49);

        hasExtraTurn = false;
        restart();
    }
    /**
     * Maps the specified game difficulty level to an AI choice, allowing the selection
     * of an appropriate AI strategy based on the game's difficulty.
     * @param difficulty The chosen difficulty level for the game.
     * @return An integer representing the AI strategy choice:
     *         0 for EASY, 1 for MEDIUM, 2 for HARD, defaulting to 0 for other cases.
     */
    private int mapDifficultyToAIChoice(Game.GameDifficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 0;
            case MEDIUM:
                return 1;
            case HARD:
                return 2;
            default:
                return 0;} 
        }
    /**
     * Draws the grids with radar background for both players, any ship being placed, and the status panel.
     * @param g Reference to the Graphics object for drawing.
     */
    public void paint(Graphics g) {
        super.paint(g);
        drawRadarBackground(g, computer.getPosition().x, computer.getPosition().y, computer.getWidth(), computer.getHeight());
        drawRadarBackground(g, player.getPosition().x, player.getPosition().y, player.getWidth(), player.getHeight());
        computer.paint(g);
        player.paint(g);
        if(gameState == GameState.PlacingShips) {
            placingShip.paint(g);
        }
        statusPanel.paint(g);
    }
    /**
     * Handles input based on keys that are pressed.
     * Escape quits the application. S restarts.
     * R rotates the ship while in PlacingShips state.
     * D activates the debug mode to show computer ships and treasures.
     * @param keyCode The key that was pressed.
     */
    public void handleInput(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(1);
        } else if(keyCode == KeyEvent.VK_S) {
            restart(); //S to restart
        } else if(gameState == GameState.PlacingShips && keyCode == KeyEvent.VK_R) {
            placingShip.toggleSideways(); //R to rotate the ship
            updateShipPlacement(tempPlacingPosition);
        } else if(keyCode == KeyEvent.VK_D) {
            debugModeActive = !debugModeActive; //D to active the debug mode
        }
        repaint();
    }
    /**
     * Resets all the class's properties back to their defaults ready for a new game to begin.
     */
    public void restart() {
        // Reset the score and the process
        statusPanel.setCompHitCount(0);
        statusPanel.setPlayerHitCount(0);
        compCount=0;
        playerCount =0;
        computer.reset();
        player.reset();

        // Player can see their own ships by default
        player.setShowShips(true);
        aiController.reset();

        // Reset all the features to default
        tempPlacingPosition = new Position(0,0);
        placingShip = new Ship(new Position(0,0),
                new Position(player.getPosition().x,player.getPosition().y),
                SelectionGrid.BOAT_SIZES[0], true);
        placingShipIndex = 0;
        updateShipPlacement(tempPlacingPosition);
        computer.populateShips();
        debugModeActive = false;
        statusPanel.reset();
        gameState = GameState.PlacingShips;
        hasExtraTurn = false;
    }
    /**
     * Uses the mouse position to test update the ship being placed during the
     * PlacingShip state. Then if the place it has been placed is valid the ship will
     * be locked in by calling placeShip().
     * @param mousePosition Mouse coordinates inside the panel.
     */
    private void tryPlaceShip(Position mousePosition) {
        Position targetPosition = player.getPositionInGrid(mousePosition.x, mousePosition.y);
        updateShipPlacement(targetPosition);
        if(player.canPlaceShipAt(targetPosition.x, targetPosition.y,
                SelectionGrid.BOAT_SIZES[placingShipIndex],placingShip.isSideways())) {
            placeShip(targetPosition);
        }
    }
    /**
     * Finalises the insertion of the ship being placed by storing it in the player's grid.
     * Then either prepares the next ship for placing, or moves to the next state.
     * @param targetPosition The position on the grid to insert the ship at.
     */
    private void placeShip(Position targetPosition) {
        player.placeShip(placingShip,tempPlacingPosition.x,tempPlacingPosition.y);
        placingShipIndex++;
        // If there are still ships to place
        if(placingShipIndex < SelectionGrid.BOAT_SIZES.length) {
            placingShip = new Ship(new Position(targetPosition.x, targetPosition.y),
                    new Position(player.getPosition().x + targetPosition.x * SelectionGrid.CELL_SIZE,
                            player.getPosition().y + targetPosition.y * SelectionGrid.CELL_SIZE),
                    SelectionGrid.BOAT_SIZES[placingShipIndex], true);
            updateShipPlacement(tempPlacingPosition);
        } else {
            gameState = GameState.FiringShots;
            statusPanel.setTopLine("ATTACK THE ENEMY!");
            statusPanel.setBottomLine("DESTROY ALL SHIPS TO WIN!");
        }
    }
    /**
     * Attempts to fire at a position on the computer's board.
     * The player is notified if they hit/missed, or nothing if they
     * have clicked the same place again. After the player's turn,
     * the AI is given a turn if the game is not already ended.
     * @param mousePosition Mouse coordinates inside the panel.
     */
    private void tryFireAtComputer(Position mousePosition) {
        Position targetPosition = computer.getPositionInGrid(mousePosition.x,mousePosition.y);

        // Ignore if position was already clicked
        if (targetPosition.x < 1 || targetPosition.y < 1) {
            return;
        }

        // Play the sound
        PlaySound.playSound("shoot.wav");

        if(!computer.isPositionMarked(targetPosition)) {
            doPlayerTurn(targetPosition);
            // Only do the AI turn if the game didn't end from the player's turn and player didn't have extra turn from treasure.
            if(!computer.areAllShipsDestroyed() && !hasExtraTurn) {
                doAITurn();
            }
            hasExtraTurn = false;
        }
    }
    /**
     * Sets the state to indicate that an extra turn is available.
     * This method is used to signal an additional turn opportunity.
     */
    public void extraTurn() {
        hasExtraTurn = true;
    }
    /**
     * Processes the player's turn based on where they selected to attack.
     * Based on the result of the attack a message is displayed to the player,
     * and if they destroyed the last ship the game updates to a won state.
     * @param targetPosition The grid position clicked on by the player.
     */
    private void doPlayerTurn(Position targetPosition) {
        boolean hit = computer.markPosition(targetPosition, true);
        boolean hitTreasure = computer.isTreasureAtPosition(targetPosition);
        String statusMessage = "";

        // Handle the situation that player hit a ship
        if (hit) {
            playerCount++; // Update score
            statusPanel.setPlayerHitCount(playerCount);
        }
        // Handle the situation that player hit a treasure
        if(hitTreasure) {
            computer.markTreasureAsOpened(targetPosition); // Mark the treasure state as opened
            statusMessage = "TREASURE FOUND! YOU HAVE 1 MORE MOVE!!";
            extraTurn(); // The player gained extra turn
            // Timer to manage the sound of treasure to prevent overlapping with the shooting sound
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    PlaySound.playSound("treasure.wav");
                }
            }, 50);
        }

        // Hit/Miss, destroyed message
        String hitMiss = hit ? "HIT!" : "MISSED!";
        String destroyed = "";

        // Draw a marker at a cell when player shoot
        Marker marker = computer.getMarkerAtPosition(targetPosition);

        // Handle destroyed ships
        if(hit && marker.getAssociatedShip() != null && marker.getAssociatedShip().isDestroyed()) {
            destroyed = "ENEMY'S SHIP HAS SUNK!";
        }

        // Display message
        statusPanel.setTopLine(statusMessage + " YOU " + hitMiss + " " + destroyed);

        // Checking if the player win
        if(computer.areAllShipsDestroyed()) {
            gameState = GameState.GameOver;
            statusPanel.showGameOver(true);
            PlayVideo.playVideo("toothless.mp4"); // Play video
        }
    }
    /**
     * Processes the AI turn by using the AI Controller to select a move.
     * Then processes the result to display it to the player. If the AI
     * destroyed the last ship the game will end with AI winning.
     */
    private void doAITurn() {
        // Select move
        Position aiMove = aiController.selectMove();
        boolean hit = player.markPosition(aiMove, false);

        // Message
        String hitMiss = hit ? "HIT!" : "MISSED!";
        String destroyed = "";

        // Handle hit situation
        if(hit) {
            compCount++; // Update score
            statusPanel.setCompHitCount(compCount);
        }

        // Draw a marker when the computer shoot
        Marker marker = player.getMarkerAtPosition(aiMove);
        if(hit && marker.getAssociatedShip() != null && marker.getAssociatedShip().isDestroyed()) {
            destroyed = "YOUR SHIP HAS SUNK!";
        }

        // Display message
        statusPanel.setBottomLine("ENEMY " + hitMiss + " " + destroyed);

        // Checking if the computer win
        if(player.areAllShipsDestroyed()) {
            // Computer wins!
            gameState = GameState.GameOver;
            statusPanel.showGameOver(false);
            PlayVideo.playVideo("meme12.mp4"); // Play video
        }
    }
    /**
     * Updates the ship being placed location if the mouse is inside the grid.
     * @param mousePosition Mouse coordinates inside the panel.
     */
    private void tryMovePlacingShip(Position mousePosition) {
        if(player.isPositionInside(mousePosition)) {
            Position targetPos = player.getPositionInGrid(mousePosition.x, mousePosition.y);
            updateShipPlacement(targetPos);
        }
    }
    /**
     * Constrains the ship to fit inside the grid. Updates the drawn position of the ship,
     * and changes the colour of the ship based on whether it is a valid or invalid placement.
     * @param targetPos The grid coordinate where the ship being placed should change to.
     */
    private void updateShipPlacement(Position targetPos) {
        // Constrain to fit inside the grid
        if(placingShip.isSideways()) {
            targetPos.x = Math.min(targetPos.x, SelectionGrid.GRID_WIDTH - SelectionGrid.BOAT_SIZES[placingShipIndex]);
        } else {
            targetPos.y = Math.min(targetPos.y, SelectionGrid.GRID_HEIGHT - SelectionGrid.BOAT_SIZES[placingShipIndex]);
        }

        // Update drawing position to use the new target position
        placingShip.setDrawPosition(new Position(targetPos),
                new Position(player.getPosition().x + targetPos.x * SelectionGrid.CELL_SIZE,
                        player.getPosition().y + targetPos.y * SelectionGrid.CELL_SIZE));

        // Store the grid position for other testing cases
        tempPlacingPosition = targetPos;

        // Change the colour of the ship based on whether it could be placed at the current location.
        if(player.canPlaceShipAt(tempPlacingPosition.x, tempPlacingPosition.y,
                SelectionGrid.BOAT_SIZES[placingShipIndex],placingShip.isSideways())) {
        }
    }
    /**
     * Triggered when the mouse button is released. If in the PlacingShips state and the
     * cursor is inside the player's grid it will try to place the ship.
     * Otherwise if in the FiringShots state and the cursor is in the computer's grid,
     * it will try to fire at the computer.
     * @param e Details about where the mouse event occurred.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        Position mousePosition = new Position(e.getX(), e.getY());
        if(gameState == GameState.PlacingShips && player.isPositionInside(mousePosition)) {
            tryPlaceShip(mousePosition);
        } else if(gameState == GameState.FiringShots && computer.isPositionInside(mousePosition)) {
            tryFireAtComputer(mousePosition);
        }
        repaint();
    }
    /**
     * Triggered when the mouse moves inside the panel. Does nothing if not in the PlacingShips state.
     * Will try and move the ship that is currently being placed based on the mouse coordinates.
     * @param e Details about where the mouse event occurred.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if(gameState != GameState.PlacingShips) return;
        tryMovePlacingShip(new Position(e.getX(), e.getY()));
        repaint();
    }
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void mouseClicked(MouseEvent e) {}
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void mousePressed(MouseEvent e) {}
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void mouseEntered(MouseEvent e) {}
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void mouseExited(MouseEvent e) {}
    /**
     * Not used.
     * @param e Not used.
     */
    @Override
    public void mouseDragged(MouseEvent e) {}
}
