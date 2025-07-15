package modelo;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Carga los niveles del juego desde archivos
public class Nivel {

    public static NivelInfo cargarNivel(int numero, CategoriaJuego categoria) {
        List<Fantasma> fantasmas = new ArrayList<>();
        List<Fruta> frutas = new ArrayList<>();
        List<Punto> puntos = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        Laberinto laberinto = new Laberinto();
        PacMan pacman = null;

        String ruta = "/resources/niveles/nivel" + numero + ".txt";

        try {
            URL url = Nivel.class.getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró el archivo del nivel: " + ruta);
                return null;
            }

            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            List<char[]> filas = new ArrayList<>();
            String linea;
            while ((linea = br.readLine()) != null) {
                filas.add(linea.toCharArray());
            }

            char[][] diseno = new char[filas.size()][];
            for (int i = 0; i < filas.size(); i++) {
                diseno[i] = filas.get(i);
            }

            laberinto.setDiseno(diseno);

            for (int f = 0; f < diseno.length; f++) {
                for (int c = 0; c < diseno[0].length; c++) {
                    char ch = diseno[f][c];
                    Point posicion = new Point(c * 32, f * 32);

                    switch (ch) {
                        case 'P' -> pacman = new PacMan(posicion);
                        case 'r' -> fantasmas.add(new FantasmaRojo(posicion, cargarImagen("redGhost.png"), 1.0));
                        case 'p' -> fantasmas.add(new FantasmaRosa(posicion, cargarImagen("pinkGhost.png"), 1.0));
                        case 'b' -> fantasmas.add(new FantasmaAzul(posicion, cargarImagen("blueGhost.png"), 1.0));
                        case 'o' -> fantasmas.add(new FantasmaNaranja(posicion, cargarImagen("orangeGhost.png"), 1.0));
                        case 'f' -> {
                            if (categoria == CategoriaJuego.CLASICO_MEJORADO) {
                                System.out.println("powerup cargado en"+ posicion);
                                powerUps.add(new PowerUp(posicion, cargarImagen("powerFood.png"), TipoPowerUp.INVENCIBILIDAD, 5000));
                            }
                        }
                        case 'F' -> frutas.add(new Fruta(posicion, cargarImagen("cherry.png"), 100, 5000));
                        case '.', 'O' -> puntos.add(new Punto(posicion, 10));
                    }
                }
            }

            br.close();

        } catch (Exception e) {
            System.err.println("Error cargando nivel desde archivo: " + e.getMessage());
            e.printStackTrace();
        }

        return new NivelInfo(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
    }

    private static Image cargarImagen(String nombreArchivo) {
        try {
            String ruta = "/resources/imgs/" + nombreArchivo;
            URL url = Nivel.class.getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró la imagen: " + ruta);
                return null;
            }
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen '" + nombreArchivo + "': " + e.getMessage());
            return null;
        }
    }
}
