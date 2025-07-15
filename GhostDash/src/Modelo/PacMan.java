package modelo;

import javax.swing.ImageIcon;
import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics;
import java.net.URL;

public class PacMan extends Personaje {
    private int vidas;
    private Image imgUp;
    private Image imgDown;
    private Image imgLeft;
    private Image imgRight;
    private Direccion direccion;

    public PacMan(Point posicion) {
        super(posicion, null, 2.0);
        this.vidas = 3;

        this.imgRight = cargarImagen("pacmanRight.png");
        this.imgLeft = cargarImagen("pacmanLeft.png");
        this.imgUp = cargarImagen("pacmanUp.png");
        this.imgDown = cargarImagen("pacmanDown.png");

        this.direccion = Direccion.DERECHA;
        this.imagen = imgRight;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
        switch (direccion) {
            case ARRIBA -> imagen = imgUp;
            case ABAJO -> imagen = imgDown;
            case IZQUIERDA -> imagen = imgLeft;
            case DERECHA -> imagen = imgRight;
            case NINGUNA -> {
                // No hacer nada, conservar la imagen actual
            } 
        }
    }

    public Direccion getDireccion() {
        return direccion;
    }

    private Image cargarImagen(String nombreArchivo) {
        try {
            // Usar ruta absoluta desde el classpath
            String ruta = "/resources/imgs/" + nombreArchivo;
            URL url = getClass().getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró el recurso: " + ruta);
                return null;
            }
            ImageIcon icon = new ImageIcon(url);
            return icon.getImage();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + nombreArchivo + " → " + e.getMessage());
            return null;
        }
    }

    public int getVidas() {
        return vidas;
    }

    public void comer(ElementoJuego elemento) {
        // Lógica pendiente
    }

    public void usarPowerUp(PowerUp powerUp) {
        powerUp.activar();
    }

    public void morir() {
        vidas--;
    }

    public void setPosicion(Point nuevaPosicion) {
        this.posicion = nuevaPosicion;
    }

    @Override
    public void mover(Direccion direccion) {
        int paso = 32;
        int columnas = 21;
        int maxX = (columnas - 1) * paso;

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
            case NINGUNA ->{
                //no se mueve
            }
        }
        setDireccion(direccion);
    }

    @Override
    public void dibujar(Graphics g) {
        if (imagen != null && posicion != null) {
            int ancho = 30;
            int alto = 30;

            // Centramos la imagen respecto al centro de la celda de 32x32
            int offsetX = (32 - ancho) / 2;
            int offsetY = (32 - alto) / 2;

            g.drawImage(imagen, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
        } else {
            System.err.println("Imagen o posición de PacMan no inicializada.");
        }
    }

    public Image getImagen() {
        return this.imagen;
    }
}
