import controller.Controller;
import view.GraphicUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            GraphicUI ui = new GraphicUI(controller);
            ui.setVisible(true);
        });
    }

}
