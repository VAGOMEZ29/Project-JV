package modelo;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase Nivel se encarga de cargar la estructura y los elementos
 * de un nivel específico desde un archivo de texto (.txt).
 */
public class Nivel {

    /**
     * Carga todos los datos de un nivel (laberinto, personajes, puntos, etc.)
     * desde un archivo de recursos.
     *
     * @param numero    El número del nivel a cargar (ej. 1 para "nivel1.txt").
     * @param categoria El modo de juego actual (CLASICO, CLASICO_MEJORADO).
     * @return Un objeto NivelInfo que contiene todos los elementos del nivel
     *         cargado.
     */
    public static NivelInfo cargarNivel(int numero, CategoriaJuego categoria) {
        // Inicializamos las listas para almacenar los elementos del juego.
        List<Fantasma> fantasmas = new ArrayList<>();
        List<Fruta> frutas = new ArrayList<>();
        List<Punto> puntos = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        Laberinto laberinto = new Laberinto();
        PacMan pacman = null;

        // Construimos la ruta al archivo de nivel dentro de la carpeta de recursos.
        String ruta = "/resources/niveles/nivel" + numero + ".txt";

        try (InputStream is = Nivel.class.getResourceAsStream(ruta);
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println("❌ No se encontró el archivo del nivel: " + ruta);
                return null;
            }

            // Leemos el archivo línea por línea para construir el diseño del laberinto.
            List<char[]> filas = new ArrayList<>();
            String linea;
            while ((linea = br.readLine()) != null) {
                filas.add(linea.toCharArray());
            }

            // Convertimos la lista de filas a un array 2D de caracteres.
            char[][] diseno = new char[filas.size()][];
            filas.toArray(diseno);
            laberinto.setDiseno(diseno);

            // Recorremos el diseño del laberinto para crear los objetos del juego.
            for (int f = 0; f < diseno.length; f++) {
                for (int c = 0; c < diseno[f].length; c++) {
                    char ch = diseno[f][c];
                    Point posicion = new Point(c * 32, f * 32);

                    switch (ch) {
                        case 'P' -> pacman = new PacMan(posicion);

                        // Creación de los fantasmas.
                        case 'r' ->
                            fantasmas.add(new FantasmaRojo(posicion, cargarImagen("redGhost.png"), 2.0, laberinto));
                        case 'p' ->
                            fantasmas.add(new FantasmaRosa(posicion, cargarImagen("pinkGhost.png"), 2.0, laberinto));
                        case 'b' ->
                            fantasmas.add(new FantasmaAzul(posicion, cargarImagen("blueGhost.png"), 2.0, laberinto));
                        case 'o' -> fantasmas
                                .add(new FantasmaNaranja(posicion, cargarImagen("orangeGhost.png"), 2.0, laberinto));

                        // --- CAMBIO IMPLEMENTADO AQUÍ ---
                        // El caracter 'f' ahora SIEMPRE crea un PowerUp de invencibilidad,
                        // sin importar si el modo es Clásico o Mejorado.
                        case 'f' -> powerUps.add(new PowerUp(posicion, cargarImagen("powerFood.png"),
                                TipoPowerUp.INVENCIBILIDAD, 5000));

                        // Creación de Frutas.
                        case 'F' -> frutas.add(new Fruta(posicion, cargarImagen("cherry.png"), 100, 5000));

                        // Creación de Puntos normales.
                        case '.', 'O' -> puntos.add(new Punto(posicion, 10));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico cargando nivel desde archivo: " + e.getMessage());
            e.printStackTrace();
            return null; // Devolvemos null si la carga falla.
        }

        // Encapsulamos todos los elementos cargados en un objeto NivelInfo y lo
        // devolvemos.
        return new NivelInfo(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
    }

    /**
     * Método de utilidad para cargar una imagen desde la carpeta de recursos.
     *
     * @param nombreArchivo El nombre del archivo de imagen (ej. "pacmanRight.png").
     * @return un objeto Image, o null si la imagen no se encuentra.
     */
    private static Image cargarImagen(String nombreArchivo) {
        try {
            URL url = Nivel.class.getResource("/resources/imgs/" + nombreArchivo);
            if (url == null) {
                System.err.println("❌ No se encontró la imagen en recursos: " + nombreArchivo);
                return null;
            }
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("❌ Error al cargar imagen '" + nombreArchivo + "': " + e.getMessage());
            return null;
        }
    }
}