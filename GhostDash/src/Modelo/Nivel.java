package modelo;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Nivel {

    public static NivelInfo cargarNivel(int numero, CategoriaJuego categoria) {
        List<Fantasma> fantasmas = new ArrayList<>();
        List<Fruta> frutas = new ArrayList<>();
        List<Punto> puntos = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        Laberinto laberinto = new Laberinto();
        PacMan pacman = null;
        Point posicionAparicionFruta = null;

        String ruta = "/resources/niveles/nivel" + numero + ".txt";

        try (InputStream is = Nivel.class.getResourceAsStream(ruta);
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println("❌ No se encontró el archivo del nivel: " + ruta);
                return null;
            }

            List<char[]> filas = new ArrayList<>();
            String linea;
            while ((linea = br.readLine()) != null) {
                filas.add(linea.toCharArray());
            }

            char[][] diseno = new char[filas.size()][];
            filas.toArray(diseno);
            laberinto.setDiseno(diseno);

            for (int f = 0; f < diseno.length; f++) {
                for (int c = 0; c < diseno[f].length; c++) {
                    char ch = diseno[f][c];
                    Point posicion = new Point(c * 32, f * 32);

                    switch (ch) {
                        case 'P' -> pacman = new PacMan(posicion);

                        case 'r' -> { // Fantasma Rojo
                            if (categoria == CategoriaJuego.MULTIJUGADOR) {
                                fantasmas.add(new FantasmaJugador(posicion, cargarImagen("redGhost.png"), 2.0));
                            } else {
                                fantasmas.add(new FantasmaRojo(posicion, cargarImagen("redGhost.png"), 2.0, laberinto));
                            }
                        }
                        case 'p' -> { // Fantasma Rosa
                            fantasmas.add(new FantasmaRosa(posicion, cargarImagen("pinkGhost.png"), 2.0, laberinto));
                        }

                        case 'b' -> { // Fantasma Azul
                            if (categoria != CategoriaJuego.MULTIJUGADOR) {
                                fantasmas
                                        .add(new FantasmaAzul(posicion, cargarImagen("blueGhost.png"), 2.0, laberinto));
                            }
                        }
                        case 'o' -> { // Fantasma Naranja
                            if (categoria != CategoriaJuego.MULTIJUGADOR) {
                                fantasmas.add(
                                        new FantasmaNaranja(posicion, cargarImagen("orangeGhost.png"), 2.0, laberinto));
                            }
                        }

                        case 'f' -> powerUps.add(
                                new PowerUp(posicion, cargarImagen("powerFood.png"), TipoPowerUp.INVENCIBILIDAD, 5000));
                        case 'F' -> posicionAparicionFruta = posicion;
                        case '.', 'O' -> puntos.add(new Punto(posicion, 10));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico cargando nivel desde archivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return new NivelInfo(laberinto, pacman, fantasmas, frutas, puntos, powerUps, posicionAparicionFruta);
    }

    public static Image cargarImagen(String nombreArchivo) {
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