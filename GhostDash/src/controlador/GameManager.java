package controlador;

import vista.GamePanel;
import vista.MenuPrincipal;
import modelo.*;

import javax.swing.*;

/**
 * GameManager es la clase principal que controla el flujo general de la aplicación.
 * Gestiona los estados del juego (menú, jugando), la ventana,
 * y el hilo principal del juego (Game Loop).
 */
public class GameManager implements Runnable {

    private JFrame ventana;
    private GamePanel gamePanel;
    private MenuPrincipal menuPrincipal;
    private GameController gameController;
    private SoundManager soundManager = new SoundManager();

    private Thread gameThread;
    private volatile GameState gameState; // 'volatile' asegura visibilidad entre hilos

    private final int FPS = 60; // Fotogramas por segundo

    /**
     * Constructor: al iniciar la aplicación, se muestra el menú principal.
     */
    public GameManager() {
        mostrarMenu(); // En vez de iniciar el juego directamente
    }

    /**
     * Muestra el menú principal y prepara los botones de acción.
     */
    public void mostrarMenu() {
        gameState = GameState.MENU_PRINCIPAL;

        // Asegúrate de que esté inicializado antes
        if (soundManager == null) {
            soundManager = new SoundManager();
        }

        // Inicia música de fondo del menú
        soundManager.reproducirMusicaFondo("pacman_inicio.wav");

        menuPrincipal = new MenuPrincipal(e -> {
            if (e.getSource() == menuPrincipal.getBtnJugar()) {
                String categoria = menuPrincipal.getCategoriaSeleccionada();
                System.out.println("Categoría seleccionada: " + categoria);

                gameController = new GameController();

                if (categoria.equalsIgnoreCase("Mejorado")) {
                    gameController.setCategoria(CategoriaJuego.CLASICO_MEJORADO);
                } else {
                    gameController.setCategoria(CategoriaJuego.CLASICO);
                }

                // Detiene música del menú al iniciar el juego
                soundManager.detenerMusicaFondo();
                iniciarJuego();

            } else if (e.getSource() == menuPrincipal.getBtnInstrucciones()) {
                JOptionPane.showMessageDialog(ventana,
                        "Controles:\n← ↑ → ↓ para mover a Pac-Man.\nCome todos los puntos para ganar.\nEvita los fantasmas (a menos que tengas un Power-Up).\n\nModos:\nClásico: experiencia original.\nMejorado: incluye mejoras gráficas e IA.\nMultijugador: (próximamente)",
                        "Instrucciones", JOptionPane.INFORMATION_MESSAGE);

            } else if (e.getSource() == menuPrincipal.getBtnSalir()) {
                System.exit(0);
            }
        });

        crearVentanaConPanel(menuPrincipal);
    }

    /**
     * Prepara e inicia una nueva sesión de juego.
     */
    public void iniciarJuego() {
        System.out.println("Cambiando a estado: JUGANDO");
        gameState = GameState.JUGANDO;

        gamePanel = gameController.prepararJuego();

        crearVentanaConPanel(gamePanel);

        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Crea una ventana nueva (JFrame) y añade el panel recibido.
     * Reutilizable tanto para menú como para juego.
     *
     * @param panel El panel que se desea mostrar (menú o juego).
     */
    private void crearVentanaConPanel(JPanel panel) {
        if (ventana != null) {
            ventana.dispose(); // Cerramos la ventana anterior si existe
        }

        ventana = new JFrame("GhostDash");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.add(panel);
        ventana.pack(); // Ajusta el tamaño al contenido del panel
        ventana.setLocationRelativeTo(null); // Centra la ventana en la pantalla
        ventana.setVisible(true);
        panel.requestFocusInWindow(); // Necesario para capturar teclas
    }

    /**
     * Este es el corazón del juego. El bucle se ejecuta continuamente
     * mientras el hilo del juego esté activo.
     */
    @Override
    public void run() {
        // Intervalo de tiempo para dibujar a 60 FPS
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                if (gameState == GameState.JUGANDO) {
                    // 1. Lógica del juego
                    gameController.actualizarJuego();
                    // 2. Redibujar todo en pantalla
                    gamePanel.repaint();
                }
                delta--;
            }
        }
    }
}
