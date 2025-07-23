package controlador;

import modelo.CategoriaJuego;
import vista.GamePanel;
import vista.MenuPrincipal;

import javax.swing.*;
import java.awt.Color;

public class GameManager {

    private JFrame ventana;
    private GamePanel gamePanel;
    private MenuPrincipal menuPrincipal;
    private GameController gameController;
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
                String categoria = menuPrincipal.getCategoriaSeleccionada();
                gameController = new GameController(this);
                if (categoria.equalsIgnoreCase("Mejorado")) {
                    gameController.setCategoria(CategoriaJuego.CLASICO_MEJORADO);
                } else {
                    gameController.setCategoria(CategoriaJuego.CLASICO);
                }
                soundManager.detenerMusicaFondo();
                iniciarJuego();
            } else if (e.getSource() == menuPrincipal.getBtnInstrucciones()) {
                String instrucciones = """
                        ¡Bienvenido a GhostDash!

                        --- CONTROLES ---
                        - Usa las Flechas del Teclado para mover a Pac-Man.
                        - Presiona 'P' durante el juego para pausar.

                        --- OBJETIVO ---
                        Come todos los puntos amarillos del laberinto para pasar de nivel.
                        ¡Evita que los fantasmas te atrapen!

                        --- MODOS DE JUEGO ---
                        - Clásico: La experiencia original de Pac-Man.
                        - Mejorado: Incluye nuevos Power-Ups y una IA de fantasmas mejorada.
                        - Multijugador: (¡Próximamente!)

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
        gamePanel = gameController.prepararJuego();
        crearVentanaConPanel(gamePanel);

        if (gameLoop != null)
            gameLoop.stop();

        int delay = 1000 / FPS;
        gameLoop = new Timer(delay, e -> {
            if (gameState == GameState.JUGANDO) {
                gameController.actualizarJuego();
                gamePanel.actualizarHUD(gameController.getPuntuacion(), gameController.getVidas());
            }
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    public void notificarMuerteDePacman() {
        if (procesandoMuerte)
            return;

        this.procesandoMuerte = true;
        gameLoop.stop();
        soundManager.reproducirEfecto("pacman_muerto.wav");
        gameController.restarVida();

        if (gameController.isGameOver()) {
            setEstado(GameState.GAME_OVER);
            gamePanel.mostrarPantallaFinal("Game Over", Color.RED);
            gamePanel.mostrarBotonReiniciar();
        } else {
            setEstado(GameState.VIDA_PERDIDA);
            gestionarPausaYReinicio();
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

    public void togglePause() {
        if (gameState == GameState.JUGANDO)
            setEstado(GameState.PAUSA);
        else if (gameState == GameState.PAUSA)
            setEstado(GameState.JUGANDO);
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