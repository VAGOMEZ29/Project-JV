package Modelo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

public class Fantasma extends Personaje {
    private EstadoFantasma estado;
    private TipoIA tipoIA;
    private Direccion direccion = Direccion.IZQUIERDA; // Dirección inicial por defecto

    public Fantasma(Point posicion, Image imagen, double velocidad, EstadoFantasma estado, TipoIA tipoIA) {
        super(posicion, imagen, velocidad);
        this.estado = estado;
        this.tipoIA = tipoIA;
    }

    public EstadoFantasma getEstado() {
        return estado;
    }

    public void setEstado(EstadoFantasma estado) {
        this.estado = estado;
    }

    public TipoIA getTipoIA() {
        return tipoIA;
    }

    public void setTipoIA(TipoIA tipoIA) {
        this.tipoIA = tipoIA;
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
}
