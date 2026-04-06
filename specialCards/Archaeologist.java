package model.specialCards;

import model.GameState;
import model.Player;
import model.tiles.Tile;
import model.tiles.TileType;

/**
 * archaeologist character card.
 *
 * allows the current player to take up to two tiles from one valid region,
 * excluding landslide and the region already used in the current turn.
 */
public class Archaeologist extends CharacterCard {

    /**
     * creates an archaeologist card for the given player.
     *
     * @param player the owner of the card
     *
     * precondition: player != null
     * postcondition: the card is associated with the given player
     */
    public Archaeologist(Player player) {
        super(player);
        this.name = "Archaeologist";
    }

    /**
     * applies the effect of the archaeologist card.
     *
     * the card searches all tile regions in order and takes up to two tiles
     * from the first valid region found.
     * excluded regions:
     * - landslide
     * - the region already taken this turn
     * @param gameState the current game state
     * precondition: gameState != null
     * precondition: card has not already been used
     * postcondition: the current player may receive up to two tiles
     * postcondition: tiles are taken from only one region
     * postcondition: the card is marked as used
     */
    @Override
    public void useCard(GameState gameState) {

        // card can only be used once
        if (used) return;

        // region already used in this turn
        TileType excluded = gameState.getTakenAreaThisTurn();

        // iterate through all tile types
        for (TileType type : TileType.values()) {

            // skip landslide region
            if (type == TileType.LANDSLIDE) continue;

            // skip the already used region
            if (type == excluded) continue;

            int available = gameState.getBoard().getRegionSize(type);

            // take up to two tiles from the first valid region
            if (available > 0) {
                int count = Math.min(2, available);

                gameState.getCurrentPlayer()
                        .addTiles(gameState.getBoard().removeTiles(type, count));

                // tiles are taken from only one region
                break;
            }
        }

        // mark the card as used
        used = true;
    }
}
