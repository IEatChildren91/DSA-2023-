import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Battleship
 * Author: Kudo
 *
 * The GamePanel class oversees the state information and interactions among various game elements.
 * It manages two game grids: one for the human player and another for the computer.
 * There is a status panel located between these two grids.
 * The status panel displays the current state of the game.
 * The player's interaction with the game changes based on the current state.
 * During the ship placement phase, the player can place their ships on their grid.
 * In the attack phase, the player can target the computer's grid.
 * The status panel keeps the player informed about the progress and comparative state of the game.
 * It provides an overview of the current situation on both the player's and the computer's grids.
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    /**
     * The GameStates influence how a player can interact with the game at any given moment.
     * During the "PlacingShips" state, the player is permitted to position their ships on their grid.
     * The "PlacingShips" state concludes when all the player's ships have been placed on the grid.
     * When the game transitions into the "FiringShots" state, the player can launch attacks on the computer's grid and subsequently receive responses.
     * The "FiringShots" state concludes once all the ships present on either the player's or the computer's grid have been sunk.
     * The "GameOver" state initiates when either the player's fleet or the computer's fleet has been entirely destroyed.
     * During the "GameOver" state, no further input is accepted to prevent unintended actions.
     * The "GameOver" state ends when the player chooses to either exit the game or start a new one.
     */
    public enum GameState { PlacingShips, FiringShots, GameOver }
    private int compCount;
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
    public boolean hasExtraTurn;
    public boolean aiHasExtraTurn;
    private BufferedImage radarBG;

    /**
     * Initialises everything necessary to begin playing the game. The grids for each player are initialised and
     * then used to determine how much space is required. The listeners are attached, AI configured, and
     * everything set to begin the game with placing a ship for the player.
     */
    /*private void drawSonarScreen(Graphics g, int gridX, int gridY, int gridWidth, int gridHeight) {
        Graphics2D g2d = (Graphics2D) g; // Cast to Graphics2D for more control

        int centerX = gridX + gridWidth / 2;
        int centerY = gridY + gridHeight / 2;
        int radius = 50*4 ;

        // Set the transparency
        float alpha = 0.75f; // Adjust this value for desired transparency (0.0 to 1.0)
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);

        g2d.setComposite(ac);
        float thickness = 0.55f; // Adjust this value for desired thickness
        g2d.setStroke(new BasicStroke(thickness));

        // Draw the sonar circles
        g2d.setColor(new Color(0, 255, 0)); // Green color for sonar
        int numCircles = 8; // Increased to 7 circles
        for (int i = 1; i <= numCircles; i++) {
            int circleRadius = i * radius / numCircles; // Adjusted for smaller gaps
            g2d.drawOval(centerX - circleRadius + 25, centerY - circleRadius+25, 2 * circleRadius, 2 * circleRadius);
        }


        // Draw the sonar lines
        for (int i = 45; i < 360; i += 90) {
            double rad = Math.toRadians(i);
            int lineX = centerX + (int) (radius * Math.cos(rad));
            int lineY = centerY + (int) (radius * Math.sin(rad));
            g2d.drawLine(centerX, centerY, lineX, lineY);
        }

        // Reset to default composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }*/
    private void drawRadarBackground(Graphics g, int gridX, int gridY, int gridWidth, int gridHeight) {
        if (radarBG != null) {
            int imageWidth = 50*10 + 200;
            int imageHeight = 50*9-35;
            int newX = gridX + (gridWidth - imageWidth) / 2 + 15; // Move it 50 pixels to the right
            int newY = gridY + (gridHeight - imageHeight) / 2 + 25; // Center it vertically and move it up by 25 pixels
            Graphics2D g2d = (Graphics2D) g;
            float alpha = 0.7f; // 0.0f is fully transparent, 1.0f is fully opaque
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(alphaComposite);
            g2d.drawImage(radarBG, newX, newY, imageWidth, imageHeight, null);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }
    public static void playSound(String soundFileName) {
        try {
            File soundFile = new File(soundFileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public GamePanel(int aiChoice) {
        try {
            radarBG = ImageIO.read(new File("radar.png")); // Load the radar image
        } catch (IOException e) {
            e.printStackTrace();
        }
        int gap = 60; // Gap between the two grids
        computer = new SelectionGrid(0, 0);
        player = new SelectionGrid(computer.getWidth() + gap, 0);
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        int totalWidth = computer.getWidth() + player.getWidth() + gap;
        int maxHeight = Math.max(computer.getHeight(), player.getHeight());
        setPreferredSize(new Dimension(totalWidth, maxHeight + 150));
        addMouseListener(this);
        addMouseMotionListener(this);
        if (aiChoice == 0) {
            aiController = new SimpleRandomAI(player);
        } else {
            aiController = new SmarterAI(player, aiChoice == 2, aiChoice == 2);
        }
        statusPanel = new StatusPanel(new Position(0, maxHeight), totalWidth, 49);
        hasExtraTurn = false;
        aiHasExtraTurn = false;
        restart();
    }

    /**
     * Draws the grids for both players, any ship being placed, and the status panel.
     *
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
     * D activates the debug mode to show computer ships.
     *
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
        computer.reset();
        player.reset();
        // Player can see their own ships by default
        player.setShowShips(true);
        aiController.reset();
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
        aiHasExtraTurn = false;
    }

    /**
     * Uses the mouse position to test update the ship being placed during the
     * PlacingShip state. Then if the place it has been placed is valid the ship will
     * be locked in by calling placeShip().
     *
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
     *
     * @param targetPosition The position on the grid to insert the ship at.
     */
    private void placeShip(Position targetPosition) {
        placingShip.setShipPlacementColour(Ship.ShipPlacementColour.Placed);
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
     *
     * @param mousePosition Mouse coordinates inside the panel.
     */
    private void tryFireAtComputer(Position mousePosition) {

        Position targetPosition = computer.getPositionInGrid(mousePosition.x,mousePosition.y);
        // Ignore if position was already clicked
        if (targetPosition.x < 1 || targetPosition.y < 1) {
            return;
        }
        playSound("shoot.wav");
        if(!computer.isPositionMarked(targetPosition)) {
            doPlayerTurn(targetPosition);
            // Only do the AI turn if the game didn't end from the player's turn.
            if(!computer.areAllShipsDestroyed() && !hasExtraTurn) {
                doAITurn();
            }
            hasExtraTurn = false;
        }
    }
    public void extraTurn() {
        hasExtraTurn = true;
    }
    public void aiExtraTurn() {aiHasExtraTurn = true;}

    /**
     * Processes the player's turn based on where they selected to attack.
     * Based on the result of the attack a message is displayed to the player,
     * and if they destroyed the last ship the game updates to a won state.
     *
     * @param targetPosition The grid position clicked on by the player.
     */
    private void doPlayerTurn(Position targetPosition) {
        boolean hit = computer.markPosition(targetPosition);
        boolean hitTreasure = computer.isTreasureAtPosition(targetPosition);
        String statusMessage = "";
        if (hit) {
            playerCount++;
            statusPanel.setPlayerHitCount(playerCount);
        }
        if(hitTreasure) {
            statusMessage = "TREASURE FOUND! YOU HAVE 1 MORE MOVE!!";
            extraTurn();
            //doPlayerTurn(targetPosition);
            playSound("treasure.wav"); // Optionally play a sound
        }
        String hitMiss = hit ? "HIT!" : "MISSED!";
        String destroyed = "";

        Marker marker = computer.getMarkerAtPosition(targetPosition);
        if(hit && marker.getAssociatedShip() != null && marker.getAssociatedShip().isDestroyed()) {
            destroyed = "ENEMY'S SHIP HAS SUNK!";
        }
        statusPanel.setTopLine(statusMessage + " YOU " + hitMiss + " " + destroyed);

        if(computer.areAllShipsDestroyed()) {
            playSound("win.wav");
            gameState = GameState.GameOver;
            statusPanel.showGameOver(true);
        }
    }


    /**
     * Processes the AI turn by using the AI Controller to select a move.
     * Then processes the result to display it to the player. If the AI
     * destroyed the last ship the game will end with AI winning.
     */
    private void doAITurn() {
        Position aiMove = aiController.selectMove();
        boolean hit = player.markPosition(aiMove);
        boolean hitTreasure = player.isTreasureAtPosition(aiMove);
        String hitMiss = hit ? "HIT!" : "MISSED!";
        String destroyed = "";
        //String statusMessage = "";
        if(hit) {
            compCount++;
            statusPanel.setCompHitCount(compCount);
        }
        if(hitTreasure) {
            aiExtraTurn();
            //statusMessage = "TREASURE FOUND! ENEMY HAS 1 MORE MOVE!";
        }
        Marker marker = player.getMarkerAtPosition(aiMove);
        if(hit && marker.getAssociatedShip() != null && marker.getAssociatedShip().isDestroyed()) {
            destroyed = "YOUR SHIP HAS SUNK!";
        }
        statusPanel.setBottomLine(/*statusMessage */"ENEMY " + hitMiss + " " + destroyed);
        if(player.areAllShipsDestroyed()) {
            // Computer wins!
            playSound("lose.wav");
            gameState = GameState.GameOver;
            statusPanel.showGameOver(false);
        } else if (aiHasExtraTurn && !player.areAllShipsDestroyed()) {
            aiHasExtraTurn = false;
            doAITurn();
        }
    }
    /**
     * Updates the ship being placed location if the mouse is inside the grid.
     *
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
     *
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
            placingShip.setShipPlacementColour(Ship.ShipPlacementColour.Valid);
        } else {
            placingShip.setShipPlacementColour(Ship.ShipPlacementColour.Invalid);
        }
    }

    /**
     * Triggered when the mouse button is released. If in the PlacingShips state and the
     * cursor is inside the player's grid it will try to place the ship.
     * Otherwise if in the FiringShots state and the cursor is in the computer's grid,
     * it will try to fire at the computer.
     *
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
     *
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
     *
     * @param e Not used.
     */
    @Override
    public void mouseClicked(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mousePressed(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseEntered(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseExited(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseDragged(MouseEvent e) {}
}
