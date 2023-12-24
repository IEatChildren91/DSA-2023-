import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
     * Initialises everything necessary to begin playing the game. The grids for each player are initialised and
     * then used to determine how much space is required. The listeners are attached, AI configured, and
     * everything set to begin the game with placing a ship for the player.
     */
    public GamePanel(int aiChoice) {
        computer = new SelectionGrid(0,0);
        player = new SelectionGrid(0,computer.getHeight()+50);
        setBackground(new Color(175, 238, 238));
        setPreferredSize(new Dimension(computer.getWidth(), player.getPosition().y + player.getHeight()));
        addMouseListener(this);
        addMouseMotionListener(this);
        if(aiChoice == 0) aiController = new SimpleRandomAI(player);
        else aiController = new SmarterAI(player,aiChoice == 2,aiChoice == 2);
        statusPanel = new StatusPanel(new Position(0,computer.getHeight()+1),computer.getWidth(),49);
        restart();

    }
    /**
     * Draws the grids for both players, any ship being placed, and the status panel.
     *
     * @param g Reference to the Graphics object for drawing.
     */
    public void paint(Graphics g) {
        super.paint(g);
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
            statusPanel.setTopLine("Attack the Enemy!");
            statusPanel.setBottomLine("Destroy all Ships to win!");
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
        if(!computer.isPositionMarked(targetPosition)) {
            doPlayerTurn(targetPosition);
            // Only do the AI turn if the game didn't end from the player's turn.
            if(!computer.areAllShipsDestroyed()) {
                doAITurn();
            }
        }
    }

    /**
     * Processes the player's turn based on where they selected to attack.
     * Based on the result of the attack a message is displayed to the player,
     * and if they destroyed the last ship the game updates to a won state.
     *
     * @param targetPosition The grid position clicked on by the player.
     */
    private void doPlayerTurn(Position targetPosition) {
        boolean hit = computer.markPosition(targetPosition);
        String hitMiss = hit ? "Hit" : "Missed";
        String destroyed = "";
        if(hit && computer.getMarkerAtPosition(targetPosition).getAssociatedShip().isDestroyed()) {
            destroyed = "(Sunk)";
        }
        statusPanel.setTopLine("You " + hitMiss + " " + targetPosition +" "+ destroyed);
        if(computer.areAllShipsDestroyed()) {
            // Player wins!
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
        String hitMiss = hit ? "Hit" : "Missed";
        String destroyed = "";
        if(hit && player.getMarkerAtPosition(aiMove).getAssociatedShip().isDestroyed()) {
            destroyed = "(Sunk)";
        }
        statusPanel.setBottomLine("Enemy " + hitMiss + " " + aiMove + " " + destroyed);
        if(player.areAllShipsDestroyed()) {
            // Computer wins!
            gameState = GameState.GameOver;
            statusPanel.showGameOver(false);
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
