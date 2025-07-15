package modelo;

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
        // Lógica del efecto del PowerUp (se maneja en GameController)
    }

    public void dibujar(Graphics g) {
        if (imagen != null && posicion != null) {
            int ancho = 15;
            int alto = 15;

            // Corregido: centramos la imagen dentro del bloque de 32x32
            int offsetX = (32 - ancho) / 2;
            int offsetY = (32 - alto) / 2;

            g.drawImage(imagen, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
        }
    }
}
