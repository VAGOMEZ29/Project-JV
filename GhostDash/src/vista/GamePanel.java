package vista;

import controlador.GameManager;
import controlador.GameState;
import modelo.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

public class GamePanel extends JPanel {

    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas = Collections.emptyList();
    private List<Fruta> frutas = Collections.emptyList();
    private List<Punto> puntos = Collections.emptyList();
    private List<PowerUp> powerUps = Collections.emptyList();
    private int puntuacion;
    private int vidas;
    private JButton botonReiniciar;
    private Runnable reiniciarListener;
    private GameManager gameManager;
    private boolean mostrarPantallaFinal = false;
    private String mensajeFinal = "";
    private Color colorMensajeFinal = Color.WHITE;
    private GameState estadoJuego = GameState.JUGANDO;

    private CategoriaJuego categoria; // Campo para saber en qué modo estamos
    private FantasmaJugador fantasmaJugador;

    public GamePanel(Laberinto laberinto, PacMan pacman, List<Fantasma> fantasmas,
            List<Fruta> frutas, List<Punto> puntos, List<PowerUp> powerUps, CategoriaJuego categoria) { // <-- Parámetro
                                                                                                        // añadido
        this.laberinto = laberinto;
        this.pacman = pacman;
        this.fantasmas = fantasmas;
        this.frutas = frutas;
        this.puntos = puntos;
        this.powerUps = powerUps;
        this.categoria = categoria; // <-- Asignación
        setPreferredSize(new Dimension(laberinto.getColumnas() * 32, laberinto.getFilas() * 32 + 40));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        crearBotonReiniciar();
    }

    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }

    private void crearBotonReiniciar() {
        int panelWidth = laberinto.getColumnas() * 32;
        int panelHeight = laberinto.getFilas() * 32 + 40;
        botonReiniciar = new JButton("Volver al Menú");
        botonReiniciar.setBounds((panelWidth - 200) / 2, (panelHeight - 40) / 2 + 40, 200, 40);
        botonReiniciar.setVisible(false);
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 18));
        botonReiniciar.setBackground(new Color(30, 30, 30));
        botonReiniciar.setForeground(Color.YELLOW);
        botonReiniciar.setFocusPainted(false);
        botonReiniciar.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        botonReiniciar.addActionListener(e -> {
            if (reiniciarListener != null) {
                mostrarPantallaFinal = false;
                botonReiniciar.setVisible(false);
                reiniciarListener.run();
            }
        });
        add(botonReiniciar);
    }

    public void setReiniciarListener(Runnable listener) {
        this.reiniciarListener = listener;
    }

    public void inicializarControles(PacMan pacman, FantasmaJugador fantasmaJugador) {
        this.pacman = pacman;
        this.fantasmaJugador = fantasmaJugador;
        for (var kl : getKeyListeners())
            removeKeyListener(kl);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    if (gameManager != null)
                        gameManager.togglePause();
                    return;
                }
                // Controles de PAC-MAN (Flechas)
                if (pacman != null) {
                    Direccion dirPacman = switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP -> Direccion.ARRIBA;
                        case KeyEvent.VK_DOWN -> Direccion.ABAJO;
                        case KeyEvent.VK_LEFT -> Direccion.IZQUIERDA;
                        case KeyEvent.VK_RIGHT -> Direccion.DERECHA;
                        default -> null;
                    };
                    if (dirPacman != null)
                        pacman.setDireccionDeseada(dirPacman);
                }
                // Controles del FANTASMA (W-A-S-D y Espacio)
                if (fantasmaJugador != null) {
                    Direccion dirFantasma = switch (e.getKeyCode()) {
                        case KeyEvent.VK_W -> Direccion.ARRIBA;
                        case KeyEvent.VK_S -> Direccion.ABAJO;
                        case KeyEvent.VK_A -> Direccion.IZQUIERDA;
                        case KeyEvent.VK_D -> Direccion.DERECHA;
                        default -> null;
                    };
                    if (dirFantasma != null)
                        fantasmaJugador.setDireccionDeseada(dirFantasma);
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        fantasmaJugador.activarAceleron(1.5, 2000);
                    }
                }
            }
        });
        requestFocusInWindow();
    }

    public void actualizarHUD(int puntuacion, int vidas, CategoriaJuego categoria) { // <-- Parámetro añadido
        this.puntuacion = puntuacion;
        this.vidas = vidas;
        this.categoria = categoria;
    }

    public void mostrarPantallaFinal(String mensaje, Color color) {
        this.mostrarPantallaFinal = true;
        this.mensajeFinal = mensaje;
        this.colorMensajeFinal = color;
        repaint();
    }

    public void mostrarBotonReiniciar() {
        botonReiniciar.setVisible(true);
        requestFocusInWindow();
    }

    public void setEstadoJuego(GameState estado) {
        this.estadoJuego = estado;
        if (estado == GameState.JUGANDO) {
            this.mostrarPantallaFinal = false;
        }
    }

    // Setters para actualizar las referencias a los objetos del juego.
    public void setPacMan(PacMan p) {
        this.pacman = p;
    }

    public void setFantasmas(List<Fantasma> f) {
        this.fantasmas = f;
    }

    public void setFrutas(List<Fruta> f) {
        this.frutas = f;
    }

    public void setPowerUps(List<PowerUp> p) {
        this.powerUps = p;
    }

    public void setPuntos(List<Punto> p) {
        this.puntos = p;
    }

    public void setLaberinto(Laberinto l) {
        this.laberinto = l;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dibujo del laberinto
        if (laberinto != null) {
            for (int fila = 0; fila < laberinto.getFilas(); fila++) {
                for (int col = 0; col < laberinto.getColumnas(); col++) {
                    if (laberinto.getDiseno()[fila][col] == 'X') {
                        g.setColor(new Color(25, 25, 166));
                        g.fillRect(col * 32, fila * 32, 32, 32);
                    }
                }
            }
        }

        // Dibujo de los elementos del juego
        if (puntos != null)
            for (Punto punto : List.copyOf(puntos))
                punto.dibujar(g);
        if (frutas != null)
            for (Fruta fruta : List.copyOf(frutas))
                fruta.dibujar(g);
        if (powerUps != null)
            for (PowerUp powerUp : List.copyOf(powerUps))
                powerUp.dibujar(g);
        if (pacman != null)
            pacman.dibujar(g);
        if (fantasmas != null)
            for (Fantasma fantasma : List.copyOf(fantasmas))
                fantasma.dibujar(g);

        // --- DIBUJO DEL HUD (CON LÓGICA ADAPTATIVA) ---
        if (laberinto != null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, laberinto.getFilas() * 32, getWidth(), 40);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 18));

            // Si es multijugador, el HUD muestra la información relevante para la partida.
            if (this.categoria == CategoriaJuego.MULTIJUGADOR) {
                g.drawString("Puntos Restantes: " + (puntos.size() + powerUps.size()), 20,
                        laberinto.getFilas() * 32 + 25);
                g.drawString("Vidas Pac-Man: " + vidas, getWidth() - 180, laberinto.getFilas() * 32 + 25);
            }
            // Si es modo clásico/mejorado, muestra la puntuación y las vidas.
            else {
                g.drawString("Puntuación: " + puntuacion, 20, laberinto.getFilas() * 32 + 25);
                g.drawString("Vidas: " + vidas, getWidth() - 100, laberinto.getFilas() * 32 + 25);
            }
        }

        // Dibujo de pantallas superpuestas (Game Over, Pausa)
        if (mostrarPantallaFinal) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(colorMensajeFinal);
            int anchoTexto = g.getFontMetrics().stringWidth(mensajeFinal);
            g.drawString(mensajeFinal, (getWidth() - anchoTexto) / 2, getHeight() / 2 - 20);
        } else if (estadoJuego == GameState.PAUSA) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.YELLOW);
            String texto = "PAUSA";
            int anchoTexto = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, (getWidth() - anchoTexto) / 2, getHeight() / 2);
        }
    }
}