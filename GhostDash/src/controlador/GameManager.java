package controlador;

import javax.swing.JFrame;
import vista.GamePanel;

/**
 * GameManager es la clase principal que controla el flujo general de la
 * aplicación.
 * Gestiona los estados del juego (menú, jugando, pausa), la ventana
 * y el hilo principal del juego (Game Loop).
 */
public class GameManager implements Runnable {

    private JFrame ventana;
    private GamePanel gamePanel;
    private GameController gameController;

    private Thread gameThread;
    private volatile GameState gameState; // 'volatile' asegura visibilidad entre hilos

    private final int FPS = 60; // Nuestro objetivo de fotogramas por segundo

    public GameManager() {
        // Por ahora, al iniciar la aplicación, saltamos directamente a jugar.
        // Más adelante, aquí cambiaremos para que el estado inicial sea MENU_PRINCIPAL
        // y se muestre una pantalla de menú.
        iniciarJuego();
    }

    /**
     * Prepara e inicia una nueva sesión de juego.
     */
    public void iniciarJuego() {
        System.out.println("Cambiando a estado: JUGANDO");
        gameState = GameState.JUGANDO;

        // 1. Creamos un controlador para la partida.
        gameController = new GameController();

        // 2. El controlador prepara todos los objetos del juego y nos devuelve el panel
        // gráfico.
        gamePanel = gameController.prepararJuego();

        // 3. Creamos la ventana principal de la aplicación.
        crearVentana();

        // 4. Iniciamos el hilo (thread) que contendrá el bucle principal del juego.
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Configura y muestra la ventana (JFrame) del juego.
     */
    private void crearVentana() {
        ventana = new JFrame("GhostDash");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);

        ventana.add(gamePanel); // Añadimos el panel del juego a la ventana.

        ventana.pack(); // Ajusta el tamaño de la ventana al del panel.
        ventana.setLocationRelativeTo(null); // Centra la ventana en la pantalla.
        ventana.setVisible(true);

        // Es crucial dar el foco al panel para que escuche las teclas.
        gamePanel.requestFocusInWindow();
    }

    /**
     * Este es el corazón del juego. El bucle se ejecuta continuamente
     * mientras el hilo del juego esté activo.
     */
    @Override
    public void run() {
        // Calculamos el intervalo de tiempo para cada fotograma en nanosegundos.
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            // Este sistema asegura que el juego se actualice a una velocidad constante (60
            // veces por segundo)
            if (delta >= 1) {
                if (gameState == GameState.JUGANDO) {
                    // 1. Actualiza toda la lógica (movimiento, IA, etc.)
                    gameController.actualizarJuego();
                    // 2. Vuelve a dibujar la pantalla con los cambios.
                    gamePanel.repaint();
                }
                delta--;
            }
        }
    }
}