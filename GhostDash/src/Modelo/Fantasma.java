package modelo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.List;

public abstract class Fantasma extends Personaje {
    private EstadoFantasma estado;
    private Direccion direccion = Direccion.IZQUIERDA; // Dirección inicial por defecto

    public Fantasma(Point posicion, Image imagen, double velocidad, EstadoFantasma estado) {
        super(posicion, imagen, velocidad);
        this.estado = estado;
    }

    public EstadoFantasma getEstado() {
        return estado;
    }

    public void setEstado(EstadoFantasma estado) {
        this.estado = estado;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    @Override
    public void mover(Direccion direccion) {
        int paso = 32;
        int maxCol = 21;
        int maxX = (maxCol - 1) * paso;

        this.direccion = direccion; // Guarda la dirección actual

        switch (direccion) {
            case ARRIBA -> posicion.translate(0, -paso);
            case ABAJO -> posicion.translate(0, paso);
            case IZQUIERDA -> {
                posicion.translate(-paso, 0);
                if (posicion.x < 0) {
                    posicion.x = maxX;
                }
            }
            case DERECHA -> {
                posicion.translate(paso, 0);
                if (posicion.x > maxX) {
                    posicion.x = 0;
                }
            }
        }
    }

    @Override
    public void dibujar(Graphics g) {
        if (imagen != null && posicion != null) {
            g.drawImage(imagen, posicion.x, posicion.y, null);
        }
    }

    public abstract void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto);

    protected Direccion calcularMejorDireccion(Point objetivo, Laberinto laberinto) {
        Direccion mejorDireccion = this.getDireccion().invertir(); // Dirección por defecto (última opción)
        double distanciaMinima = Double.MAX_VALUE;

        // No permitimos que el fantasma dé la vuelta a menos que sea la única opción
        Direccion direccionProhibida = this.getDireccion().invertir();

        for (Direccion dir : Direccion.values()) {
            if (dir == direccionProhibida) {
                continue; // Ignorar la dirección inversa
            }

            if (laberinto.puedeMover(this.getPosicion(), dir)) {
                // Calcula la posición futura si tomamos esta dirección
                Point posicionFutura = new Point(
                        this.getPosicion().x + (dir.getDx() * 32),
                        this.getPosicion().y + (dir.getDy() * 32));

                // Calcula la distancia desde la nueva posición hasta el objetivo
                double distancia = posicionFutura.distanceSq(objetivo); // Usamos distanceSq por eficiencia

                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    mejorDireccion = dir;
                }
            }
        }

        // Si ninguna dirección fue válida, la única opción es dar la vuelta.
        if (mejorDireccion == this.getDireccion().invertir()
                && laberinto.puedeMover(this.getPosicion(), mejorDireccion)) {
            return mejorDireccion;
        }

        return mejorDireccion;
    }
}
