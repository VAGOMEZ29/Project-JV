package Modelo;

import java.awt.Image;
import java.awt.Point;

public abstract class Personaje extends ElementoJuego {
    protected double velocidad;
    protected Direccion direccion;

    public Personaje(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen);
        this.velocidad = velocidad;
        this.direccion = Direccion.IZQUIERDA;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public abstract void mover(Direccion direccion);
}
