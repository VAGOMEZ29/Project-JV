package Vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import Modelo.*;

public class VistaJuego extends JPanel implements KeyListener {
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;
    private List<Punto> puntos;
    private int puntuacion;
    private int vidas;
    private boolean gameOver;

    public VistaJuego(Laberinto laberinto, PacMan pacman, List<Fantasma> fantasmas,
            List<Fruta> frutas, List<Punto> puntos) {
        this.laberinto = laberinto;
        this.pacman = pacman;
        this.fantasmas = fantasmas;
        this.frutas = frutas;
        this.puntos = puntos;

        // Configuración inicial del panel
        setPreferredSize(new Dimension(608, 672)); // Tamaño del tablero
        setBackground(Color.BLACK);
        setFocusable(true); // Permite recibir eventos de teclado
        addKeyListener(this);

        // Debug inicial
        System.out.println("[DEBUG] Laberinto cargado: " + (laberinto != null));
        System.out.println("[DEBUG] PacMan cargado en: " + pacman.getPosicion());
        System.out.println("[DEBUG] Fantasmas cargados: " + fantasmas.size());
    }

    public void actualizarEstado(int puntuacion, int vidas, boolean gameOver) {
        this.puntuacion = puntuacion;
        this.vidas = vidas;
        this.gameOver = gameOver;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Dibujar laberinto (paredes)
        if (laberinto != null && laberinto.getDiseno() != null) {
            char[][] diseño = laberinto.getDiseno();
            for (int fila = 0; fila < diseño.length; fila++) {
                for (int col = 0; col < diseño[0].length; col++) {
                    if (diseño[fila][col] == 'X') {
                        g.setColor(Color.BLUE);
                        g.fillRect(col * 32, fila * 32, 32, 32);
                    }
                }
            }
        }

        // 2. Dibujar puntos
        for (Punto p : puntos) {
            if (p != null)
                p.dibujar(g);
        }

        // 3. Dibujar frutas
        for (Fruta fruta : frutas) {
            if (fruta != null && fruta.getImagen() != null) {
                g.drawImage(fruta.getImagen(), fruta.getPosicion().x, fruta.getPosicion().y, this);
            }
        }

        // 4. Dibujar fantasmas
        for (Fantasma fantasma : fantasmas) {
            if (fantasma != null && fantasma.getImagen() != null) {
                g.drawImage(fantasma.getImagen(), fantasma.getPosicion().x, fantasma.getPosicion().y, this);
            }
        }

        // 5. Dibujar PacMan
        if (pacman != null && pacman.getImagen() != null) {
            g.drawImage(pacman.getImagen(), pacman.getPosicion().x, pacman.getPosicion().y, this);
        }

        // 6. Dibujar información de juego
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Puntuación: " + puntuacion, 20, 20);
        g.drawString("Vidas: " + vidas, 500, 20);

        // 7. Dibujar Game Over si corresponde
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String texto = "GAME OVER";
            int anchoTexto = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, (getWidth() - anchoTexto) / 2, getHeight() / 2);
        }
    }

    // Implementación de KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        Direccion dir = switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> Direccion.ARRIBA;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> Direccion.ABAJO;
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> Direccion.IZQUIERDA;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Direccion.DERECHA;
            default -> null;
        };

        if (dir != null && pacman != null) {
            pacman.setDireccion(dir);
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No se requiere acción al soltar teclas
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No se requiere acción al teclear
    }
}