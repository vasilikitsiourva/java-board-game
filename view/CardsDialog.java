package view;

import controller.Controller;
import model.specialCards.CharacterCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CardsDialog extends JDialog {

    public CardsDialog(JFrame parent, Controller controller, List<CharacterCard> cards) {
        super(parent, "Choose Character", true);
        setSize(650, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Select a character card (once per game)", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, Math.max(1, cards.size()), 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (CharacterCard card : cards) {
            JButton b = new JButton(card.getClass().getSimpleName());
            b.addActionListener(e -> {
                controller.useCharacterCard(card, null);
                dispose();
            });
            grid.add(b);
        }

        add(grid, BorderLayout.CENTER);
    }
}
