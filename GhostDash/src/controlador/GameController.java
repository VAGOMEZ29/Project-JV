package controlador;

import modelo.*; // Importa todas las clases del modelo
import vista.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GameController actúa como el gestor de una partida en curso.
 * Prepara el nivel y actualiza el estado de todos los elementos del juego
 * en cada fotograma. Ya no controla el bucle principal del juego.
 */
public class GameController {

    private GamePanel vista;
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;
    private List<Punto> puntos;

    // El método iniciar() ha sido reemplazado.
    // El GameManager ahora se encarga de la inicialización general.

    /**
     * Prepara todos los elementos necesarios para una partida (laberinto,
     * personajes, etc.)
     * y crea el panel de juego (GamePanel) para ser mostrado en la ventana
     * principal.
     * 
     * @return El GamePanel listo para ser añadido al JFrame.
     */
    public GamePanel prepararJuego() {
        cargarLaberintoYElementos();

        // Crea la vista (el panel) y la devuelve para que el GameManager la gestione.
        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos);
        return vista;
    }

    /**
     * Carga el diseño del laberinto desde un mapa de caracteres y crea
     * una instancia de cada elemento del juego (Pac-Man, fantasmas, puntos)
     * en su posición inicial.
     */
    private void cargarLaberintoYElementos() {
        String[] mapa = {
                "XXXXXXXXXXXXXXXXXXX",
                "X........X........X",
                "X.XX.XXX.X.XXX.XX.X",
                "X.................X",
                "X.XX.X.XXXXX.X.XX.X",
                "X....X.......X....X",
                "XXXX.XXXX.X.XXXXXXX",
                "OOOO.X... ...X.OOOOO",
                "XXXX.X.XXrXX.X.XXXX",
                "O......bp.o.......O",
                "XXXX.X.XXXXX.X.XXXX",
                "OOOO.X.......X.XOOO",
                "XXXX.X.XXXXX.X.XXXX",
                "X........X........X",
                "X.XX.XXX.X.XXX.XX.X",
                "X..X.....P.....X..X",
                "XX.X.X.XXXXX.X.X.XX",
                "X....X...X...X....X",
                "X.XXXXXX.X.XXXXXX.X",
                "X.................X",
                "XXXXXXXXXXXXXXXXXXX"
        };
        // Nota: He reemplazado los espacios en blanco por '.' para mayor claridad.
        // La lógica para crear los puntos debe ajustarse si es necesario.

        int filas = mapa.length;
        int columnas = mapa[0].length();
        char[][] diseno = new char[filas][columnas];

        fantasmas = new ArrayList<>();
        frutas = new ArrayList<>();
        puntos = new ArrayList<>();
        laberinto = new Laberinto();

        for (int f = 0; f < filas; f++) {
            diseno[f] = mapa[f].toCharArray();
        }
        laberinto.setDiseno(diseno);

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                char simbolo = diseno[f][c];
                Point posicion = new Point(c * 32, f * 32);

                switch (simbolo) {
                    case 'P' -> pacman = new PacMan(posicion);

                    // ¡AQUÍ ESTÁ EL CAMBIO CLAVE! Se instancian las clases específicas de
                    // fantasmas.
                    case 'r' -> fantasmas.add(new FantasmaRojo(posicion, cargarImagen("redGhost.png"), 1.0));
                    case 'p' -> fantasmas.add(new FantasmaRosa(posicion, cargarImagen("pinkGhost.png"), 1.0));
                    case 'b' -> fantasmas.add(new FantasmaAzul(posicion, cargarImagen("blueGhost.png"), 1.0));
                    case 'o' -> fantasmas.add(new FantasmaNaranja(posicion, cargarImagen("orangeGhost.png"), 1.0));

                    case 'F' -> frutas.add(new Fruta(posicion, cargarImagen("cherry.png"), 100, 5000));

                    // Si el espacio está vacío (representado por '.') o es un túnel ('O') se añade
                    // un punto.
                    case '.', 'O' -> puntos.add(new Punto(posicion, 10));
                }
            }
        }
    }

    public void actualizarJuego() {
        // 1. Actualizar la lógica de movimiento de Pac-Man
        // Comprobamos si Pac-Man puede moverse AHORA y si la casilla destino es válida.
        if (pacman.puedeMoverseAhora() && laberinto.puedeMover(pacman.getPosicion(), pacman.getDireccion())) {
            pacman.mover(pacman.getDireccion());
            aplicarEfectoTunel(pacman);
        }

        // 2. Actualizar la lógica de movimiento de cada Fantasma
        for (Fantasma fantasma : fantasmas) {
            // Comprobamos si el FANTASMA puede moverse AHORA.
            if (fantasma.puedeMoverseAhora()) {
                // Si puede, entonces calcula su siguiente movimiento y lo ejecuta.
                fantasma.actualizarMovimiento(pacman, fantasmas, laberinto);
                aplicarEfectoTunel(fantasma);
            }
        }

        // 3. Futura lógica de juego
        // verificarColisiones();
        // verificarComerPuntos();
        // verificarCondicionVictoria();
    }

    /**
     * Gestiona el teletransporte de un personaje cuando llega
     * a los extremos del túnel horizontal del laberinto.
     * 
     * @param personaje El personaje (Pac-Man o Fantasma) a evaluar.
     */
    private void aplicarEfectoTunel(Personaje personaje) {
        int paso = 32;
        int columnas = laberinto.getDiseno()[0].length;
        int maxX = (columnas - 1) * paso;

        Point pos = personaje.getPosicion();

        if (pos.x < -paso / 2) { // Si se ha movido completamente fuera por la izquierda
            personaje.setPosicion(new Point(maxX, pos.y));
        } else if (pos.x > maxX + paso / 2) { // Si se ha movido completamente fuera por la derecha
            personaje.setPosicion(new Point(0, pos.y));
        }
    }

    /**
     * Carga un recurso de imagen desde el classpath o el sistema de archivos.
     * 
     * @param nombreArchivo El nombre del archivo de imagen (ej. "pacman.png").
     * @return un objeto Image o null si ocurre un error.
     */
    private Image cargarImagen(String nombreArchivo) {
        try {
            InputStream is = getClass().getResourceAsStream("/imgs/" + nombreArchivo);
            if (is != null) {
                return ImageIO.read(is);
            }
            File archivo = new File("src/imgs/" + nombreArchivo);
            if (archivo.exists()) {
                return ImageIO.read(archivo);
            }
            System.err.println("❌ Recurso no encontrado: " + nombreArchivo);
        } catch (Exception e) {
            System.err.println("❌ Error al cargar la imagen '" + nombreArchivo + "': " + e.getMessage());
        }
        return null;
    }

    // --- MÉTODOS ELIMINADOS ---
    // iniciarBucleJuego() -> Gestionado por GameManager
    // iniciarBucleFantasma() -> Gestionado por GameManager
    // moverFantasma() -> Lógica movida a las clases de Fantasma
    // calcularDireccionHaciaPacman() -> Lógica movida a FantasmaRojo
    // direccionPatrullaje() -> Lógica movida a FantasmaRosa
    // puedeMover() -> Lógica movida a Laberinto
}