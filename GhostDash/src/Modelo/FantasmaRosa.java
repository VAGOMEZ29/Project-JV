package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaRosa extends Fantasma {

    private final Point esquinaObjetivo;

    public FantasmaRosa(Point posicion, Image imagen, double velocidad, Laberinto laberinto) {
        super(posicion, imagen, velocidad);
        int tile = 32;
        this.esquinaObjetivo = new Point(1 * tile, 1 * tile);
    }

    @Override
    public Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal) {
        if (modoGlobal == ModoGlobalIA.DISPERSAR) {
            return this.esquinaObjetivo;
        }
        // Modo PERSEGUIR:
        Point objetivo = new Point(pacman.getPosicion());
        int offset = 4 * 32;
        objetivo.translate(pacman.getDireccion().getDx() * offset, pacman.getDireccion().getDy() * offset);
        return objetivo;
    }
}