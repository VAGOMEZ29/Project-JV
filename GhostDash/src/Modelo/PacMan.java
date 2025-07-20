package modelo;

import javax.swing.ImageIcon;
import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics;
import java.net.URL;

public class PacMan extends Personaje {
    private int vidas;
    private Image imgUp, imgDown, imgLeft, imgRight;
    private Direccion direccionDeseada;
    private final Point posicionInicial;

    public PacMan(Point posicion) {
        super(posicion, null, 2.0);
        this.posicionInicial = new Point(posicion);
        this.vidas = 3;
        this.imgRight = cargarImagen("pacmanRight.png");
        this.imgLeft = cargarImagen("pacmanLeft.png");
        this.imgUp = cargarImagen("pacmanUp.png");
        this.imgDown = cargarImagen("pacmanDown.png");
        this.direccion = Direccion.DERECHA;
        this.direccionDeseada = Direccion.NINGUNA;
        this.imagen = imgRight;
    }

    public Point getPosicionInicial() {
        return this.posicionInicial;
    }

    @Override
    public void setDireccion(Direccion direccion) {
        if (direccion == Direccion.NINGUNA)
            return;
        this.direccion = direccion;
        switch (direccion) {
            case ARRIBA -> imagen = imgUp;
            case ABAJO -> imagen = imgDown;
            case IZQUIERDA -> imagen = imgLeft;
            case DERECHA -> imagen = imgRight;
        }
    }

    public Direccion getDireccionDeseada() {
        return direccionDeseada;
    }

    public void setDireccionDeseada(Direccion direccionDeseada) {
        this.direccionDeseada = direccionDeseada;
    }

    private Image cargarImagen(String nombreArchivo) {
        try {
            String ruta = "/resources/imgs/" + nombreArchivo;
            URL url = getClass().getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró el recurso: " + ruta);
                return null;
            }
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + nombreArchivo + " -> " + e.getMessage());
            return null;
        }
    }

    public int getVidas() {
        return vidas;
    }

    public void morir() {
        vidas--;
    }

    @Override
    public void mover(Direccion direccion) {
        int paso = 32;
        setDireccion(direccion);
        switch (direccion) {
            case ARRIBA -> posicion.translate(0, -paso);
            case ABAJO -> posicion.translate(0, paso);
            case IZQUIERDA -> posicion.translate(-paso, 0);
            case DERECHA -> posicion.translate(paso, 0);
            case NINGUNA -> {
            }
        }
    }

    @Override
    public void dibujar(Graphics g) {
        if (imagen != null && posicion != null) {
            int ancho = 30, alto = 30;
            int offsetX = (32 - ancho) / 2, offsetY = (32 - alto) / 2;
            g.drawImage(imagen, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
        }
    }
}