package vista;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        this.setTitle("Pac-Man Mejorado");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(800, 600); // Un tamaño por defecto para que sea visible
        this.setLocationRelativeTo(null); // Centra la ventana
        this.setVisible(true);
    }
    // No main method here.
}