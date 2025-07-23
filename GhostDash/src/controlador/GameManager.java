package controlador;

import modelo.CategoriaJuego;
import vista.GamePanel;
import vista.MenuPrincipal;
import vista.PausePanel;

import javax.swing.*;
import java.awt.Color;

public class GameManager {

    private JFrame ventana;
    private GamePanel gamePanel;
    private MenuPrincipal menuPrincipal;
    private GameController gameController;
    private PausePanel pausePanel;
    private final SoundManager soundManager = new SoundManager();
    private Timer gameLoop;
    private GameState gameState;
    private final int FPS = 60;
    private boolean procesandoMuerte = false;

    public GameManager() {
        mostrarMenu();
    }

    public void mostrarMenu() {
        setEstado(GameState.MENU_PRINCIPAL);
        if (gameLoop != null)
            gameLoop.stop();
        soundManager.reproducirMusicaFondo("pacman_inicio.wav");

        menuPrincipal = new MenuPrincipal(e -> {
            if (e.getSource() == menuPrincipal.getBtnJugar()) {
                String categoriaStr = menuPrincipal.getCategoriaSeleccionada();
                gameController = new GameController(this);
                if (categoriaStr.equalsIgnoreCase("Mejorado")) {
                    gameController.setCategoria(CategoriaJuego.CLASICO_MEJORADO);
                } else if (categoriaStr.equalsIgnoreCase("Multijugador")) {
                    gameController.setCategoria(CategoriaJuego.MULTIJUGADOR);
                } else {
                    gameController.setCategoria(CategoriaJuego.CLASICO);
                }
                soundManager.detenerMusicaFondo();
                iniciarJuego();
            } else if (e.getSource() == menuPrincipal.getBtnInstrucciones()) {
                String instrucciones = """
                        ¡Bienvenido a GhostDash!
                        --- CONTROLES ---
                        - Pac-Man (J1): Flechas del Teclado
                        - Fantasma (J2): W-A-S-D para mover, ESPACIO para acelerar.
                        - Pausa: P
                        --- OBJETIVO ---
                        - Pac-Man: Come todos los puntos para ganar.
                        - Fantasma: Atrapa a Pac-Man 3 veces para ganar.
                        ¡Buena suerte!
                        """;
                JOptionPane.showMessageDialog(ventana, instrucciones, "Instrucciones", JOptionPane.INFORMATION_MESSAGE,
                        null);
            } else if (e.getSource() == menuPrincipal.getBtnSalir()) {
                System.exit(0);
            }
        });
        crearVentanaConPanel(menuPrincipal);
    }

    public void iniciarJuego() {
        setEstado(GameState.JUGANDO);
        gameController = new GameController(this);
        gamePanel = gameController.prepararJuego();
        pausePanel = new PausePanel(this, gameController);
        pausePanel.setVisible(false);

        crearVentanaDeJuegoConCapas();

        if (gameLoop != null)
            gameLoop.stop();

        int delay = 1000 / FPS;
        gameLoop = new Timer(delay, e -> {
            if (gameState == GameState.JUGANDO) {
                gameController.actualizarJuego();
                gamePanel.actualizarHUD(gameController.getPuntuacion(), gameController.getVidas(),
                        gameController.getCategoria());
                if (gameController.isGameWon())
                    gestionarVictoria();
                else if (gameController.isGameOver())
                    gestionarDerrota();
            }
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    /**
     * Alterna el estado del juego entre JUGANDO y PAUSA.
     * Ahora también controla la visibilidad del panel de pausa y gestiona el foco
     * del teclado.
     */
    public void togglePause() {
        if (gameState == GameState.JUGANDO) {
            setEstado(GameState.PAUSA);
            pausePanel.setVisible(true);
        } else if (gameState == GameState.PAUSA) {
            setEstado(GameState.JUGANDO);
            pausePanel.setVisible(false);

            // ¡LA LÍNEA QUE LO ARREGLA TODO!
            // Forzamos al GamePanel a recuperar el foco para que pueda escuchar el teclado
            // de nuevo.
            gamePanel.requestFocusInWindow();
        }
    }

    private void crearVentanaDeJuegoConCapas() {
        if (ventana != null)
            ventana.dispose();
        ventana = new JFrame("GhostDash");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(gamePanel.getPreferredSize());

        gamePanel.setBounds(0, 0, gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

        pausePanel.setBounds(0, 0, gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height);
        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);

        ventana.add(layeredPane);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    public void notificarMuerteDePacman() {
        if (procesandoMuerte)
            return;
        this.procesandoMuerte = true;
        gameLoop.stop();
        soundManager.reproducirEfecto("pacman_muerto.wav");
        gameController.restarVida();
        if (!gameController.isGameOver()) {
            setEstado(GameState.VIDA_PERDIDA);
            gestionarPausaYReinicio();
        } else {
            gameLoop.start();
        }
    }

    private void gestionarPausaYReinicio() {
        Timer pausaTimer = new Timer(2000, e -> {
            gameController.reiniciarPosiciones();
            setEstado(GameState.JUGANDO);
            this.procesandoMuerte = false;
            gameLoop.start();
        });
        pausaTimer.setRepeats(false);
        pausaTimer.start();
    }

    private void gestionarVictoria() {
        gameLoop.stop();
        setEstado(GameState.GAME_OVER);
        if (gameController.getCategoria() == CategoriaJuego.MULTIJUGADOR) {
            gamePanel.mostrarPantallaFinal("¡Pac-Man Gana!", Color.GREEN);
        } else {
            gamePanel.mostrarPantallaFinal("¡Has Ganado!", Color.GREEN);
        }
        gamePanel.mostrarBotonReiniciar();
    }

    private void gestionarDerrota() {
        gameLoop.stop();
        setEstado(GameState.GAME_OVER);
        if (gameController.getCategoria() == CategoriaJuego.MULTIJUGADOR) {
            gamePanel.mostrarPantallaFinal("¡Fantasma Gana!", Color.RED);
        } else {
            gamePanel.mostrarPantallaFinal("Game Over", Color.RED);
        }
        gamePanel.mostrarBotonReiniciar();
    }

    public void setEstado(GameState estado) {
        this.gameState = estado;
        if (gamePanel != null)
            gamePanel.setEstadoJuego(estado);
    }

    private void crearVentanaConPanel(JPanel panel) {
        if (ventana != null)
            ventana.dispose();
        ventana = new JFrame("GhostDash");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.add(panel);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        panel.requestFocusInWindow();
    }
}