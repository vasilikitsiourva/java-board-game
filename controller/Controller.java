package controller;
import model.GameState;
import model.Player;
import model.specialCards.Assistant;
import model.specialCards.CharacterCard;
import model.tiles.Tile;
import model.tiles.TileType;
import view.GraphicUI;
import javax.swing.SwingUtilities;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * controller of the game.
 * coordinates game flow between model and view.
 * does not contain game rules.
 */
public class Controller {

    // reference to game state to model
    private GameState gameState;

    // reference to view
    private GraphicUI view;

    // game flow flags
    private boolean gameStarted;
    private boolean tilesDrawn;
    private boolean turnHasEnded;
    private boolean hasPickedTiles;
    private boolean assistantActive;
    private boolean cardUsedThisTurn;

    // timer state
    private int timeLeft;
    private Thread timerThread;
    private volatile boolean timerActive;

    private int playerCount;

    /**
     * creates controller and initializes game state.
     */
    public Controller() {
        this.gameState = new GameState();
    }

    /**
     * sets the graphical view.
     * precondition: view != null
     * postcondition: controller can update ui
     */
    public void setView(GraphicUI view) {
        this.view = view;
    }

    /**
     * @return current game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * starts a new game.
     * precondition: playerCount must be 1 or 4
     * postcondition: game initialized and timer started
     */
    public void startNewGame(int playerCount) {

        this.playerCount = playerCount;

        // create default player names
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            names.add("Player " + i);
        }

        // initialize model
        gameState.initializeGame();
        gameState.setupPlayers(names);

        // one player mode: place initial landslides
        if (playerCount == 1) {
            gameState.placeInitialLandslides();
        }

        // reset controller flags
        gameStarted = true;
        tilesDrawn = false;
        turnHasEnded = false;
        hasPickedTiles = false;
        cardUsedThisTurn = false;

        // start turn timer
        startTurnTimer();

        if (view != null) {
            view.refreshBoard();
        }
    }

    /**
     * draws tiles from bag to board.
     * handles single player and 4 player logic.
     * precondition: game has started
     */
    public void drawTiles() {

        if (!gameStarted || tilesDrawn || turnHasEnded) return;

        // one player mode
        if (playerCount == 1) {

            // returns true if landslide was drawn
            if (gameState.drawTileToBoardSinglePlayer()) {

                if (gameState.isGameOver()) {
                    if (view != null) view.refreshBoard();
                    handleGameOver();
                    return;
                }

                // thief ends turn immediately
                endTurn();
                return;
            }

        } else {
            // multi-player draws 4 tiles
            for (int i = 0; i < 4; i++) {
                gameState.drawTileToBoard();

                if (gameState.isGameOver()) {
                    if (view != null) view.refreshBoard();
                    handleGameOver();
                    return;
                }
            }
        }

        tilesDrawn = true;

        if (view != null) view.refreshBoard();
    }

    /**
     * handles end of game logic.
     * computes scores and shows result.
     */
    private void handleGameOver() {

        stopTurnTimer();

        StringBuilder sb = new StringBuilder("GAME OVER!\n\n");

        // one player mode
        if (gameState.getPlayers().size() == 1) {

            Player p = gameState.getPlayers().get(0);

            int playerScore = gameState.computeScore(p);
            int thiefScore = gameState.getThiefScore();

            sb.append(p.getName()).append(": ")
                    .append(playerScore).append(" points\n");

            sb.append("Thief: ").append(thiefScore)
                    .append(" points\n\n");

            if (playerScore > thiefScore) sb.append("YOU WIN!");
            else if (playerScore < thiefScore) sb.append("THIEF WINS!");
            else sb.append("IT'S A DRAW!");

            if (view != null) view.showGameOver(sb.toString());
            return;
        }

        // multi-player mode
        Map<Player, Integer> scores = gameState.computeFinalScores();

        int maxScore = -1;
        String winner = "";

        for (Player p : scores.keySet()) {

            int score = scores.get(p);

            sb.append(p.getName()).append(": ")
                    .append(score).append(" points\n");

            if (score > maxScore) {
                maxScore = score;
                winner = p.getName();
            } else if (score == maxScore) {
                winner += " & " + p.getName();
            }
        }

        sb.append("\nWINNER: ").append(winner);

        if (view != null) view.showGameOver(sb.toString());
    }

    /**
     * selects tiles from a board area.
     * precondition: tiles have been drawn
     * postcondition: tiles added to current player
     */
    public List<Tile> selectTiles(TileType area, int count) {

        if (!gameStarted || !tilesDrawn || turnHasEnded) {
            return new ArrayList<>();
        }

        TileType takenArea = gameState.getTakenAreaThisTurn();

        // assistant rule: only one tile
        if (assistantActive && count > 1) return new ArrayList<>();

        // cannot switch area without assistant
        if (takenArea != null && takenArea != area && !assistantActive) {
            return new ArrayList<>();
        }

        // normal validation
        if (!assistantActive && !gameState.canTakeTiles(area, count)) {
            return new ArrayList<>();
        }

        // assistant ignores normal rules but checks availability
        if (assistantActive &&
                gameState.getBoard().getRegionSize(area) < count) {
            return new ArrayList<>();
        }

        List<Tile> taken = gameState.takeTiles(area, count);
        if (taken.isEmpty()) return new ArrayList<>();

        if (assistantActive) {
            assistantActive = false; // consumed
        } else {
            gameState.setTakenAreaThisTurn(area);
        }

        hasPickedTiles = true;
        return taken;
    }

    /**
     * uses a character card.
     * precondition: card not used
     * postcondition: card effects applied
     */
    public void useCharacterCard(CharacterCard card, TileType area) {

        if (!gameStarted || turnHasEnded || card.isUsed() || cardUsedThisTurn) return;

        // digger must follow tile selection
        if (card instanceof model.specialCards.Digger &&
                gameState.getTakenAreaThisTurn() == null) {
            return;
        }

        if (card instanceof Assistant) assistantActive = true;

        if (area != null) gameState.setSelectedArea(area);

        gameState.useCard(card);

        if (!(card instanceof Assistant)) {
            cardUsedThisTurn = true;
        }
    }

    /**
     * ends current players turn.
     * resets flags and moves to next player.
     */
    public void endTurn() {

        stopTurnTimer();
        gameState.nextPlayer();

        // programmer (csdas) automatic action
        Player current = gameState.getCurrentPlayer();
        TileType reserved = current.getReservedArea();

        if (reserved != null) {
            int available = gameState.getBoard().getRegionSize(reserved);
            int count = Math.min(2, available);

            if (count > 0) {
                current.addTiles(
                        gameState.getBoard().removeTiles(reserved, count)
                );
            }

            current.setReservedArea(null);
        }

        // reset turn state
        tilesDrawn = false;
        turnHasEnded = false;
        hasPickedTiles = false;
        assistantActive = false;
        cardUsedThisTurn = false;
        gameState.setTakenAreaThisTurn(null);

        startTurnTimer();

        if (view != null) {
            view.refreshPlayer();
            view.refreshBoard();
        }
    }

    /**
     * starts turn countdown timer.
     */
    private void startTurnTimer() {

        stopTurnTimer();
        timeLeft = 30;
        timerActive = true;

        if (view != null) {
            SwingUtilities.invokeLater(() ->
                    view.updateTimer(timeLeft)
            );
        }

        timerThread = new Thread(() -> {
            try {
                while (timeLeft > 0 && timerActive) {
                    Thread.sleep(1000);
                    if (!timerActive) return;

                    timeLeft--;

                    if (view != null) {
                        SwingUtilities.invokeLater(() ->
                                view.updateTimer(timeLeft)
                        );
                    }
                }

                if (timerActive) {
                    SwingUtilities.invokeLater(this::endTurn);
                }

            } catch (InterruptedException ignored) {}
        });

        timerThread.start();
    }

    /**
     * stops the running timer.
     */
    private void stopTurnTimer() {
        timerActive = false;
        if (timerThread != null) timerThread.interrupt();
    }

    // ===== getters for view =====

    public boolean isGameStarted() { return gameStarted; }
    public boolean hasDrawnTiles() { return tilesDrawn; }
    public boolean hasUsedCard() { return cardUsedThisTurn; }
    public boolean hasUsedCardThisTurn() { return cardUsedThisTurn; }

    public Player getCurrentPlayer() { return gameState.getCurrentPlayer(); }

    public int getCurrentPlayerScore() {
        return gameState.computeScore(gameState.getCurrentPlayer());
    }

    public int getTilesInArea(TileType type) {
        return gameState.getBoard().getRegionSize(type);
    }

    public List<Tile> getTilesInAreaDetailed(TileType type) {
        return gameState.getBoard().getTiles(type);
    }

    public TileType getTakenAreaThisTurn() {
        return gameState.getTakenAreaThisTurn();
    }

    public boolean isAssistantActive() {
        return assistantActive;
    }

    /**
     * saves game state to file.
     * precondition: file != null
     */
    public void saveGame(File file) throws IOException {

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(gameState);
        }
    }

    /**
     * loads game state from file.
     * precondition: file exists
     * postcondition: game restored
     */
    public void loadGame(File file)
            throws IOException, ClassNotFoundException {

        stopTurnTimer();

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {
            gameState = (GameState) in.readObject();
        }

        // reset controller flags
        tilesDrawn = false;
        turnHasEnded = false;
        hasPickedTiles = false;
        assistantActive = false;
        cardUsedThisTurn = false;

        startTurnTimer();

        if (view != null) {
            view.refreshPlayer();
            view.refreshBoard();
        }
    }
}
