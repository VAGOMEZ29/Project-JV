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
                JOptionPane.showMessageDialog(ventana,
                        instrucciones,
                        "Instrucciones",
                        JOptionPane.INFORMATION_MESSAGE,
                        null); // El 'null' es para el ícono por defecto
            }
            // Botón SALIR
            else if (e.getSource() == menuPrincipal.getBtnSalir()) {
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
            // --- BUCLE DE JUEGO PRINCIPAL Y DEFINITIVO ---

            if (gameState == GameState.JUGANDO) {
                // 1. El controlador actualiza la lógica interna de la partida.
                gameController.actualizarJuego();

                // 2. La vista actualiza su HUD (puntos, vidas).
                gamePanel.actualizarHUD(gameController.getPuntuacion(), gameController.getVidas());

                // 3. El JEFE (GameManager) pregunta si el juego terminó.
                if (gameController.isGameOver()) {
                    gameLoop.stop(); // Detiene el juego inmediatamente.
                    setEstado(GameState.GAME_OVER);
                    gamePanel.mostrarPantallaFinal("Game Over", Color.RED);
                    gamePanel.mostrarBotonReiniciar();
                } else if (gameController.isGameWon()) {
                    gameLoop.stop();
                    setEstado(GameState.GAME_OVER); // Usamos GAME_OVER para detener el bucle
                    gamePanel.mostrarPantallaFinal("¡Has Ganado!", Color.GREEN);
                    gamePanel.mostrarBotonReiniciar();
                }
            }
            // Siempre redibujamos el panel, sin importar el estado.
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    public void togglePause() {
        if (gameState == GameState.JUGANDO)
            setEstado(GameState.PAUSA);
        else if (gameState == GameState.PAUSA)
            setEstado(GameState.JUGANDO);
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

    public void setEstado(GameState estado) {
        this.gameState = estado;
        if (gamePanel != null) {
            gamePanel.setEstadoJuego(estado); // Informamos al panel para que dibuje la pausa, etc.
        }
    }
}