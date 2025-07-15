package modelo;

import java.awt.*;
import java.util.List;
import javax.imageio.ImageIO;

public abstract class Fantasma extends Personaje {
    private EstadoFantasma estado = EstadoFantasma.NORMAL;//Estado inicial del fantasma
    private Direccion direccion = Direccion.IZQUIERDA;
    private Point posicionInicial;

    private static Image imagenAsustado;

    private long tiempoInicioHuida;
    private long duracionHuida;
    private double velocidadBase;

    static {
        try {
            imagenAsustado = ImageIO.read(Fantasma.class.getResourceAsStream("/resources/imgs/scaredGhost.png"));
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fantasma asustado: " + e.getMessage());
        }
    }

    public Fantasma(Point posicion, Image imagen, double velocidad, EstadoFantasma estado) {
        super(posicion, imagen, velocidad);
        this.estado = estado;
        this.velocidadBase = velocidad;
        this.posicionInicial = new Point(posicion);
    }

    public void activarHuida(long duracionMs) {
        this.estado = EstadoFantasma.HUIDA;
        this.tiempoInicioHuida = System.currentTimeMillis();
        this.duracionHuida = duracionMs;
        this.setVelocidad(velocidadBase * 0.5);
    }

    public void actualizarEstado() {
        if (estado == EstadoFantasma.HUIDA || estado == EstadoFantasma.PARPADEANDO) {
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicioHuida;
            long tiempoRestante = duracionHuida - tiempoTranscurrido;

            if (tiempoRestante < 2000 && tiempoRestante > 0) {
                estado = EstadoFantasma.PARPADEANDO;
            } else if (tiempoRestante <= 0) {
                estado = EstadoFantasma.NORMAL;
                setVelocidad(velocidadBase);
            }
        }
    }

    public EstadoFantasma getEstado() {
        return estado;
    }

    public void setEstado(EstadoFantasma estado) {
        this.estado = estado;
    }

    public Point getPosicionInicial() {
        return posicionInicial;
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

        this.direccion = direccion;

        switch (direccion) {
            case ARRIBA -> posicion.translate(0, -paso);
            case ABAJO -> posicion.translate(0, paso);
            case IZQUIERDA -> {
                posicion.translate(-paso, 0);
                if (posicion.x < -paso / 2) {
                    posicion.x = maxX;
                }
            }
            case DERECHA -> {
                posicion.translate(paso, 0);
                if (posicion.x > maxX + paso / 2) {
                    posicion.x = 0;
                }
            }
            case NINGUNA -> {}
        }
    }

    @Override
    public void dibujar(Graphics g) {
        if (posicion != null) {
            int ancho = 28;
            int alto = 28;

            int offsetX = (32 - ancho) / 2;
            int offsetY = (32 - alto) / 2;

            Image img = imagen;

            if (estado == EstadoFantasma.HUIDA || estado == EstadoFantasma.PARPADEANDO) {
                long tiempoRestante = (tiempoInicioHuida + duracionHuida) - System.currentTimeMillis();

                if (tiempoRestante < 2000 && imagenAsustado != null) {
                    long intervalo = (System.currentTimeMillis() / 300) % 2;
                    img = (intervalo == 0) ? imagenAsustado : imagen;
                } else {
                    img = imagenAsustado;
                }
            }

            g.drawImage(img, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
        } else {
            System.err.println("[ERROR] Imagen o posición del fantasma no inicializada.");
        }
    }

    public abstract void actualizarMovimiento(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto);

    protected Direccion calcularMejorDireccion(Point objetivo, Laberinto laberinto) {
        Direccion mejorDireccion = this.getDireccion().invertir();
        double distanciaMinima = Double.MAX_VALUE;
        Direccion prohibida = this.getDireccion().invertir();

        for (Direccion dir : Direccion.values()) {
            if (dir == prohibida) continue;
            if (laberinto.puedeMover(this.getPosicion(), dir)) {
                Point futura = new Point(
                        this.getPosicion().x + dir.getDx() * 32,
                        this.getPosicion().y + dir.getDy() * 32);
                double distancia = futura.distanceSq(objetivo);
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    mejorDireccion = dir;
                }
            }
        }

        if (laberinto.puedeMover(this.getPosicion(), mejorDireccion)) {
            return mejorDireccion;
        }

        return Direccion.NINGUNA;
    }
}
