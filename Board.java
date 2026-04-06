package model;

import model.tiles.Tile;
import model.tiles.TileType;

import java.io.Serializable;
import java.util.*;
/**
 * represents the game board.
 * the board is divided into regions based on tile type.
 */

public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    // maps each tile type to its corresponding region
    private final Map<TileType, List<Tile>> regions;

    /**
     * constructs an empty board with one region per tile type.
     * postcondition: all regions are initialized and empty
     */
    public Board() {
        regions = new EnumMap<>(TileType.class);

        // initialize empty region for each tile type
        for (TileType type : TileType.values()) {
            regions.put(type, new ArrayList<>());
        }
    }

    /**
     * places a tile in its corresponding region.
     * precondition: tile != null
     * postcondition: tile added to its region
     */
    public void placeTile(Tile tile) {

        if (tile == null) return;

        TileType type = tile.getType();
        if (type == null) return;

        List<Tile> region = regions.get(type);

        // safety check: region should always exist
        if (region == null) {
            region = new ArrayList<>();
            regions.put(type, region);
        }

        region.add(tile);
    }

    /**
     * removes a number of tiles from a region.
     * precondition: count more or equal to 0
     * postcondition: up to count tiles removed from region
     * @return list of removed tiles
     */
    public List<Tile> removeTiles(TileType type, int count) {

        List<Tile> region = regions.get(type);
        List<Tile> removed = new ArrayList<>();

        if (region == null) return removed;

        // remove tiles from the end of the region
        for (int i = 0; i < count && !region.isEmpty(); i++) {
            removed.add(region.remove(region.size() - 1));
        }

        return removed;
    }

    /**
     * returns a copy of the tiles in a region.
     * @return list of tiles in the given region
     */
    public List<Tile> getTiles(TileType type) {

        List<Tile> region = regions.get(type);

        // return copy to protect internal structure
        return region == null ? new ArrayList<>() : new ArrayList<>(region);
    }

    /**
     * returns the number of tiles in a region.
     * @return region size
     */
    public int getRegionSize(TileType type) {

        List<Tile> region = regions.get(type);
        return region == null ? 0 : region.size();
    }

    /**
     * checks if the landslide region is full.
     * @return true if landslide has at least 16 tiles
     */
    public boolean isLandslideFull() {
        return getRegionSize(TileType.LANDSLIDE) >= 16;
    }

    /**
     * clears all regions except landslide.
     * used in single-player thief logic.
     * postcondition: all non-landslide regions are empty
     */
    public void clearAllAreasExceptLandslide() {

        for (TileType type : TileType.values()) {
            if (type != TileType.LANDSLIDE) {
                regions.get(type).clear();
            }
        }
    }

    /**
     * removes and returns all tiles except landslide tiles.
     * used when the thief steals tiles in one-player mode.
     * postcondition: all non-landslide regions are empty
     * @return list of stolen tiles
     */
    public List<Tile> getAllTilesExceptLandslide() {

        List<Tile> stolen = new ArrayList<>();

        for (TileType type : TileType.values()) {
            if (type != TileType.LANDSLIDE) {
                stolen.addAll(removeTiles(type, getRegionSize(type)));
            }
        }

        return stolen;
    }
}
