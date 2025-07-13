package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.List;

public class FantasmaRojo extends Fantasma {

    public FantasmaRojo(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad, EstadoFantasma.NORMAL);
    }

    @Override
    public void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto) {
        // Lógica de Persecución (copiada de tu GameController)
        Point posF = this.getPosicion();
        Point posP = pacman.getPosicion();
        int dx = posP.x - posF.x;
        int dy = posP.y - posF.y;

        Direccion nuevaDireccion = (Math.abs(dx) > Math.abs(dy))
                ? (dx > 0 ? Direccion.DERECHA : Direccion.IZQUIERDA)
                : (dy > 0 ? Direccion.ABAJO : Direccion.ARRIBA);

        if (laberinto.puedeMover(this.getPosicion(), nuevaDireccion)) {
            this.setDireccion(nuevaDireccion);
            this.mover(nuevaDireccion);
        } else {
            // Lógica de respaldo si choca: buscar otra salida
            // (Por ahora, podemos dejarlo simple, pero esto se puede mejorar)
            Direccion direccionAleatoria = Direccion.aleatoria();
            if (laberinto.puedeMover(this.getPosicion(), direccionAleatoria)) {
                this.setDireccion(direccionAleatoria);
                this.mover(direccionAleatoria);
            }
        }
    }

}