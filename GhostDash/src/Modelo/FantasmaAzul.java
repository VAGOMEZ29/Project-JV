package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaAzul extends Fantasma {

    public FantasmaAzul(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad, EstadoFantasma.NORMAL);
    }

    @Override
    public void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto) {
        // Paso 1: Encontrar al fantasma rojo (Blinky)
        Fantasma fantasmaRojo = null;
        for (Fantasma f : fantasmas) {
            if (f instanceof FantasmaRojo) {
                fantasmaRojo = f;
                break;
            }
        }

        // Si Blinky no está, Inky se comporta de forma simple (persigue como Blinky)
        if (fantasmaRojo == null) {
            Direccion dirSimple = calcularMejorDireccion(pacman.getPosicion(), laberinto);
            this.mover(dirSimple);
            return;
        }

        // Paso 2: Calcular la casilla intermedia (2 casillas delante de Pac-Man)
        Point posicionIntermedia = new Point(pacman.getPosicion());
        Direccion dirPacman = pacman.getDireccion();
        posicionIntermedia.translate(dirPacman.getDx() * 32 * 2, dirPacman.getDy() * 32 * 2);

        // Paso 3: Calcular el vector desde Blinky a la casilla intermedia
        Point posBlinky = fantasmaRojo.getPosicion();
        int vectorX = posicionIntermedia.x - posBlinky.x;
        int vectorY = posicionIntermedia.y - posBlinky.y;

        // Paso 4: El objetivo final de Inky es el resultado de ese vector
        Point objetivoFinal = new Point(posicionIntermedia.x + vectorX, posicionIntermedia.y + vectorY);

        // Calculamos la mejor dirección hacia ese complejo objetivo y nos movemos
        Direccion nuevaDireccion = calcularMejorDireccion(objetivoFinal, laberinto);
        this.mover(nuevaDireccion);
    }
}