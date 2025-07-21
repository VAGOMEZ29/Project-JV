package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaRojo extends Fantasma {

    private final Point esquinaObjetivo;

    public FantasmaRojo(Point posicion, Image imagen, double velocidad, Laberinto laberinto) {
        super(posicion, imagen, velocidad);
        int tile = 32;
        this.esquinaObjetivo = new Point((laberinto.getColumnas() - 2) * tile, 1 * tile);
    }

    @Override
    public Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal) {
        if (modoGlobal == ModoGlobalIA.DISPERSAR) {
            return this.esquinaObjetivo;
        }
        // Modo PERSEGUIR:
        return pacman.getPosicion();
    }

    @Override
    public int getPrioridad() {
        return 1;
    }
}