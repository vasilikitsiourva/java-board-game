package model.tiles;

/**
 * landslide tile.
 *
 * represents a landslide tile on the board.
 * this tile is treated as a special blocking region in the game.
 */
public class LandSlide extends Tile {

    /**
     * creates a landslide tile.
     *
     * postcondition: tile type is set to landslide
     */
    public LandSlide() {
        super(TileType.LANDSLIDE); // correct initialization of tile type
    }

    /**
     * sets the tile type to landslide.
     *
     * note: this is not a constructor but a regular method.
     *
     * postcondition: tile type is set to landslide
     */
    public void LandSlide() {
        this.type = TileType.LANDSLIDE;
    }

    /**
     * checks whether this tile is a landslide.
     *
     * @return true always, since this is a landslide tile
     */
    @Override
    public boolean isLandslide() {
        return true;
    }
}
