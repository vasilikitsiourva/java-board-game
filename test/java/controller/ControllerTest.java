package controller;

import model.Player;
import model.tiles.Tile;
import model.tiles.TileType;
import model.specialCards.Archaeologist;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * junit tests for controller logic.
 * these tests focus on game rules and state changes,
 * without involving ui or timer functionality.
 */
public class ControllerTest {



    /**
     * verifies that starting a new game correctly initializes
     * the game state and current player.
     */
    @Test
    void startNewGameInitializesGame() {

        Controller controller = new Controller();

        // start game with 2 players
        controller.startNewGame(2);

        // game should be marked as started
        assertTrue(controller.isGameStarted());

        // current player should not be null
        assertNotNull(controller.getCurrentPlayer());
    }


    /**
     * verifies that drawing tiles marks the tiles as drawn
     * for the current turn.
     */
    @Test
    void drawTilesMarksTilesDrawn() {

        Controller controller = new Controller();
        controller.startNewGame(2);

        // draw tiles for the current player
        controller.drawTiles();

        // controller should record that tiles have been drawn
        assertTrue(controller.hasDrawnTiles());
    }


    /**
     * verifies that selecting tiles returns exactly one tile
     * when one tile is requested and available.
     */
    @Test
    void selectTilesReturnsExactlyOneWhenAvailable() {

        Controller controller = new Controller();
        controller.startNewGame(2);

        // manually place a mosaic tile on the board
        controller.getGameState()
                .getBoard()
                .placeTile(new model.tiles.Mosaic(java.awt.Color.GREEN));

        // mark tiles as drawn to allow selection
        controller.drawTiles();

        // select one mosaic tile
        List<Tile> taken = controller.selectTiles(TileType.MOSAIC, 1);

        // exactly one tile should be returned
        assertEquals(1, taken.size());
    }



    /**
     * verifies that ending a turn changes the current player.
     */
    @Test
    void endTurnChangesCurrentPlayer() {

        Controller controller = new Controller();
        controller.startNewGame(2);

        // store the first current player
        Player first = controller.getCurrentPlayer();

        // end the turn
        controller.endTurn();

        // get the new current player
        Player second = controller.getCurrentPlayer();

        // current player should be different
        assertNotEquals(first, second);
    }



    /**
     * verifies that using a character card
     * marks the card as used.
     */
    @Test
    void useCharacterCardMarksCardAsUsed() {

        Controller controller = new Controller();
        controller.startNewGame(1);

        Player p = controller.getCurrentPlayer();

        // find the archaeologist card
        Archaeologist card = null;
        for (model.specialCards.CharacterCard c : p.getCards()) {
            if (c instanceof Archaeologist) {
                card = (Archaeologist) c;
                break;
            }
        }

        // archaeologist card should exist
        assertNotNull(card);

        // use the card
        controller.useCharacterCard(card, null);

        // card should now be marked as used
        assertTrue(card.isUsed());
    }

    /**
     * verifies that only one character card
     * can be used per turn.
     */
    @Test
    void cannotUseTwoCardsInSameTurn() {

        Controller controller = new Controller();
        controller.startNewGame(1);

        Player p = controller.getCurrentPlayer();

        // get the first two character cards
        model.specialCards.CharacterCard card1 = p.getCards().get(0);
        model.specialCards.CharacterCard card2 = p.getCards().get(1);

        // use the first card
        controller.useCharacterCard(card1, null);

        // attempt to use a second card in the same turn
        controller.useCharacterCard(card2, null);

        // second card should not be marked as used
        assertFalse(card2.isUsed());
    }
}
