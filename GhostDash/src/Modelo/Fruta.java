package modelo;

import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics;

public class Fruta extends ElementoJuego {
    private int valor;
    private int tiempoVisible;

    public Fruta(Point posicion, Image imagen, int valor, int tiempoVisible) {
        super(posicion, imagen);
        this.valor = valor;
        this.tiempoVisible = tiempoVisible;
    }

    public int getValor() {
        return valor;
    }

    public int getTiempoVisible() {
        return tiempoVisible;
    }

    @Override
    public void dibujar(Graphics g) {
        if (imagen != null && posicion != null) {
            int ancho = 20;
            int alto = 20;

            // Centramos la imagen dentro de la celda de 32x32
            int offsetX = (32 - ancho) / 2;
            int offsetY = (32 - alto) / 2;

            g.drawImage(imagen, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
        }
    }
}
