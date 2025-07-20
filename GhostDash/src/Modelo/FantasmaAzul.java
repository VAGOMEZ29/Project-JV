package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaAzul extends Fantasma {

    private final Point esquinaObjetivo;

    public FantasmaAzul(Point posicion, Image imagen, double velocidad, Laberinto laberinto) {
        super(posicion, imagen, velocidad);
        int tile = 32;
        this.esquinaObjetivo = new Point((laberinto.getColumnas() - 2) * tile, (laberinto.getFilas() - 2) * tile);
    }

    @Override
    public Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal) {
        if (modoGlobal == ModoGlobalIA.DISPERSAR) {
            return this.esquinaObjetivo;
        }
        // Modo PERSEGUIR:
        Fantasma fantasmaRojo = null;
        for (Fantasma f : fantasmas) {
            if (f instanceof FantasmaRojo) {
                fantasmaRojo = f;
                break;
            }
        }
        if (fantasmaRojo == null)
            return pacman.getPosicion();
        Point pivote = new Point(pacman.getPosicion());
        pivote.translate(pacman.getDireccion().getDx() * 64, pacman.getDireccion().getDy() * 64);
        Point posBlinky = fantasmaRojo.getPosicion();
        int vecX = pivote.x - posBlinky.x;
        int vecY = pivote.y - posBlinky.y;
        return new Point(pivote.x + vecX, pivote.y + vecY);
    }
}