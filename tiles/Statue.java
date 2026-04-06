package model.tiles;

/**
 * statue tile.
 *
 * represents a statue tile with a specific statue type.
 * the statue type affects scoring rules.
 */
public class Statue extends Tile {

    /** type of the statue */
    private final TypeOfStatue statueType;

    /**
     * creates a statue tile with the given type.
     *
     * @param type the statue type
     *
     * precondition: type != null
     * postcondition: statue tile is created with the given type
     * postcondition: tile type is set to statue
     */
    public Statue(TypeOfStatue type) {
        super(TileType.STATUE);
        this.statueType = type;
        this.type = TileType.STATUE;
    }

    /**
     * returns the type of the statue.
     *
     * @return statue type
     */
    public TypeOfStatue getStatueType() {
        return statueType;
    }
}
