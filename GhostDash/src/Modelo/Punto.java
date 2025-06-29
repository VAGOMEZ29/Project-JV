package Modelo;

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;

public class Punto extends ElementoJuego {
    private int valor;

    public Punto(Point posicion, int valor) {
        super(posicion, null);
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    @Override
    public void dibujar(Graphics g) {
        // Dibuja punto
        int xCentrado = posicion.x + 12;
        int yCentrado = posicion.y + 12;
        g.setColor(Color.WHITE);
        g.fillOval(xCentrado, yCentrado, 8, 8);
    }
}
