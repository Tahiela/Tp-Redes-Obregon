package EscanerRed;

import javax.swing.SwingUtilities;

import vista.InterfazEscaner;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazEscaner ventana = new InterfazEscaner();
            ventana.setVisible(true);
        });
    }
}
																																																																																																																																																																																				