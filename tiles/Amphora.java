package model.tiles;

import java.awt.Color;

/**
 * amphora tile.
 *
 * represents an amphora tile with a specific color.
 * the color is used for scoring and grouping logic.
 */
public class Amphora extends Tile {

    /** color of the amphora */
    private final Color color;

    /**
     * creates an amphora tile with the given color.
     *
     * @param color the color of the amphora
     *
     * precondition: color != null
     * postcondition: amphora tile is created with the given color
     * postcondition: tile type is set to amphora
     */
    public Amphora(Color color) {
        super(TileType.AMPHORA);
        this.color = color;
        this.type = TileType.AMPHORA;
    }

    /**
     * returns the color of the amphora.
     *
     * @return amphora color
     */
    public Color getColor() {
        return color;
    }
}
