package Modelo;

import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics;

public class PowerUp extends ElementoJuego {
    private TipoPowerUp tipo;
    private int duracion;

    public PowerUp(Point posicion, Image imagen, TipoPowerUp tipo, int duracion) {
        super(posicion, imagen);
        this.tipo = tipo;
        this.duracion = duracion;
    }

    public TipoPowerUp getTipo() {
        return tipo;
    }

    public int getDuracion() {
        return duracion;
    }

    public void activar() {
        // Activar efecto del PowerUp
    }

    @Override
    public void dibujar(Graphics g) {
        // Dibuja power-up
        if (imagen != null && posicion != null) {
            g.drawImage(imagen, posicion.x, posicion.y, null);
        }
    }
}
