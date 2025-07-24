package vista;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameFrame extends JFrame {
    public GameFrame() {
        configureWindow();
    }

    private void configureWindow() {
        setTitle("GhostDash veaguirreg-aeraso"); // Nombre consistente con el proyecto
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    public void showPanel(JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
        setVisible(true);
    }
}