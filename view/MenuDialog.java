package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;

/**
 * menu dialog for starting a new game.
 *
 * allows the user to select the number of players
 * before the game begins.
 */
public class MenuDialog extends JDialog {

    /**
     * creates and displays the start game dialog.
     *
     * the dialog allows the user to choose between
     * one player or four players.
     *
     * @param controller the game controller
     *
     * precondition: parent != null
     * precondition: controller != null
     * postcondition: dialog is displayed centered on the parent frame
     * postcondition: a new game is started when a button is pressed
     */
    public MenuDialog(JFrame parent, Controller controller) {
        super(parent, "Start Game", true);

        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(3, 1, 10, 10));

        // instruction label
        JLabel label = new JLabel("Select number of players", SwingConstants.CENTER);
        add(label);

        // buttons for selecting number of players
        JButton onePlayer = new JButton("1 Player");
        JButton fourPlayers = new JButton("4 Players");

        // start a single player game
        onePlayer.addActionListener(e -> {
            controller.startNewGame(1);
            dispose();
        });

        // start a four player game
        fourPlayers.addActionListener(e -> {
            controller.startNewGame(4);
            dispose();
        });

        add(onePlayer);
        add(fourPlayers);
    }
}
