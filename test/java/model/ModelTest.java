package model;

// imports junit
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// imports game
import model.tiles.*;
import model.specialCards.Archaeologist;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * junit tests for the model layer of the amphipolis game.
 *
 * these tests verify game rules, scoring logic,
 * one player mode behavior and character card effects.
 */
public class ModelTest {

    /**
     * simple sanity test to verify that junit is working correctly.
     */
    @Test
    void sanityTest() {
        assertEquals(4, 2 + 2);
    }

    /**
     * verifies that the bag initializes correctly
     * and is not empty after initialization.
     */
    @Test
    void bagInitializesCorrectly() {

        Bag bag = new Bag();
        bag.initializeTiles();

        assertFalse(bag.BagIsEmpty());
    }

    /**
     * verifies that a tile is placed in the correct
     * board region based on its type.
     */
    @Test
    void tileIsPlacedInCorrectBoardRegion() {

        Board board = new Board();
        Tile tile = new Mosaic(Color.GREEN);

        board.placeTile(tile);

        assertEquals(1, board.getRegionSize(TileType.MOSAIC));
    }

    /**
     * verifies that moving to the next player
     * changes the current player.
     */
    @Test
    void nextPlayerChangesCurrentPlayer() {

        GameState gs = new GameState();
        gs.initializeGame();
        gs.setupPlayers(Arrays.asList("Player1", "Player2"));

        Player first = gs.getCurrentPlayer();
        gs.nextPlayer();

        assertNotEquals(first, gs.getCurrentPlayer());
    }

    /**
     * verifies that four mosaics of the same color
     * give four points.
     */
    @Test
    void fourSameColorMosaicsGiveFourPoints() {

        GameState gs = new GameState();
        Player p = new Player("P");

        for (int i = 0; i < 4; i++) {
            p.addTile(new Mosaic(Color.GREEN));
        }

        int score = gs.computeScore(p);

        assertEquals(4, score);
    }

    /**
     * verifies that three amphoras of different colors
     * give one point.
     */
    @Test
    void threeDifferentAmphorasGiveOnePoint() {

        GameState gs = new GameState();
        Player p = new Player("P");

        p.addTile(new Amphora(Color.RED));
        p.addTile(new Amphora(Color.GREEN));
        p.addTile(new Amphora(Color.YELLOW));

        int score = gs.computeScore(p);

        assertEquals(1, score);
    }

    /**
     * verifies that a complete skeleton family
     * gives six points.
     */
    @Test
    void skeletonFamilyGivesSixPoints() {

        GameState gs = new GameState();
        Player p = new Player("P");

        p.addTile(new Skeleton(true, false, TypeOfSkeleton.BIGSKELETON));
        p.addTile(new Skeleton(false, true, TypeOfSkeleton.BIGSKELETON));
        p.addTile(new Skeleton(true, false, TypeOfSkeleton.BIGSKELETON));
        p.addTile(new Skeleton(false, true, TypeOfSkeleton.BIGSKELETON));

        p.addTile(new Skeleton(true, false, TypeOfSkeleton.LITTLESKELETON));
        p.addTile(new Skeleton(false, true, TypeOfSkeleton.LITTLESKELETON));

        int score = gs.computeScore(p);

        assertEquals(6, score);
    }

    /**
     * verifies statue scoring comparison between two players.
     */
    @Test
    void statueScoringWithTwoPlayers() {

        GameState gs = new GameState();
        gs.initializeGame();

        Player p1 = new Player("P1");
        Player p2 = new Player("P2");

        p1.addTile(new Statue(TypeOfStatue.CARYATID));
        p1.addTile(new Statue(TypeOfStatue.CARYATID));

        p2.addTile(new Statue(TypeOfStatue.CARYATID));

        gs.setupPlayers(Arrays.asList("P1", "P2"));
        gs.getPlayers().set(0, p1);
        gs.getPlayers().set(1, p2);

        int score1 = gs.computeScore(p1);
        int score2 = gs.computeScore(p2);

        assertTrue(score1 >= score2);
    }

    /**
     * verifies that the thief removes all tiles
     * except landslide tiles in one player mode.
     */
    @Test
    void thiefStealsAllTilesExplicitly() {

        GameState gs = new GameState();
        gs.initializeGame();
        gs.setupPlayers(Arrays.asList("Solo"));

        Board board = gs.getBoard();

        board.placeTile(new Mosaic(Color.GREEN));
        board.placeTile(new Amphora(Color.RED));

        board.getAllTilesExceptLandslide();

        int remaining =
                board.getRegionSize(TileType.MOSAIC) +
                        board.getRegionSize(TileType.AMPHORA) +
                        board.getRegionSize(TileType.STATUE) +
                        board.getRegionSize(TileType.SKELETON);

        assertEquals(0, remaining);
    }

    /**
     * verifies that the archaeologist card
     * gives up to two tiles to the player.
     */
    @Test
    void archaeologistGivesUpToTwoTiles() {

        GameState gs = new GameState();
        gs.initializeGame();
        gs.setupPlayers(Arrays.asList("P"));

        Player p = gs.getCurrentPlayer();
        Archaeologist card = new Archaeologist(p);

        card.useCard(gs);

        assertTrue(p.getTakenTiles().size() <= 2);
    }
}
