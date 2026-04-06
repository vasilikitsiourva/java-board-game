
package view;

import controller.Controller;
import model.Player;
import model.specialCards.*;
import model.tiles.*;

import javax.sound.sampled.*;
import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * graphical user interface of the amphipolis game.
 *
 * <p>
 * this class represents the main application window.
 * it is responsible for rendering the board, player info,
 * cards, tiles and handling user interaction.
 * </p>
 *
 * <p>
 * it follows the mvc pattern and delegates all game logic
 * to the controller.
 * </p>
 */
public class GraphicUI extends JFrame {

    /** reference to the main game controller */
    private final Controller controller;

    /* window dimensions */
    private static final int FRAME_W = 950;
    private static final int FRAME_H = 678;

    private static final int BOARD_W = 650;
    private static final int BOARD_H = 520;

    private static final int RIGHT_W = 250;
    private static final int BOTTOM_H = 120;

    private static final int TILE_W = 30;
    private static final int TILE_H = 30;

    private static final int CARD_W = 85;
    private static final int CARD_H = 120;

    /* root */

    /** root container using absolute positioning */
    private JPanel root;

    /* board */

    /** panel that draws the board background image */
    private BoardBackgroundPanel boardPanel;

    /** clickable panels for each tile area */
    private JPanel mosaicArea;
    private JPanel statueArea;
    private JPanel landslideArea;
    private JPanel amphoraArea;
    private JPanel skeletonArea;

    /* right panel */

    /** panel containing player info and controls */
    private JPanel rightPanel;
    private JLabel playerLabel;
    private JLabel scoreLabel;
    private JLabel timerLabel;

    private JPanel cardsPanel;
    private JButton useCardBtn;
    private JButton drawBtn;
    private JButton endTurnBtn;

    /* bottom strip */

    /** bottom panel displaying taken and collected tiles */
    private JPanel bottomStrip;
    private JPanel takenThisTurnPanel;
    private JPanel collectedPanel;

    /**
     * list of tiles taken during the current turn.
     * cleared when the turn ends.
     */
    private final List<Tile> takenThisTurn = new ArrayList<>();

    /* music */

    /** currently playing music clip */
    private Clip musicClip;

    /** index of the player whose music is currently playing */
    private int currentMusicPlayer = -1;

    /**
     * constructs the main game window.
     * initializes ui components and connects the controller with the view.
     *
     * @param controller game controller instance
     *
     * precondition: controller != null
     * postcondition: ui is visible and ready for interaction
     */
    public GraphicUI(Controller controller) {
        this.controller = controller;
        controller.setView(this);

        setTitle("Amphipolis");
        setSize(FRAME_W, FRAME_H);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        root = new JPanel(null);
        setContentPane(root);

        /* menu bar */
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> doSave());

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.addActionListener(e -> doLoad());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        initBoard();
        initRightPanel();
        initBottomStrip();

        // initial game setup dialog
        new MenuDialog(this, controller).setVisible(true);

        refreshPlayer();
        refreshBoard();
    }

    /* board */

    /**
     * initializes the game board, background and all tile areas.
     */
    private void initBoard() {
        boardPanel = new BoardBackgroundPanel("/view/images/background.png");
        boardPanel.setBounds(0, 0, BOARD_W, BOARD_H);
        root.add(boardPanel);

        mosaicArea = createArea(new Rectangle(30, 20, 260, 180), TileType.MOSAIC);
        statueArea = createArea(new Rectangle(330, 20, 260, 180), TileType.STATUE);
        landslideArea = createArea(new Rectangle(260, 190, 200, 160), TileType.LANDSLIDE);
        amphoraArea = createArea(new Rectangle(30, 360, 260, 180), TileType.AMPHORA);
        skeletonArea = createArea(new Rectangle(330, 360, 320, 180), TileType.SKELETON);

        timerLabel = new JLabel("Time left: 30");
        timerLabel.setBounds(10, 10, 200, 25);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 14f));

        boardPanel.add(timerLabel);
        boardPanel.add(mosaicArea);
        boardPanel.add(statueArea);
        boardPanel.add(landslideArea);
        boardPanel.add(amphoraArea);
        boardPanel.add(skeletonArea);
    }

    /**
     * updates the timer label on the board.
     *
     * @param seconds remaining time in seconds
     *
     * precondition: seconds more or equal to 0
     * postcondition: timer label updated
     */
    public void updateTimer(int seconds) {
        timerLabel.setText("Time left: " + seconds);
    }

    /**
     * creates a clickable board area for a specific tile type.
     * handles tile selection through mouse interaction.
     *
     * @param bounds area bounds
     * @param type tile type represented
     *
     * precondition: bounds != null
     * precondition: type != null
     * @return configured panel for the area
     */
    private JPanel createArea(Rectangle bounds, final TileType type) {

        // flow layout keeps tiles centered inside the area
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        p.setBounds(bounds);
        p.setOpaque(false);

        // mouse listener delegates logic to controller
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // guard conditions
                if (!controller.isGameStarted()) return;
                if (!controller.hasDrawnTiles()) return;

                int available = controller.getTilesInArea(type);
                if (available <= 0) return;

                // landslide allows only one tile per turn
                int maxPerTurn = (type == TileType.LANDSLIDE) ? 1 : 2;
                int maxNow = Math.min(maxPerTurn, available);

                int take = (maxNow == 1) ? 1 : askHowManyTiles(type);
                if (take <= 0) return;

                List<Tile> taken = controller.selectTiles(type, take);
                if (taken.isEmpty()) return;

                takenThisTurn.addAll(taken);

                refreshBoard();
                refreshPlayer();
                redrawTakenThisTurn();
            }
        });

        return p;
    }

    /* right panel */

    /**
     * initializes the right panel with player info cards and actions.
     */
    private void initRightPanel() {
        rightPanel = new JPanel(null);
        rightPanel.setBounds(BOARD_W, 0, RIGHT_W, FRAME_H);
        rightPanel.setBackground(new Color(240, 240, 240));
        rightPanel.setOpaque(true);
        root.add(rightPanel);

        playerLabel = new JLabel("Player", SwingConstants.CENTER);
        playerLabel.setBounds(20, 15, 210, 30);
        playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 16f));
        rightPanel.add(playerLabel);

        JLabel lbl = new JLabel("Use Character", SwingConstants.CENTER);
        lbl.setBounds(20, 50, 210, 20);
        rightPanel.add(lbl);

        cardsPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        cardsPanel.setBounds(20, 75, 210, 380);
        cardsPanel.setOpaque(false);
        rightPanel.add(cardsPanel);

        useCardBtn = new JButton("Use Character");
        useCardBtn.setBounds(30, 470, 190, 35);
        useCardBtn.addActionListener(e -> openUseCardDialog());
        rightPanel.add(useCardBtn);

        drawBtn = new JButton("Draw Tiles");
        drawBtn.setBounds(30, 510, 190, 40);
        drawBtn.addActionListener(e -> {
            controller.drawTiles();
            refreshBoard();
            refreshPlayer();
        });
        rightPanel.add(drawBtn);

        endTurnBtn = new JButton("End Turn");
        endTurnBtn.setBounds(30, 560, 180, 42);
        endTurnBtn.addActionListener(e -> {
            takenThisTurn.clear();
            redrawTakenThisTurn();
            controller.endTurn();
            refreshPlayer();
            refreshBoard();
            refreshScore();
        });
        rightPanel.add(endTurnBtn);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setBounds(30, 610, 180, 25);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 14f));
        rightPanel.add(scoreLabel);
    }

    /**
     * refreshes the score label for the current player.
     */
    private void refreshScore() {
        if (!controller.isGameStarted()) {
            scoreLabel.setText("Score: 0");
            return;
        }
        scoreLabel.setText("Score: " + controller.getCurrentPlayerScore());
    }

    /**
     * opens a dialog allowing the player to use a character card.
     * enforces all card usage rules.
     */
    private void openUseCardDialog() {
        if (!controller.isGameStarted()) return;
        if (controller.hasUsedCard()) {
            JOptionPane.showMessageDialog(this,
                    "You have already used a character card this turn :(");
            return;
        }

        Player p = controller.getCurrentPlayer();
        if (p == null) return;

        // collect unused cards only
        List<CharacterCard> usable = new ArrayList<>();
        for (CharacterCard c : p.getCards()) {
            if (c != null && !c.isUsed()) usable.add(c);
        }

        if (usable.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available cards");
            return;
        }

        String[] options = new String[usable.size()];
        for (int i = 0; i < usable.size(); i++) {
            options[i] = usable.get(i).getName();
        }

        String choice = (String) JOptionPane.showInputDialog(
                this, "Choose a character card:",
                "Use Character",
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == null) return;

        CharacterCard chosen = null;
        for (CharacterCard c : usable) {
            if (c.getName().equals(choice)) {
                chosen = c;
                break;
            }
        }
        if (chosen == null) return;

        TileType area = null;
        if (chosen.needsAreaSelection()) {
            area = askArea();
            if (area == null) return;
        }

        if (chosen instanceof Digger &&
                controller.getTakenAreaThisTurn() == null) {
            JOptionPane.showMessageDialog(this,
                    "You must take tiles before using digger");
            return;
        }

        controller.useCharacterCard(chosen, area);
        refreshPlayer();
        refreshBoard();
    }

    /* bottom strip */

    /**
     * initializes the bottom strip that shows taken and collected tiles.
     */
    private void initBottomStrip() {
        bottomStrip = new JPanel(null);
        bottomStrip.setBounds(0, BOARD_H, BOARD_W, BOTTOM_H);
        bottomStrip.setBackground(Color.WHITE);
        root.add(bottomStrip);

        JLabel t1 = new JLabel("Taken this turn:");
        t1.setBounds(10, 5, 200, 20);
        bottomStrip.add(t1);

        takenThisTurnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        takenThisTurnPanel.setBounds(130, 0, BOARD_W - 140, 50);
        takenThisTurnPanel.setOpaque(false);
        bottomStrip.add(takenThisTurnPanel);

        JLabel t2 = new JLabel("Collected tiles:");
        t2.setBounds(10, 60, 200, 20);
        bottomStrip.add(t2);

        collectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        collectedPanel.setBounds(130, 55, BOARD_W - 140, 60);
        collectedPanel.setOpaque(false);
        bottomStrip.add(collectedPanel);
    }

    /**
     * refreshes all board regions based on the current game state.
     */
    public void refreshBoard() {
        updateRegion(TileType.MOSAIC, mosaicArea);
        updateRegion(TileType.STATUE, statueArea);
        updateRegion(TileType.LANDSLIDE, landslideArea);
        updateRegion(TileType.AMPHORA, amphoraArea);
        updateRegion(TileType.SKELETON, skeletonArea);
        applyVisualLock();
    }

    /**
     * updates a single board region by redrawing its tiles.
     */
    private void updateRegion(TileType type, JPanel panel) {
        panel.removeAll();
        for (Tile t : controller.getTilesInAreaDetailed(type)) {
            panel.add(new JLabel(loadTileImage(t)));
        }
        panel.revalidate();
        panel.repaint();
    }

    /**
     * refreshes all player related ui elements.
     */
    public void refreshPlayer() {
        Player p = controller.getCurrentPlayer();
        if (p != null) {
            int index = controller.getGameState().getPlayers().indexOf(p);
            playMusicForPlayer(index);
        }

        playerLabel.setText(p != null ? p.getName() : "Player");

        cardsPanel.removeAll();
        if (p != null) {
            for (CharacterCard c : p.getCards()) {
                JLabel lbl = new JLabel(scaleCard(c));
                lbl.setEnabled(!c.isUsed());
                cardsPanel.add(lbl);
            }
            while (cardsPanel.getComponentCount() < 6) {
                cardsPanel.add(new JLabel());
            }
        }

        redrawCollected(p);
        refreshScore();
    }

    /**
     * redraws tiles taken during the current turn.
     */
    private void redrawTakenThisTurn() {
        takenThisTurnPanel.removeAll();
        for (Tile t : takenThisTurn) {
            takenThisTurnPanel.add(new JLabel(loadTileImage(t)));
        }
        takenThisTurnPanel.revalidate();
        takenThisTurnPanel.repaint();
    }

    /**
     * redraws tiles collected by the given player.
     */
    private void redrawCollected(Player p) {
        collectedPanel.removeAll();
        if (p != null) {
            for (Tile t : p.getTakenTiles()) {
                collectedPanel.add(new JLabel(loadTileImage(t)));
            }
        }
        collectedPanel.revalidate();
        collectedPanel.repaint();
    }

    /**
     * loads and scales the correct image for a tile.
     *
     * @param tile tile to visualize
     * @return scaled image icon
     */
    private ImageIcon loadTileImage(Tile tile) {
        if (tile == null) {
            return scale("/view/images/tile_back.png", TILE_W, TILE_H);
        }

        String file = "tile_back.png";

        if (tile instanceof Mosaic) {
            Mosaic m = (Mosaic) tile;
            if (Color.GREEN.equals(m.getColor())) file = "mosaic_green.png";
            else if (Color.RED.equals(m.getColor())) file = "mosaic_red.png";
            else file = "mosaic_yellow.png";

        } else if (tile instanceof Amphora) {
            Amphora a = (Amphora) tile;
            if (Color.BLUE.equals(a.getColor())) file = "amphora_blue.png";
            else if (Color.RED.equals(a.getColor())) file = "amphora_red.png";
            else if (Color.GREEN.equals(a.getColor())) file = "amphora_green.png";
            else if (Color.YELLOW.equals(a.getColor())) file = "amphora_yellow.png";
            else if (Color.MAGENTA.equals(a.getColor())) file = "amphora_purple.png";
            else file = "amphora_brown.png";

        } else if (tile instanceof Statue) {
            Statue s = (Statue) tile;
            file = (s.getStatueType() == TypeOfStatue.CARYATID)
                    ? "caryatid.png" : "sphinx.png";

        } else if (tile instanceof Skeleton) {
            Skeleton sk = (Skeleton) tile;
            if (sk.getSkeletonType() == TypeOfSkeleton.BIGSKELETON)
                file = sk.isTopPart() ? "skeleton_big_top.png" : "skeleton_big_bottom.png";
            else
                file = sk.isTopPart() ? "skeleton_small_top.png" : "skeleton_small_bottom.png";

        } else if (tile.getType() == TileType.LANDSLIDE) {
            file = "landslide.png";
        }

        return scale("/view/images/" + file, TILE_W, TILE_H);
    }

    /**
     * loads and scales the image for a character card.
     */
    private ImageIcon scaleCard(CharacterCard c) {
        if (c == null) return new ImageIcon();

        String file;
        if (c instanceof Archaeologist) file = "archaeologist.png";
        else if (c instanceof Assistant) file = "assistant.png";
        else if (c instanceof Digger) file = "digger.png";
        else if (c instanceof Professor) file = "professor.png";
        else if (c instanceof CSDas) file = "coder.PNG";
        else return new ImageIcon();

        return scale("/view/images/" + file, CARD_W, CARD_H);
    }

    /**
     * loads an image from resources and scales it.
     */
    private ImageIcon scale(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("image not found: " + path);
            return new ImageIcon();
        }

        Image img = new ImageIcon(url)
                .getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    /**
     * background panel that paints the board image.
     */
    private static class BoardBackgroundPanel extends JPanel {
        private final Image bg;

        BoardBackgroundPanel(String path) {
            bg = new ImageIcon(getClass().getResource(path)).getImage();
            setLayout(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * asks the user how many tiles to take from an area.
     *
     * @return number of tiles selected (0 if cancelled)
     */
    private int askHowManyTiles(TileType type) {
        Object[] options = {"1", "2"};
        int res = JOptionPane.showOptionDialog(
                this,
                "How many tiles from " + type + "?",
                "Select Tiles",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (res == 0) return 1;
        if (res == 1) return 2;
        return 0;
    }

    /**
     * shows game over dialog and terminates the application.
     *
     * precondition: message != null
     */
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);

        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
        }
    }

    /**
     * shows a dialog asking the user to select a tile area.
     */
    private TileType askArea() {
        return (TileType) JOptionPane.showInputDialog(
                this,
                "Select area:",
                "Choose Area",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new TileType[]{
                        TileType.MOSAIC,
                        TileType.STATUE,
                        TileType.AMPHORA,
                        TileType.SKELETON
                },
                TileType.MOSAIC
        );
    }

    /**
     * applies visual locking to board areas depending on game state.
     */
    private void applyVisualLock() {
        TileType locked = controller.getTakenAreaThisTurn();
        boolean assistantActive = controller.isAssistantActive();

        lockArea(mosaicArea, TileType.MOSAIC, locked, assistantActive);
        lockArea(statueArea, TileType.STATUE, locked, assistantActive);
        lockArea(amphoraArea, TileType.AMPHORA, locked, assistantActive);
        lockArea(skeletonArea, TileType.SKELETON, locked, assistantActive);
        lockArea(landslideArea, TileType.LANDSLIDE, locked, assistantActive);
    }

    /**
     * locks or unlocks a specific area visually.
     */
    private void lockArea(JPanel panel,
                          TileType area,
                          TileType lockedArea,
                          boolean assistantActive) {

        if (lockedArea == null || assistantActive || area == lockedArea) {
            panel.setEnabled(true);
            panel.setOpaque(false);
            return;
        }

        panel.setEnabled(false);
        panel.setOpaque(false);
        panel.setBackground(new Color(50, 15, 60, 80));
    }

    /**
     * saves the current game state to a file.
     */
    private void doSave() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                controller.saveGame(fc.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Save failed", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * loads a previously saved game state.
     */
    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                controller.loadGame(fc.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Load failed", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * plays background music for the active player.
     *
     * @param playerIndex index of the current player
     *
     * precondition: playerIndex more o requal to 0
     */
    private void playMusicForPlayer(int playerIndex) {

        if (currentMusicPlayer == playerIndex) return;

        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }

        try {
            String path = "/view/music/Player" + (playerIndex + 1) + ".wav";
            URL url = getClass().getResource(path);

            if (url == null) {
                System.err.println("music not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();

            currentMusicPlayer = playerIndex;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
