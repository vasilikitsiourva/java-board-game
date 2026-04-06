package model.tiles;

/**
 * skeleton tile.
 *
 * represents a skeleton tile, which may consist of a top part,
 * a bottom part, and a specific skeleton type.
 */
public class Skeleton extends Tile {

    /** indicates whether this tile is the top part of a skeleton */
    private final boolean topPart;

    /** indicates whether this tile is the bottom part of a skeleton */
    private final boolean bottomPart;

    /** type of the skeleton (for example big or small) */
    private final TypeOfSkeleton skeletonType;

    /**
     * creates a skeleton tile with the given properties.
     *
     * @param topPart true if this tile represents the top part
     * @param bottomPart true if this tile represents the bottom part
     * @param skeletonType the type of skeleton
     *
     * precondition: skeletonType != null
     * postcondition: skeleton tile is created with the given properties
     * postcondition: tile type is set to skeleton
     */
    public Skeleton(boolean topPart, boolean bottomPart, TypeOfSkeleton skeletonType) {
        super(TileType.SKELETON);
        this.topPart = topPart;
        this.bottomPart = bottomPart;
        this.skeletonType = skeletonType;
    }

    /**
     * checks whether this tile is the top part of a skeleton.
     *
     * @return true if this is the top part, false otherwise
     */
    public boolean isTopPart() {
        return topPart;
    }

    /**
     * checks whether this tile is the bottom part of a skeleton.
     *
     * @return true if this is the bottom part, false otherwise
     */
    public boolean isBottomPart() {
        return bottomPart;
    }

    /**
     * returns the type of the skeleton.
     *
     * @return skeleton type
     */
    public TypeOfSkeleton getSkeletonType() {
        return skeletonType;
    }
}
