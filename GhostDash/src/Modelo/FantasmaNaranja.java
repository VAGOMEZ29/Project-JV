package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaNaranja extends Fantasma {

    private final Point esquinaObjetivo;
    private static final int DISTANCIA_HUIDA_SQ = (8 * 32) * (8 * 32);

    public FantasmaNaranja(Point posicion, Image imagen, double velocidad, Laberinto laberinto) {
        super(posicion, imagen, velocidad);
        int tile = 32;
        this.esquinaObjetivo = new Point(1 * tile, (laberinto.getFilas() - 2) * tile);
    }

    @Override
    public Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal) {
        if (modoGlobal == ModoGlobalIA.DISPERSAR) {
            return this.esquinaObjetivo;
        }
        // Modo PERSEGUIR:
        if (this.getPosicion().distanceSq(pacman.getPosicion()) > DISTANCIA_HUIDA_SQ) {
            return pacman.getPosicion();
        } else {
            return this.esquinaObjetivo;
        }
    }
}