package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaNaranja extends Fantasma {
    // Distancia (en píxeles) a la que Clyde se "asusta". 8 casillas * 32
    // píxeles/casilla.
    private static final int DISTANCIA_HUIDA = 8 * 32;
    private Point esquinaSeguridad;

    public FantasmaNaranja(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad, EstadoFantasma.NORMAL);
    }

    @Override
    public void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto) {
        // La esquina de seguridad de Clyde (abajo a la izquierda)
        if (esquinaSeguridad == null) {
            esquinaSeguridad = new Point(0, (laberinto.getDiseno().length - 1) * 32);
        }

        Point objetivo;
        double distanciaAPacman = this.getPosicion().distance(pacman.getPosicion());

        if (distanciaAPacman > DISTANCIA_HUIDA) {
            // Si está lejos, persigue a Pac-Man directamente (IA de Blinky)
            objetivo = pacman.getPosicion();
        } else {
            // Si está cerca, huye a su esquina
            objetivo = esquinaSeguridad;
        }

        // Calculamos la mejor dirección y nos movemos
        Direccion nuevaDireccion = calcularMejorDireccion(objetivo, laberinto);
        this.mover(nuevaDireccion);
    }
}