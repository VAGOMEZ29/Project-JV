// Archivo: PausePanel.java
// Ubicación: paquete vista
package vista;

import controlador.GameController;
import controlador.GameManager;
import javax.swing.*;
import java.awt.*;

public class PausePanel extends JPanel {

    private final GameManager gameManager;
    private final GameController gameController;

    public PausePanel(GameManager gameManager, GameController gameController) {
        this.gameManager = gameManager;
        this.gameController = gameController;

        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(new Color(20, 20, 20, 220));
        menuContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // --- Título ---
        JLabel pauseTitle = new JLabel("PAUSA");
        pauseTitle.setFont(new Font("Arial", Font.BOLD, 36));
        pauseTitle.setForeground(Color.YELLOW);
        pauseTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuContainer.add(pauseTitle);
        menuContainer.add(Box.createVerticalStrut(30)); // Espacio

        // --- Botones de Acción ---
        JButton btnReanudar = createMenuButton("Reanudar");
        btnReanudar.addActionListener(e -> gameManager.togglePause());
        menuContainer.add(btnReanudar);
        menuContainer.add(Box.createVerticalStrut(15));

        JButton btnReiniciar = createMenuButton("Reiniciar Nivel");
        btnReiniciar.addActionListener(e -> {
            gameManager.togglePause();
            gameController.reiniciarNivel();
        });
        menuContainer.add(btnReiniciar);
        menuContainer.add(Box.createVerticalStrut(15));

        JButton btnSalir = createMenuButton("Salir al Menú");
        btnSalir.addActionListener(e -> gameManager.mostrarMenu());
        menuContainer.add(btnSalir);

        add(menuContainer);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setFocusable(false);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}