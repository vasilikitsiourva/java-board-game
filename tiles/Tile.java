package model.tiles;

import java.io.Serializable;

/**
 * abstract base class for all tiles.
 *
 * defines common properties shared by all tile types,
 * such as the tile type and basic identification methods.
 */
public abstract class Tile implements Serializable {

    /** serialization identifier */
    private static final long serialVersionUID = 1L;

    /** type of the tile */
    protected TileType type;

    /**
     * creates a tile with the given tile type.
     *
     * @param tileType the type of the tile
     *
     * precondition: tileType != null
     * postcondition: tile is created with the given type
     */
    public Tile(TileType tileType) {
        this.type = tileType;
    }

    /**
     * returns the type of this tile.
     *
     * @return tile type
     */
    public TileType getType() {
        return type;
    }

    /**
     * checks whether this tile is a landslide tile.
     *
     * @return true if the tile type is landslide, false otherwise
     */
    public boolean isLandslide() {
        return type == TileType.LANDSLIDE;
    }
}
