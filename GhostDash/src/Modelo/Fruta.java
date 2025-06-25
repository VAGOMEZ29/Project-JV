package Modelo;

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
        // Dibuja fruta
        if (imagen != null && posicion != null) {
            g.drawImage(imagen, posicion.x, posicion.y, null);
        }
    }
}
