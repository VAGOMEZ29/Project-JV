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

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void mostrarMenu() {
        setEstado(GameState.MENU_PRINCIPAL);
        if (gameLoop != null)
            gameLoop.stop();
        soundManager.reproducirMusicaFondo("pacman_inicio.wav");

        menuPrincipal = new MenuPrincipal(e -> {
            if (e.getSource() == menuPrincipal.getBtnJugar()) {
                String categoriaStr = menuPrincipal.getCategoriaSeleccionada();
                // Se crea un nuevo GameController para cada partida, asegurando un estado
                // limpio.
                gameController = new GameController(this);
                if (categoriaStr.equalsIgnoreCase("Mejorado")) {
                    gameController.setCategoria(CategoriaJuego.CLASICO_MEJORADO);
                } else if (categoriaStr.equalsIgnoreCase("Multijugador")) {
                    gameController.setCategoria(CategoriaJuego.MULTIJUGADOR);
                }else {
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
            else if (e.getSource() == menuPrincipal.getBtnCargarPartida()) {
                    System.out.println("nada?");
                    mostrarCargarPartida();
            }
        });
        crearVentanaConPanel(menuPrincipal);
    }

    public void iniciarJuego() {
        // ¡LA LÍNEA QUE LO ARREGLA TODO!
        // Reseteamos el bloqueo de muerte al inicio de CADA partida.
        this.procesandoMuerte = false;

        setEstado(GameState.JUGANDO);
        gamePanel = gameController.prepararJuego();
        pausePanel = new PausePanel(this, gameController);
        pausePanel.setVisible(false);

        gamePanel.actualizarHUD(
            gameController.getPuntuacion(),
            gameController.getVidas(),
            gameController.getCategoria()
        );
        
        crearVentanaDeJuegoConCapas();

        if (gameLoop != null)
            gameLoop.stop();

        int delay = 1000 / FPS;
        gameLoop = new Timer(delay, e -> {
            if (gameState == GameState.JUGANDO) {
                gameController.actualizarJuego();
                gamePanel.actualizarHUD(gameController.getPuntuacion(), gameController.getVidas(),
                        gameController.getCategoria());

                // Las condiciones de victoria/derrota se comprueban después de notificar la
                // muerte.
                if (gameController.isGameWon()) {
                    gestionarVictoria();
                } else if (gameController.isGameOver()) {
                    gestionarDerrota();
                }
            }
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    public void togglePause() {
        if (gameState == GameState.JUGANDO) {
            setEstado(GameState.PAUSA);
            pausePanel.setVisible(true);
        } else if (gameState == GameState.PAUSA) {
            setEstado(GameState.JUGANDO);
            pausePanel.setVisible(false);
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

        // Ahora, la comprobación de Game Over se hace en el bucle principal.
        // Si no es game over, simplemente iniciamos la pausa.
        if (!gameController.isGameOver()) {
            setEstado(GameState.VIDA_PERDIDA);
            gestionarPausaYReinicio();
        } else {
            // Si es game over, reanudamos el bucle brevemente para que
            // la lógica en iniciarJuego() lo detecte y muestre la pantalla final.
            gameLoop.start();
        }
    }

    private void gestionarPausaYReinicio() {
        Timer pausaTimer = new Timer(2000, e -> {
            gameController.reiniciarPosiciones();
            setEstado(GameState.JUGANDO);
            this.procesandoMuerte = false; // Se desbloquea aquí
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

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void mostrarCargarPartida(){
        JPanel cargarPartidaPanel = new vista.CargarPartidaPanel(this);
        crearVentanaConPanel(cargarPartidaPanel);
    }

}