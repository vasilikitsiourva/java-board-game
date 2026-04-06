package model;


import model.tiles.*;

import java.awt.*;

import java.util.*;
import java.util.List;
import java.io.Serializable;

/**
 * represents the bag of the game.
 * the bag contains all tiles and allows random drawing.
 */
public class Bag implements Serializable {

    private static final long serialVersionUID = 1L;

    // list of all remaining tiles
    private List<Tile> tiles;

    // random generator for drawing tiles
    private Random rand;

    /**
     * constructs an empty bag.
     * postcondition: tiles list is empty
     */
    public Bag() {
        tiles = new ArrayList<>();
        rand = new Random();
    }

    /**
     * initializes the bag with all game tiles.
     * clears previous contents and shuffles the bag.
     * postcondition: tiles.size() == 135
     */
    public void initializeTiles() {

        removeAllTiles();

        // mosaics: 3 colors, 9 each
        Color[] mosaicColors = {
                Color.GREEN,
                Color.YELLOW,
                Color.RED
        };

        for (Color c : mosaicColors) {
            for (int i = 0; i < 9; i++) {
                tiles.add(new Mosaic(c));
            }
        }

        // amphoras: 6 colors, 5 each
        Color[] amphoraColors = {
                Color.GREEN,
                Color.YELLOW,
                Color.RED,
                Color.MAGENTA,
                new Color(139, 95, 19), // brown
                Color.BLUE
        };

        for (Color c : amphoraColors) {
            for (int i = 0; i < 5; i++) {
                tiles.add(new Amphora(c));
            }
        }

        // skeletons: big and small (top & bottom)
        for (int i = 0; i < 10; i++) {
            tiles.add(new Skeleton(true, false, TypeOfSkeleton.BIGSKELETON));
            tiles.add(new Skeleton(false, true, TypeOfSkeleton.BIGSKELETON));
        }

        for (int i = 0; i < 5; i++) {
            tiles.add(new Skeleton(true, false, TypeOfSkeleton.LITTLESKELETON));
            tiles.add(new Skeleton(false, true, TypeOfSkeleton.LITTLESKELETON));
        }

        // statues: 12 of each type
        for (int i = 0; i < 12; i++) {
            tiles.add(new Statue(TypeOfStatue.CARYATID));
            tiles.add(new Statue(TypeOfStatue.SPHINX));
        }

        // landslides: 24 tiles
        for (int i = 0; i < 24; i++) {
            tiles.add(new LandSlide());
        }

        shuffleTiles();
    }

    /**
     * draws and removes the first tile of a specific category.
     * used during game initialization.
     * precondition: at least one tile of the given category exists
     * @return the drawn tile
     */
    public Tile drawTileOfCategory(TileType category) {

        for (Tile t : tiles) {
            if (t.getType() == category) {
                tiles.remove(t);
                return t;
            }
        }

        throw new IllegalStateException(
                "no tile of category " + category
        );
    }

    /**
     * draws and removes a random tile from the bag.
     * precondition: tiles.size() more than 0
     * @return the drawn tile, or null if bag is empty
     */
    public Tile getRandomTile() {

        if (tiles.isEmpty()) return null;

        int index = rand.nextInt(tiles.size());
        return tiles.remove(index);
    }

    /**
     * removes all tiles from the bag.
     * used mainly in one player mode.
     * postcondition: tiles.size() == 0
     */
    public void removeAllTiles() {
        tiles.clear();
    }

    /**
     * adds a tile back to the bag.
     * mainly used when loading a saved game.
     * precondition: tile != null
     * postcondition: tiles.size() increased by 1
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    /**
     * shuffles the tiles in the bag.
     * precondition: tiles != null
     * postcondition: tiles are randomly rearranged
     */
    public void shuffleTiles() {

        for (int i = tiles.size() - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);

            // swap tiles
            Tile temp = tiles.get(i);
            tiles.set(i, tiles.get(j));
            tiles.set(j, temp);
        }
    }

    /**
     * checks whether the bag is empty.
     * @return true if bag has no tiles
     */
    public boolean BagIsEmpty() {
        return tiles.isEmpty();
    }

    /**
     * returns a copy of the remaining tiles.
     * @return list of remaining tiles
     */
    public List<Tile> remainingTiles() {
        return new ArrayList<>(tiles);
    }

    /**
     * returns the number of remaining tiles.
     * @return tiles.size()
     */
    public int RemainingTiles() {
        return tiles.size();
    }
}

