package model;

import model.specialCards.CharacterCard;
import model.tiles.Tile;
import model.tiles.TileType;

import java.io.Serializable;
import java.util.*;

/**
 * represents a player of the game.
 * stores collected tiles, character cards and bonus state.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    // player name
    private String name;

    // tiles collected by the player
    private List<Tile> takenTiles;

    // available character cards
    private List<CharacterCard> characters;

    // reserved area for programmer (csdas) card
    private TileType reservedArea;

    // bonus score gained from cards
    private int bonusScore = 0;

    /**
     * constructs a player with the given name.
     * precondition: name != null
     * postcondition: player initialized with empty collections
     */
    public Player(String name) {
        this.name = name;
        takenTiles = new ArrayList<>();
        characters = new ArrayList<>();
    }

    /**
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * adds a tile to the player's collection.
     * precondition: tile != null
     * postcondition: tile added to taken tiles
     */
    public void addTile(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException();
        }
        takenTiles.add(tile);
    }

    /**
     * returns a copy of collected tiles.
     * @return list of taken tiles
     */
    public List<Tile> getTakenTiles() {
        return new ArrayList<>(takenTiles);
    }

    /**
     * adds a character card to the player.
     * precondition: card != null
     * postcondition: card added to character list
     */
    public void addCard(CharacterCard card) {
        characters.add(card);
    }

    /**
     * returns a copy of the character cards.
     * @return list of character cards
     */
    public List<CharacterCard> getCards() {
        return new ArrayList<>(characters);
    }

    /**
     * counts collected tiles per tile type.
     * used mainly for scoring and ui.
     * @return map of tile type to count
     */
    public Map<TileType, Integer> countTiles() {

        Map<TileType, Integer> counts = new EnumMap<>(TileType.class);

        // initialize counters
        for (TileType t : TileType.values()) {
            counts.put(t, 0);
        }

        // count tiles
        for (Tile t : takenTiles) {
            counts.put(t.getType(),
                    counts.get(t.getType()) + 1);
        }

        return counts;
    }

    /**
     * sets the reserved area for programmer card.
     */
    public void setReservedArea(TileType area) {
        this.reservedArea = area;
    }

    /**
     * @return reserved area or null if none
     */
    public TileType getReservedArea() {
        return reservedArea;
    }

    /**
     * adds multiple tiles to the player.
     * postcondition: all tiles added to taken tiles
     */
    public void addTiles(List<Tile> tiles) {

        if (tiles == null) return;

        for (Tile t : tiles) {
            addTile(t);
        }
    }

    /**
     * adds bonus score from card effects.
     * postcondition: bonus score increased
     */
    public void addBonusScore(int s) {
        bonusScore += s;
    }

    /**
     * @return total bonus score
     */
    public int getBonusScore() {
        return bonusScore;
    }
}

