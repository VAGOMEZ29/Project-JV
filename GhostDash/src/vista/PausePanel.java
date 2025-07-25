package vista;

import controlador.GameController;
import controlador.GameManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PausePanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 20, 220);
    private static final Color TEXT_COLOR = Color.YELLOW;

    public PausePanel(GameManager gameManager, GameController gameController) {
        configurePanel();
        initMenu(gameManager, gameController);
    }

    private void configurePanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());
    }

    private void initMenu(GameManager gameManager, GameController gameController) {
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(BACKGROUND_COLOR);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        menuContainer.add(createTitle());
        menuContainer.add(Box.createVerticalStrut(30));
    
        menuContainer.add(createButton("Reanudar Juego", e -> gameManager.togglePause()));
        menuContainer.add(Box.createVerticalStrut(15));

        menuContainer.add(createButton("Reiniciar Nivel", e -> {
            gameManager.togglePause();
            gameController.reiniciarNivel();
        }));
        menuContainer.add(Box.createVerticalStrut(15));

        menuContainer.add(createButton("Salir al Menú", e -> gameManager.mostrarMenu()));

        add(menuContainer);
    }

    private JLabel createTitle() {
        JLabel title = new JLabel("PAUSA");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        return title;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setFocusable(false);
        button.addActionListener(action);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}