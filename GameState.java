package model;

import model.specialCards.CharacterCard;
import model.tiles.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;




/**
 * represents the complete state of the game.
 * contains board, bag, players and scoring logic.
 * this class holds all game rules.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    // core game components
    private Board board;
    private Bag bag;
    private List<Player> players;

    // index of current player
    private int currentPlayer;

    // game status flags
    private boolean gameOver;

    // used by cards that require area selection
    private TileType selectedArea;

    // score accumulated by the thief (one player mode)
    private int thief_score;

    // area selected during current turn
    private TileType takenAreaThisTurn;

    /**
     * initializes a new game state.
     * creates board, bag and places initial tiles.
     * postcondition: board and bag initialized, game ready to start
     */
    public void initializeGame() {

        board = new Board();
        bag = new Bag();
        bag.initializeTiles();

        // initial placement (one tile per main area)
        board.placeTile(bag.drawTileOfCategory(TileType.MOSAIC));
        board.placeTile(bag.drawTileOfCategory(TileType.AMPHORA));
        board.placeTile(bag.drawTileOfCategory(TileType.SKELETON));
        board.placeTile(bag.drawTileOfCategory(TileType.STATUE));

        players = new ArrayList<>();
        currentPlayer = 0;
        gameOver = false;
        selectedArea = null;
        thief_score = 0;
    }

    /**
     * creates players and assigns character cards.
     * precondition: names.size() more or equal to 1
     * postcondition: players list initialized
     */
    public void setupPlayers(List<String> names) {

        players.clear();

        for (String playerName : names) {
            Player p = new Player(playerName);

            // assign character cards
            p.addCard(new model.specialCards.Archaeologist(p));
            p.addCard(new model.specialCards.Digger(p));
            p.addCard(new model.specialCards.Professor(p));
            p.addCard(new model.specialCards.Assistant(p));
            p.addCard(new model.specialCards.CSDas(p));

            players.add(p);
        }
    }

    /**
     * moves turn to the next player.
     * precondition: players.size() more than 0
     */
    public void nextPlayer() {
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    /**
     * @return current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayer);
    }

    /**
     * @return list of all players
     */
    public List<Player> getPlayers() {
        return players;
    }

    public Bag getBag() { return bag; }
    public Board getBoard() { return board; }

    /**
     * checks if tiles can be taken from an area.
     * precondition: area != null
     * @return true if rules allow tile selection
     */
    public boolean canTakeTiles(TileType area, int count) {

        if (area == null || count <= 0) return false;
        if (area == TileType.LANDSLIDE) return false;
        if (count > 2) return false;

        return board.getRegionSize(area) >= count;
    }

    /**
     * removes tiles from board and adds them to current player.
     * postcondition: tiles added to player inventory
     */
    public List<Tile> takeTiles(TileType area, int count) {

        List<Tile> taken = board.removeTiles(area, count);

        Player player = getCurrentPlayer();
        for (Tile t : taken) {
            player.addTile(t);
        }

        return taken;
    }

    /**
     * draws one tile from the bag and places it on the board.
     * postcondition: board updated, gameOver may be set
     */
    public void drawTileToBoard() {

        if (bag.BagIsEmpty()) return;

        Tile tile = bag.getRandomTile();
        board.placeTile(tile);

        // check end condition
        if (tile.getType() == TileType.LANDSLIDE &&
                board.getRegionSize(TileType.LANDSLIDE) >= 16) {
            gameOver = true;
        }
    }

    /**
     * draws tile in one player mode.
     * handles thief logic when landslide appears.
     * @return true if landslide was drawn
     */
    public boolean drawTileToBoardSinglePlayer() {

        if (bag.BagIsEmpty()) return false;

        Tile tile = bag.getRandomTile();
        board.placeTile(tile);

        if (tile.getType() == TileType.LANDSLIDE) {

            // thief steals all non-landslide tiles
            List<Tile> stolen = board.getAllTilesExceptLandslide();
            thief_score += stolen.size();

            if (board.isLandslideFull()) gameOver = true;
            return true;
        }

        return false;
    }

    /**
     * @return true if game has ended
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * applies a character card effect.
     * precondition: card not null and not used
     * postcondition: card marked as used
     */
    public void useCard(CharacterCard card) {

        if (card != null && !card.isUsed()) {
            card.useCard(this);
            card.disable();
        }
    }

    //  area selection for cards

    public void setSelectedArea(TileType area) {
        this.selectedArea = area;
    }

    public TileType getSelectedArea() {
        return selectedArea;
    }

    public void setCSDArea(TileType area) {
        this.selectedArea = area;
    }

    public TileType getCSDArea() {
        return selectedArea;
    }

    /**
     * clears board areas except landslide.
     * used in one player mode.
     */
    public void clearBoardAreas() {
        board.clearAllAreasExceptLandslide();
    }


    // scoring logic


    /**
     * computes base score for a player (without statue ranking).
     * @return score of player
     */
    public int computeScore(Player p) {

        int score = 0;
        List<Tile> tiles = p.getTakenTiles();

        // mosaics scoring
        int green = 0, red = 0, yellow = 0;

        for (Tile t : tiles) {
            if (t instanceof Mosaic) {
                Color c = ((Mosaic) t).getColor();
                if (c.equals(Color.GREEN)) green++;
                else if (c.equals(Color.RED)) red++;
                else if (c.equals(Color.YELLOW)) yellow++;
            }
        }

        score += (green / 4) * 4;
        score += (red / 4) * 4;
        score += (yellow / 4) * 4;

        // amphoras scoring
        Map<Color, Integer> amphoras = new HashMap<>();
        for (Tile t : tiles) {
            if (t instanceof Amphora) {
                Color c = ((Amphora) t).getColor();
                amphoras.put(c, amphoras.getOrDefault(c, 0) + 1);
            }
        }

        while (true) {

            int distinct = 0;
            List<Color> used = new ArrayList<>();

            for (Map.Entry<Color, Integer> e : amphoras.entrySet()) {
                if (e.getValue() > 0) {
                    distinct++;
                    used.add(e.getKey());
                }
            }

            if (distinct < 3) break;

            if (distinct == 3) score += 1;
            else if (distinct == 4) score += 2;
            else if (distinct == 5) score += 4;
            else score += 6;

            for (Color c : used) {
                amphoras.put(c, amphoras.get(c) - 1);
            }
        }

        // skeleton scoring
        int bigTop = 0, bigBot = 0, smallTop = 0, smallBot = 0;

        for (Tile t : tiles) {
            if (t instanceof Skeleton) {
                Skeleton s = (Skeleton) t;
                if (s.getSkeletonType() == TypeOfSkeleton.BIGSKELETON) {
                    if (s.isTopPart()) bigTop++; else bigBot++;
                } else {
                    if (s.isTopPart()) smallTop++; else smallBot++;
                }
            }
        }

        int big = Math.min(bigTop, bigBot);
        int small = Math.min(smallTop, smallBot);

        int families = 0;
        while (big >= 2 && small >= 1) {
            families++;
            big -= 2;
            small -= 1;
        }

        score += families * 6;
        score += big;
        score += small;

        // add bonus score from cards
        return score + p.getBonusScore();
    }

    /**
     * computes final scores including statue ranking.
     * @return map of players to final scores
     */
    public Map<Player, Integer> computeFinalScores() {

        Map<Player, Integer> scores = new HashMap<>();

        // base score
        for (Player p : players) {
            scores.put(p, computeScore(p));
        }

        // statue ranking scoring
        for (TypeOfStatue type : TypeOfStatue.values()) {

            int max = -1;
            int min = Integer.MAX_VALUE;
            Map<Player, Integer> count = new HashMap<>();

            for (Player p : players) {
                int c = 0;
                for (Tile t : p.getTakenTiles()) {
                    if (t instanceof Statue &&
                            ((Statue) t).getStatueType() == type) {
                        c++;
                    }
                }
                count.put(p, c);
                max = Math.max(max, c);
                min = Math.min(min, c);
            }

            for (Player p : players) {
                int c = count.get(p);
                if (c == max && max > 0) {
                    scores.put(p, scores.get(p) + 6);
                } else if (c != min) {
                    scores.put(p, scores.get(p) + 3);
                }
            }
        }

        return scores;
    }

    /**
     * @return total thief score (one player mode)
     */
    public int getThiefScore() {
        return thief_score;
    }

    public void setTakenAreaThisTurn(TileType area) {
        this.takenAreaThisTurn = area;
    }

    public TileType getTakenAreaThisTurn() {
        return takenAreaThisTurn;
    }

    /**
     * places initial landslide tiles for one player mode.
     * postcondition: landslide region contains 8 tiles
     */
    public void placeInitialLandslides() {
        for (int i = 0; i < 8; i++) {
            board.placeTile(new LandSlide());
        }
    }
}
