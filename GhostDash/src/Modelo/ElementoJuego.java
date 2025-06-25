package Modelo;

import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics;

public abstract class ElementoJuego {
    protected Point posicion;
    protected Image imagen;

    public ElementoJuego(Point posicion, Image imagen) {
        this.posicion = posicion;
        this.imagen = imagen;
    }

    public abstract void dibujar(Graphics g);
    
    public Point getPosicion() {
        return posicion;
    }

    public void setPosicion(Point posicion) {
        this.posicion = posicion;
    }

    public Image getImagen() {
        return imagen;
    }

    public void setImagen(Image imagen) {
        this.imagen = imagen;
    }
}
