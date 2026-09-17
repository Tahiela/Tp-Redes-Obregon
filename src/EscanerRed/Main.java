package EscanerRed;

import views.InterfazEscaner;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazEscaner ventana = new InterfazEscaner();
            ventana.setVisible(true);
        });
    }
}
