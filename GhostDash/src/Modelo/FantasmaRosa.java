package modelo;

import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class FantasmaRosa extends Fantasma {

    public FantasmaRosa(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad, EstadoFantasma.NORMAL);
    }

    @Override
    public void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto) {
        // Lógica de Patrullaje (copiada y adaptada de tu GameController)
        Point pos = this.getPosicion();
        Direccion actual = this.getDireccion();

        // Intenta seguir recto si es posible
        if (laberinto.puedeMover(pos, actual)) {
            this.mover(actual);
            return;
        }

        // Si no puede, busca una nueva dirección en las intersecciones
        List<Direccion> opciones = new ArrayList<>();
        for (Direccion dir : Direccion.values()) {
            if (dir != actual.invertir() && laberinto.puedeMover(pos, dir)) {
                opciones.add(dir);
            }
        }

        if (!opciones.isEmpty()) {
            Direccion nuevaDireccion = opciones.get((int) (Math.random() * opciones.size()));
            this.setDireccion(nuevaDireccion);
            this.mover(nuevaDireccion);
        } else {
            // Si está en un callejón sin salida, da la vuelta
            this.setDireccion(actual.invertir());
            this.mover(actual.invertir());
        }
    }
}