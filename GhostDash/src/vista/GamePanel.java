package vista;

import modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GamePanel extends JPanel {

    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;
    private List<Punto> puntos;
    private List<PowerUp> powerUps;

    private boolean juegoTerminado = false;
    private boolean juegoGanado = false;

    private int puntuacion;
    private int vidas;

    private JButton botonReiniciar;
    private Runnable reiniciarListener;

    public GamePanel(Laberinto laberinto, PacMan pacman, List<Fantasma> fantasmas,
                     List<Fruta> frutas, List<Punto> puntos, List<PowerUp> powerUps) {
        this.laberinto = laberinto;
        this.pacman = pacman;
        this.fantasmas = fantasmas;
        this.frutas = frutas;
        this.puntos = puntos;
        this.powerUps = powerUps;

        setPreferredSize(new Dimension(laberinto.getColumnas() * 32, laberinto.getFilas() * 32));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        requestFocusInWindow();

        crearBotonReiniciar();
    }

    private void crearBotonReiniciar() {
        botonReiniciar = new JButton("Jugar de nuevo");
        botonReiniciar.setBounds(300, 350, 200, 40);
        botonReiniciar.setVisible(false);

        // Estilo visual personalizado
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 18));
        botonReiniciar.setBackground(Color.BLACK);
        botonReiniciar.setForeground(Color.YELLOW);
        botonReiniciar.setFocusPainted(false);
        botonReiniciar.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        botonReiniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonReiniciar.setContentAreaFilled(true);
        botonReiniciar.setOpaque(true);

        // Acción al hacer clic
        botonReiniciar.addActionListener(e -> {
            botonReiniciar.setVisible(false);
            if (reiniciarListener != null) reiniciarListener.run();
        });

        add(botonReiniciar);
    }

    public void setReiniciarListener(Runnable listener) {
        this.reiniciarListener = listener;
    }

    public void inicializarControles(PacMan pacman) {
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> pacman.setDireccion(Direccion.ARRIBA);
                    case KeyEvent.VK_DOWN -> pacman.setDireccion(Direccion.ABAJO);
                    case KeyEvent.VK_LEFT -> pacman.setDireccion(Direccion.IZQUIERDA);
                    case KeyEvent.VK_RIGHT -> pacman.setDireccion(Direccion.DERECHA);
                }
            }
        });
    }

    public void actualizarEstado(int puntuacion, int vidas, boolean perdio, boolean gano) {
        this.puntuacion = puntuacion;
        this.vidas = vidas;
        this.juegoTerminado = perdio;
        this.juegoGanado = gano;
        repaint();
    }

    public void mostrarBotonReiniciar() {
        botonReiniciar.setVisible(true);
        requestFocusInWindow();
    }

    public void setPacMan(PacMan pacman) {
        this.pacman = pacman;
    }

    public void setFantasmas(List<Fantasma> fantasmas) {
        this.fantasmas = fantasmas;
    }

    public void setFrutas(List<Fruta> frutas) {
        this.frutas = frutas;
    }

    public void setPowerUps(List<PowerUp> powerUps) {
        this.powerUps = powerUps;
    }

    public void setPuntos(List<Punto> puntos) {
        this.puntos = puntos;
    }

    public void setLaberinto(Laberinto laberinto) {
        this.laberinto = laberinto;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        for (Punto punto : List.copyOf(puntos)) punto.dibujar(g);
        for (Fruta fruta : List.copyOf(frutas)) fruta.dibujar(g);
        for (PowerUp powerUp : List.copyOf(powerUps)) powerUp.dibujar(g);
        for (Fantasma fantasma : List.copyOf(fantasmas)) fantasma.dibujar(g);
        if (pacman != null) pacman.dibujar(g);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Puntos: " + puntuacion, 20, 25);
        g.drawString("Vidas: " + vidas, 160, 25);

        if (juegoTerminado) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, 180)); // negro semitransparente
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("Game Over", getWidth() / 2 - 140, getHeight() / 2);
        } else if (juegoGanado) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.GREEN);
            g.drawString("You Win!", getWidth() / 2 - 110, getHeight() / 2);
        }
    }
}
