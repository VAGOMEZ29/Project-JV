package Vista;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import Modelo.*;

public class ControladorGrafico implements KeyListener {
    private VistaJuego vista;
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;
    private List<Punto> puntos;

    public void iniciar() {
        cargarLaberintoYElementos();
        crearVentanaJuego();
        iniciarBucleJuego();
        iniciarBucleFantasma();
    }

    private void cargarLaberintoYElementos() {
        String[] mapa = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXXXXXXX",
            "OOOX X       OOOOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        };

        int filas = mapa.length;
        int columnas = mapa[0].length();
        char[][] diseno = new char[filas][columnas];

        fantasmas = new ArrayList<>();
        frutas = new ArrayList<>();
        puntos = new ArrayList<>();

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                diseno[f][c] = mapa[f].charAt(c);
            }
        }

        laberinto = new Laberinto();
        laberinto.setDiseno(diseno);

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                char simbolo = mapa[f].charAt(c);
                Point posicion = new Point(c * 32, f * 32);

                switch (simbolo) {
                    case 'P' -> pacman = new PacMan(posicion);
                    case 'b' -> fantasmas.add(new Fantasma(posicion, cargarImagen("blueGhost.png"), 2.0, EstadoFantasma.NORMAL, TipoIA.PERSEGUIR));
                    case 'p' -> fantasmas.add(new Fantasma(posicion, cargarImagen("pinkGhost.png"), 2.0, EstadoFantasma.NORMAL, TipoIA.PATRULLAR));
                    case 'o' -> fantasmas.add(new Fantasma(posicion, cargarImagen("orangeGhost.png"), 2.0, EstadoFantasma.NORMAL, TipoIA.ALEATORIA));
                    case 'r' -> fantasmas.add(new Fantasma(posicion, cargarImagen("redGhost.png"), 2.0, EstadoFantasma.NORMAL, TipoIA.PERSEGUIR));
                    case 'F' -> frutas.add(new Fruta(posicion, cargarImagen("cherry.png"), 100, 5000));
                    case ' ', 'O' -> puntos.add(new Punto(posicion, 10));
                }
            }
        }
    }

    private void crearVentanaJuego() {
        vista = new VistaJuego(laberinto, pacman, fantasmas, frutas, puntos);
        JFrame ventana = new JFrame("GhostDash");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.add(vista);
        ventana.pack();
        ventana.setResizable(false);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        ventana.addKeyListener(this);
        ventana.setFocusable(true);
        ventana.requestFocusInWindow();
    }

    private void iniciarBucleJuego() {
        int delay = 300; //controla qeu tan rapido se mueve pacman
        Timer timer = new Timer(delay, e -> {
            if (puedeMover(pacman.getPosicion(), pacman.getDireccion())) {
                pacman.mover(pacman.getDireccion());
                aplicarEfectoTunel(pacman);
            }
            vista.repaint();
        });
        timer.start();
    }

    private void iniciarBucleFantasma() {
        int delay = 500;
        Timer timerFantasma = new Timer(delay, e -> {
            for (Fantasma fantasma : fantasmas) {
                moverFantasma(fantasma);
                aplicarEfectoTunel(fantasma);
            }
            vista.repaint();
        });
        timerFantasma.start();
    }

    private void moverFantasma(Fantasma fantasma) {
        Direccion nuevaDireccion;

        switch (fantasma.getTipoIA()) {
            case PERSEGUIR -> nuevaDireccion = calcularDireccionHaciaPacman(fantasma);
            case PATRULLAR -> nuevaDireccion = direccionPatrullaje(fantasma);
            case ALEATORIA -> nuevaDireccion = Direccion.aleatoria();
            default -> nuevaDireccion = Direccion.IZQUIERDA;
        }

        if (puedeMover(fantasma.getPosicion(), nuevaDireccion)) {
            fantasma.setDireccion(nuevaDireccion);
            fantasma.mover(nuevaDireccion);
        }
    }

    private Direccion calcularDireccionHaciaPacman(Fantasma f) {
        Point posF = f.getPosicion();
        Point posP = pacman.getPosicion();
        int dx = posP.x - posF.x;
        int dy = posP.y - posF.y;
        return (Math.abs(dx) > Math.abs(dy)) ? (dx > 0 ? Direccion.DERECHA : Direccion.IZQUIERDA)
                                             : (dy > 0 ? Direccion.ABAJO : Direccion.ARRIBA);
    }
    
    private Direccion direccionPatrullaje(Fantasma f) {
        Point pos = f.getPosicion();
        Direccion actual = f.getDireccion();
        //Si puede seguir recto que lo haga
        if (puedeMover(pos, actual)) return actual;

        List<Direccion> opciones = new ArrayList<>();

        for (Direccion dir : Direccion.values()) {
            if (dir != actual.invertir() && puedeMover(pos, dir)) {
                opciones.add(dir);
            }
        }

        if (opciones.isEmpty()) {
            return actual.invertir(); // Solo si no hay más caminos
        }

        return opciones.get((int)(Math.random() * opciones.size()));
    }

    private boolean puedeMover(Point posicion, Direccion direccion) {
        int paso = 32;
        int nuevaX = posicion.x + direccion.getDx() * paso;
        int nuevaY = posicion.y + direccion.getDy() * paso;

        int columnas = laberinto.getDiseno()[0].length;
        int filas = laberinto.getDiseno().length;

        // movimiento del tunel horizontal
        if ((direccion == Direccion.IZQUIERDA && posicion.x <= 0) ||
            (direccion == Direccion.DERECHA && posicion.x >= (columnas - 1) * paso)) {
            return true;
        }

        int col = nuevaX / paso;
        int fila = nuevaY / paso;

        if (fila < 0 || fila >= filas || col < 0 || col >= columnas) return false;

        return laberinto.getDiseno()[fila][col] != 'X';
    }

    private void aplicarEfectoTunel(Personaje personaje) {
        int paso = 32;
        int columnas = laberinto.getDiseno()[0].length;
        int maxX = (columnas - 1) * paso;
        int margen = paso /2;

        Point pos = personaje.getPosicion();
        int x= pos.x;
        int y= pos.y;

        if (x < margen) {
            personaje.setPosicion(new Point(maxX, y));
        } else if (x > maxX + margen) {
            personaje.setPosicion(new Point(0, y));
        }
    }

    private Image cargarImagen(String nombreArchivo) {
        try {
            InputStream is = getClass().getResourceAsStream("/imgs/" + nombreArchivo);
            if (is != null) return ImageIO.read(is);

            File archivo = new File("src/imgs/" + nombreArchivo);
            if (archivo.exists()) return ImageIO.read(archivo);

            System.err.println("❌ No se encontró: " + nombreArchivo);
        } catch (Exception e) {
            System.err.println("❌ Error al cargar: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> pacman.setDireccion(Direccion.DERECHA);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> pacman.setDireccion(Direccion.IZQUIERDA);
            case KeyEvent.VK_UP, KeyEvent.VK_W -> pacman.setDireccion(Direccion.ARRIBA);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> pacman.setDireccion(Direccion.ABAJO);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
