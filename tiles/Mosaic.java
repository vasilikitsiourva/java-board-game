package model.tiles;

import java.awt.Color;

/**
 * mosaic tile.
 *
 * represents a mosaic tile with a specific color.
 * the color is used for matching and scoring.
 */
public class Mosaic extends Tile {

    /** color of the mosaic tile */
    private final Color color;

    /**
     * creates a mosaic tile with the given color.
     *
     * @param color the color of the mosaic
     *
     * precondition: color != null
     * postcondition: mosaic tile is created with the given color
     * postcondition: tile type is set to mosaic
     *
     * @throws IllegalArgumentException if color is null
     */
    public Mosaic(Color color) {
        super(TileType.MOSAIC);

        // color must not be null
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }

        this.color = color;
    }

    /**
     * returns the color of the mosaic tile.
     *
     * @return mosaic color
     */
    public Color getColor() {
        return color;
    }
}
